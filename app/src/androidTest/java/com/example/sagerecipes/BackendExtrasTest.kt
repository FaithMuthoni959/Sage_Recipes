package com.example.sagerecipes

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sagerecipes.data.error.ErrorKind
import com.example.sagerecipes.data.error.backendCall
import com.example.sagerecipes.data.error.errorKind
import com.example.sagerecipes.data.model.Ingredient
import com.example.sagerecipes.data.model.RecommendationSource
import com.example.sagerecipes.data.remote.RecommendationService
import com.example.sagerecipes.data.remote.ScoredRecipeId
import com.example.sagerecipes.data.repository.IngredientRepository
import com.example.sagerecipes.data.repository.ProfileRepository
import com.example.sagerecipes.data.repository.RecipeRepository
import com.example.sagerecipes.data.repository.RecommendationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

/** Paging, profile, lookup by external id, AI-then-SQL fallback and error mapping, against live Supabase. */
@RunWith(AndroidJUnit4::class)
class BackendExtrasTest {

    private val ingredients = IngredientRepository()
    private val recipes = RecipeRepository()
    private val profiles = ProfileRepository()

    companion object {
        @BeforeClass
        @JvmStatic
        fun logIn() = runBlocking<Unit> { TestSupport.signInFreshUser() }
    }

    private suspend fun eggsAndPotatoes(): List<Ingredient> =
        ingredients.getAll().filter { it.name == "Eggs" || it.name == "Potatoes" }

    // ---------- Paging ----------

    @Test
    fun recipes_paging_pagesDoNotOverlap() = runBlocking<Unit> {
        val first = recipes.getAll(limit = 2, offset = 0)
        val second = recipes.getAll(limit = 2, offset = 2)
        assertTrue(first.size <= 2 && second.size <= 2)
        assertTrue(
            "Pages must not share recipes",
            first.map { it.id }.toSet().intersect(second.map { it.id }.toSet()).isEmpty(),
        )
        assertTrue("Past the end gives an empty page", recipes.getAll(limit = 5, offset = 100_000).isEmpty())
        assertTrue("Limit is capped at 100", recipes.getAll(limit = 1_000).size <= 100)
    }

    // ---------- Profile ----------

    @Test
    fun profile_readAndUpdateName() = runBlocking<Unit> {
        val before = profiles.getMyProfile()
        assertNotNull(before)
        assertEquals(TestSupport.DISPLAY_NAME, before!!.displayName)
        assertTrue(before.email!!.contains("+sagetest"))
        assertEquals(TestSupport.DISPLAY_NAME.substringBefore(' '), before.firstName)

        profiles.updateDisplayName("  Sarah Test  ")
        val after = profiles.getMyProfile()!!
        assertEquals("Sarah Test", after.displayName)
        assertEquals("Sarah", after.firstName)

        try {
            profiles.updateDisplayName("   ")
            fail("An empty name must be rejected")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    // ---------- Lookup by external id ----------

    @Test
    fun recipes_byExternalIds_matchesAndIgnoresUnknown() = runBlocking<Unit> {
        val found = recipes.getRecipesByExternalIds(listOf("sample-1", "does-not-exist"))
        assertEquals(setOf("sample-1"), found.keys)
        assertEquals("Egg and Potato Curry", found.getValue("sample-1").title)
        assertTrue(recipes.getRecipesByExternalIds(emptyList()).isEmpty())
    }

    // ---------- AI first, SQL as fallback ----------

    @Test
    fun recommend_noService_usesSql() = runBlocking<Unit> {
        val result = RecommendationRepository(recipes = recipes, ai = null).recommend(eggsAndPotatoes())
        assertEquals(RecommendationSource.SQL, result.source)
        assertTrue(result.recipes.any { it.title == "Egg and Potato Curry" })
    }

    @Test
    fun recommend_aiResult_usesAiOrderAndScore() = runBlocking<Unit> {
        val fake = FakeService { listOf(ScoredRecipeId("nope", 0.99), ScoredRecipeId("sample-1", 0.87)) }
        val result = RecommendationRepository(recipes = recipes, ai = fake).recommend(eggsAndPotatoes())

        assertEquals(RecommendationSource.AI, result.source)
        assertEquals("Unknown ids are dropped", 1, result.recipes.size)
        assertEquals("Egg and Potato Curry", result.recipes.first().title)
        assertEquals(87, result.recipes.first().matchPercent)
        assertEquals(setOf("Eggs", "Potatoes"), fake.lastIngredients!!.toSet())
    }

    @Test
    fun recommend_aiThrows_fallsBackToSql() = runBlocking<Unit> {
        val fake = FakeService { throw java.net.ConnectException("service is down") }
        val result = RecommendationRepository(recipes = recipes, ai = fake).recommend(eggsAndPotatoes())
        assertEquals(RecommendationSource.SQL, result.source)
        assertTrue(result.recipes.isNotEmpty())
    }

    @Test
    fun recommend_aiUnknownIds_fallsBackToSql() = runBlocking<Unit> {
        val fake = FakeService { listOf(ScoredRecipeId("nope", 0.9)) }
        val result = RecommendationRepository(recipes = recipes, ai = fake).recommend(eggsAndPotatoes())
        assertEquals(RecommendationSource.SQL, result.source)
        assertTrue(result.recipes.isNotEmpty())
    }

    @Test
    fun recommend_aiTooSlow_fallsBackToSql() = runBlocking<Unit> {
        val fake = FakeService {
            delay(10_000)
            emptyList()
        }
        val started = System.currentTimeMillis()
        val result = RecommendationRepository(recipes = recipes, ai = fake, aiTimeoutMs = 500)
            .recommend(eggsAndPotatoes())
        val elapsed = System.currentTimeMillis() - started

        assertEquals(RecommendationSource.SQL, result.source)
        assertTrue(result.recipes.isNotEmpty())
        assertTrue("Fallback must not wait for the slow service (took $elapsed ms)", elapsed < 6_000)
    }

    @Test
    fun recommend_emptySelection_doesNotCallService() = runBlocking<Unit> {
        val fake = FakeService { listOf(ScoredRecipeId("sample-1", 1.0)) }
        val result = RecommendationRepository(recipes = recipes, ai = fake).recommend(emptyList())
        assertTrue(result.recipes.isEmpty())
        assertEquals(RecommendationSource.SQL, result.source)
        assertEquals(0, fake.calls)
    }

    // ---------- Errors ----------

    @Test
    fun errors_missingRecipe_isNotFound() = runBlocking<Unit> {
        val result = backendCall { recipes.getRecipe(-1) }
        assertTrue(result.isFailure)
        assertEquals(ErrorKind.NOT_FOUND, result.exceptionOrNull()!!.errorKind)
    }
}

/** A stand-in for the AI service so the fallback logic can be tested without one. */
private class FakeService(
    private val behavior: suspend () -> List<ScoredRecipeId>,
) : RecommendationService {
    var calls = 0
    var lastIngredients: List<String>? = null

    override suspend fun recommend(ingredients: List<String>, limit: Int, offset: Int): List<ScoredRecipeId> {
        calls++
        lastIngredients = ingredients
        return behavior()
    }
}