package com.example.sagerecipes

import com.example.sagerecipes.data.remote.SupabaseProvider
import com.example.sagerecipes.data.repository.AuthRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters

/**
 * Talks to the real Supabase project. Tests run in name order (t01, t02, ...)
 * because later ones rely on the account created by the first. Run the whole class.
 */
@RunWith(JUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class BackendSmokeTest {

    private val client = SupabaseProvider.client
    private val auth = AuthRepository()

    companion object {
        private const val BASE_EMAIL = "your.real.email@gmail.com"   // <-- CHANGE THIS
        private const val PASSWORD = "SageTest#2026"
        private const val NAME = "Test Cook"
        private val TEST_EMAIL = BASE_EMAIL.substringBefore("@") +
                "+sagetest" + System.currentTimeMillis() + "@" + BASE_EMAIL.substringAfter("@")
    }

    // ---------- Authentication ----------

    @Test
    fun t01_signUp_createsAccountAndSession() = runBlocking<Unit> {
        runCatching { auth.signOut() }
        val hasSession = auth.signUp(TEST_EMAIL, NAME, PASSWORD)
        assertTrue(
            "No session after sign-up. Turn OFF 'Confirm email' in Supabase (Authentication > Providers > Email).",
            hasSession,
        )
    }

    @Test
    fun t02_profileRow_isCreatedByTrigger() = runBlocking<Unit> {
        val rows = client.from("profiles").select().decodeList<ProfileRow>()
        assertEquals("Expected exactly one profile row for the new user", 1, rows.size)
        assertEquals("Profile should hold the name given at sign-up", NAME, rows[0].displayName)
    }

    @Test
    fun t03_signOut_thenSignIn() = runBlocking<Unit> {
        auth.signOut()
        assertFalse("Should be logged out", auth.isLoggedIn())
        auth.signIn(TEST_EMAIL, PASSWORD)
        assertTrue("Should be logged in again", auth.isLoggedIn())
    }

    @Test
    fun t04_wrongPassword_isRejected() = runBlocking<Unit> {
        auth.signOut()
        assertFails { auth.signIn(TEST_EMAIL, "definitely-wrong") }
        assertFalse(auth.isLoggedIn())
        auth.signIn(TEST_EMAIL, PASSWORD)   // log back in for the remaining tests
    }

    // ---------- Recipes and matching ----------

    @Test
    fun t05_recipes_areReadable() = runBlocking<Unit> {
        val recipes = client.from("recipes")
            .select(Columns.list("id", "title", "cook_time_minutes", "difficulty"))
            .decodeList<RecipeRow>()
        assertTrue("No recipes found. Run the sample-data SQL script.", recipes.isNotEmpty())
    }

    @Test
    fun t06_matchRecipes_returnsRankedResults() = runBlocking<Unit> {
        val ids = ingredientIds("Eggs", "Potatoes")
        assertEquals("Eggs and Potatoes missing. Run the sample-data SQL script.", 2, ids.size)

        val matches = client.postgrest
            .rpc("match_recipes", MatchParams(ids))
            .decodeList<MatchRow>()

        assertTrue("match_recipes returned nothing", matches.isNotEmpty())
        val percents = matches.map { it.matchPercent }
        assertEquals("Results must be sorted by match_percent, highest first", percents.sortedDescending(), percents)
        matches.firstOrNull { it.title == "Egg and Potato Curry" }?.let {
            assertEquals("2 of the curry's 6 ingredients selected = 33%", 33, it.matchPercent)
        }
    }

    // ---------- Pantry and favorites ----------

    @Test
    fun t07_pantry_addReadRemove() = runBlocking<Unit> {
        val onionId = ingredientIds("Onions").single()
        client.from("pantry_items").insert(PantryInsert(onionId, "2 pcs"))
        try {
            val rows = client.from("pantry_items")
                .select(Columns.raw("quantity, ingredients(name, category)"))
                .decodeList<PantryRow>()
            assertEquals(1, rows.size)
            assertEquals("Onions", rows[0].ingredients.name)
            assertEquals("vegetable", rows[0].ingredients.category)
        } finally {
            client.from("pantry_items").delete { filter { eq("ingredient_id", onionId) } }
        }
        val after = client.from("pantry_items").select(Columns.list("ingredient_id")).decodeList<IdOnlyPantry>()
        assertTrue("Pantry should be empty after removing the item", after.isEmpty())
    }

    @Test
    fun t08_favorites_addReadRemove() = runBlocking<Unit> {
        val recipeId = client.from("recipes").select(Columns.list("id")).decodeList<IdRow>().first().id
        client.from("favorites").insert(FavoriteInsert(recipeId))
        try {
            val rows = client.from("favorites")
                .select(Columns.raw("recipe_id, recipes(title, cook_time_minutes)"))
                .decodeList<FavoriteRow>()
            assertEquals(1, rows.size)
            assertEquals(recipeId, rows[0].recipeId)
            assertTrue(rows[0].recipes.title.isNotBlank())
        } finally {
            client.from("favorites").delete { filter { eq("recipe_id", recipeId) } }
        }
    }

    // ---------- Security ----------

    @Test
    fun t09_loggedOutUser_cannotSeeRecipes() = runBlocking<Unit> {
        auth.signOut()
        val rows = try {
            client.from("recipes").select(Columns.list("id")).decodeList<IdRow>()
        } catch (e: Exception) {
            emptyList()   // an error is also an acceptable way to be blocked
        }
        assertTrue("Logged-out users must not see recipes", rows.isEmpty())
    }

    // ---------- Helpers ----------

    private suspend fun ingredientIds(vararg names: String): List<Long> =
        client.from("ingredients").select(Columns.list("id", "name", "category"))
            .decodeList<IngredientRow>()
            .filter { it.name in names }
            .map { it.id }

    private suspend fun assertFails(block: suspend () -> Unit) {
        var failed = false
        try { block() } catch (e: Exception) { failed = true }
        assertTrue("Expected the call to fail, but it succeeded", failed)
    }
}

// ---------- Data shapes (these move into the app's data/model package later) ----------

@Serializable private data class ProfileRow(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
)
@Serializable private data class IngredientRow(val id: Long, val name: String, val category: String)
@Serializable private data class RecipeRow(
    val id: Long,
    val title: String,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int? = null,
    val difficulty: String? = null,
)
@Serializable private data class IdRow(val id: Long)
@Serializable private data class IdOnlyPantry(@SerialName("ingredient_id") val ingredientId: Long)
@Serializable private data class MatchParams(@SerialName("selected_ids") val selectedIds: List<Long>)
@Serializable private data class MatchRow(
    val id: Long,
    val title: String,
    @SerialName("match_percent") val matchPercent: Int,
)
@Serializable private data class PantryInsert(
    @SerialName("ingredient_id") val ingredientId: Long,
    val quantity: String? = null,
)
@Serializable private data class IngredientBrief(val name: String, val category: String)
@Serializable private data class PantryRow(val quantity: String? = null, val ingredients: IngredientBrief)
@Serializable private data class FavoriteInsert(@SerialName("recipe_id") val recipeId: Long)
@Serializable private data class RecipeBrief(
    val title: String,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int? = null,
)
@Serializable private data class FavoriteRow(
    @SerialName("recipe_id") val recipeId: Long,
    val recipes: RecipeBrief,
)