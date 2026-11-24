package com.example.sagerecipes

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sagerecipes.data.model.IngredientCategory
import com.example.sagerecipes.data.repository.IngredientRepository
import com.example.sagerecipes.data.repository.RecipeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

/** Read-only checks of the ingredient and recipe repositories against the sample data. */
@RunWith(AndroidJUnit4::class)
class RepositoryTest {

    private val ingredients = IngredientRepository()
    private val recipes = RecipeRepository()

    companion object {
        @BeforeClass
        @JvmStatic
        fun logIn() = runBlocking<Unit> { TestSupport.signInFreshUser() }
    }

    // ---------- Ingredients ----------

    @Test
    fun ingredients_getAll_includesSampleData() = runBlocking<Unit> {
        val all = ingredients.getAll()
        assertTrue("No ingredients. Did the sample-data script run?", all.isNotEmpty())
        assertTrue(all.any { it.name == "Eggs" })
        assertEquals("Should be sorted by name", all.map { it.name }.sorted(), all.map { it.name })
    }

    @Test
    fun ingredients_byCategory_onlyReturnsThatCategory() = runBlocking<Unit> {
        val spices = ingredients.getByCategories(IngredientCategory.SPICE)
        assertTrue(spices.isNotEmpty())
        assertTrue(spices.all { it.category == IngredientCategory.SPICE })
        assertTrue(spices.any { it.name == "Turmeric" })

        val both = ingredients.getByCategories(IngredientCategory.VEGETABLE, IngredientCategory.PROTEIN)
        assertTrue(both.all { it.category == IngredientCategory.VEGETABLE || it.category == IngredientCategory.PROTEIN })
        assertTrue(both.any { it.name == "Eggs" } && both.any { it.name == "Onions" })
    }

    @Test
    fun ingredients_popular_respectsLimit() = runBlocking<Unit> {
        val popular = ingredients.getPopular(limit = 3)
        assertTrue("popular_ingredients returned nothing (did the SQL function run?)", popular.isNotEmpty())
        assertTrue(popular.size <= 3)
    }

    @Test
    fun ingredients_search_isCaseInsensitive() = runBlocking<Unit> {
        assertTrue(ingredients.search("egg").any { it.name == "Eggs" })
        assertTrue(ingredients.search("   ").isEmpty())
    }

    // ---------- Recipes ----------

    @Test
    fun recipes_recommendations_areRankedAndScored() = runBlocking<Unit> {
        val all = ingredients.getAll()
        val eggs = all.first { it.name == "Eggs" }.id
        val potatoes = all.first { it.name == "Potatoes" }.id

        val results = recipes.getRecommendations(listOf(eggs, potatoes))
        assertTrue(results.isNotEmpty())
        val curry = results.firstOrNull { it.title == "Egg and Potato Curry" }
        assertNotNull("Sample curry should match", curry)
        assertEquals(33, curry!!.matchPercent)

        assertTrue("Empty selection should return no recipes", recipes.getRecommendations(emptyList()).isEmpty())
    }

    @Test
    fun recipes_detail_hasIngredientsAndOrderedSteps() = runBlocking<Unit> {
        val id = recipes.getAll().first { it.title == "Egg and Potato Curry" }.id
        val detail = recipes.getRecipe(id)

        assertEquals("Egg and Potato Curry", detail.title)
        assertEquals(4, detail.servings)
        assertEquals("High Heat", detail.heatLevel)
        assertEquals(6, detail.ingredients.size)
        val eggs = detail.ingredients.first { it.name == "Eggs" }
        assertEquals("6 large", eggs.quantity)
        assertEquals("Peeled", eggs.prepNote)
        // Works with or without sample steps: whatever steps exist must be in order
        val numbers = detail.steps.map { it.number }
        assertEquals("Steps must be in order", numbers.sorted(), numbers)
    }

    @Test
    fun recipes_searchTagAndQuickFilters() = runBlocking<Unit> {
        assertTrue(recipes.search("curry").any { it.title == "Egg and Potato Curry" })
        assertTrue(recipes.getByTag("protein").any { it.title == "Egg and Potato Curry" })
        assertTrue(recipes.getQuick(maxMinutes = 60).any { it.title == "Egg and Potato Curry" })   // takes 40 min
        assertTrue(recipes.getQuick(maxMinutes = 30).none { it.title == "Egg and Potato Curry" })
    }
}