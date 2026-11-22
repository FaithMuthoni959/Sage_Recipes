package com.example.sagerecipes

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sagerecipes.data.repository.FavoritesRepository
import com.example.sagerecipes.data.repository.IngredientRepository
import com.example.sagerecipes.data.repository.PantryRepository
import com.example.sagerecipes.data.repository.RecipeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

/** Checks the favorites, cooking-history and pantry repositories as a freshly signed-up user. */
@RunWith(AndroidJUnit4::class)
class FavoritesPantryTest {

    private val ingredients = IngredientRepository()
    private val recipes = RecipeRepository()
    private val pantry = PantryRepository()
    private val favorites = FavoritesRepository()

    companion object {
        @BeforeClass
        @JvmStatic
        fun logIn() = runBlocking<Unit> { TestSupport.signInFreshUser() }
    }

    private suspend fun ingredientId(name: String): Long =
        ingredients.getAll().first { it.name == name }.id

    // ---------- Pantry ----------

    @Test
    fun pantry_addUpdateRemove() = runBlocking<Unit> {
        val onions = ingredientId("Onions")
        try {
            pantry.addOrUpdate(onions, "2 pcs")
            var item = pantry.getPantry().first { it.ingredient.id == onions }
            assertEquals("2 pcs", item.quantity)
            assertEquals("Onions", item.ingredient.name)

            pantry.addOrUpdate(onions, "5 pcs")
            item = pantry.getPantry().first { it.ingredient.id == onions }
            assertEquals("Quantity should be updated", "5 pcs", item.quantity)
            assertEquals("Adding twice must not duplicate", 1, pantry.getPantry().count { it.ingredient.id == onions })

            pantry.addOrUpdate(onions)   // no quantity: leave as is
            assertEquals("5 pcs", pantry.getPantry().first { it.ingredient.id == onions }.quantity)
        } finally {
            pantry.remove(onions)
        }
        assertTrue("Item should be gone", pantry.getPantry().none { it.ingredient.id == onions })
    }

    @Test
    fun pantry_addAll_skipsExistingAndDuplicates() = runBlocking<Unit> {
        val eggs = ingredientId("Eggs")
        val potatoes = ingredientId("Potatoes")
        try {
            pantry.addOrUpdate(eggs, "6")
            pantry.addAll(listOf(eggs, potatoes, potatoes))
            val ids = pantry.getPantryIngredientIds()
            assertEquals(1, ids.count { it == eggs })
            assertEquals(1, ids.count { it == potatoes })
            assertEquals("Existing quantity must be kept", "6", pantry.getPantry().first { it.ingredient.id == eggs }.quantity)
        } finally {
            pantry.remove(eggs)
            pantry.remove(potatoes)
        }
    }

    // ---------- Favorites ----------

    @Test
    fun favorites_addToggleRemove() = runBlocking<Unit> {
        val id = recipes.getAll().first().id
        favorites.remove(id)
        assertFalse(favorites.isFavorite(id))

        favorites.add(id)
        favorites.add(id)   // adding twice must not fail
        assertTrue(favorites.isFavorite(id))
        assertTrue(id in favorites.getFavoriteIds())

        assertFalse("toggle should switch it off", favorites.toggle(id))
        assertFalse(favorites.isFavorite(id))
        assertTrue("toggle should switch it back on", favorites.toggle(id))

        favorites.remove(id)
        assertTrue(id !in favorites.getFavoriteIds())
    }

    @Test
    fun favorites_listShowsRecipeAndLastMade() = runBlocking<Unit> {
        val recipe = recipes.getAll().first()
        favorites.add(recipe.id)
        try {
            val before = favorites.getFavorites().first { it.recipe.id == recipe.id }
            assertEquals(recipe.title, before.recipe.title)
            assertNull("Not cooked yet", before.lastMadeAt)

            favorites.markAsMade(recipe.id)

            val after = favorites.getFavorites().first { it.recipe.id == recipe.id }
            assertNotNull("Should now have a last-made time", after.lastMadeAt)

            val recent = favorites.getRecentlyMade()
            assertEquals("Most recent first", recipe.id, recent.first().recipe.id)
            assertEquals("One entry per recipe", 1, recent.count { it.recipe.id == recipe.id })
        } finally {
            favorites.remove(recipe.id)
        }
    }
}