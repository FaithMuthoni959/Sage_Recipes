package com.example.sagerecipes.data

import androidx.compose.runtime.mutableStateOf
import com.example.sagerecipes.data.repository.FavoritesRepository
import com.example.sagerecipes.ui.screens.recommendations.RecipeRecommendation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FavoritesManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val repo = FavoritesRepository()

    private var _favorites = mutableStateOf<List<RecipeRecommendation>>(
        listOf(
            RecipeRecommendation("1", "Egg and Potato Curry", "A rich, flavorful Kenyan-style curry.", "98% Match", "40 mins", "Medium", isFavorite = true),
            RecipeRecommendation("2", "Bhajias", "Crispy battered potatoes with herbs and spices.", "70% Match", "35 mins", "Easy", isFavorite = true)
        )
    )

    val favorites: List<RecipeRecommendation>
        get() = _favorites.value

    val favoriteRecipeIds: Set<String>
        get() = _favorites.value.map { it.id }.toSet()

    fun isFavorite(recipeId: String): Boolean = favoriteRecipeIds.contains(recipeId)

    fun toggleFavorite(recipe: RecipeRecommendation) {
        val current = _favorites.value
        val exists = current.any { it.id == recipe.id }
        _favorites.value = if (exists) {
            current.filterNot { it.id == recipe.id }
        } else {
            current + recipe.copy(isFavorite = true)
        }
        val numericId = recipe.id.toLongOrNull()
        if (numericId != null) {
            scope.launch {
                try {
                    repo.toggle(numericId)
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleFavoriteById(recipeId: String, title: String = "", description: String = "", time: String = "30 mins", difficulty: String = "Medium") {
        val recipe = _favorites.value.find { it.id == recipeId }
            ?: RecipeRecommendation(
                id = recipeId,
                title = title.ifBlank { "Recipe #$recipeId" },
                description = description.ifBlank { "A delicious recipe in your favorites." },
                matchPercentage = "Favorite",
                time = time,
                difficulty = difficulty,
                isFavorite = true
            )
        toggleFavorite(recipe)
    }

    fun syncFromRemote(remoteList: List<RecipeRecommendation>) {
        val existingIds = remoteList.map { it.id }.toSet()
        val localOnly = _favorites.value.filter { it.id !in existingIds }
        _favorites.value = remoteList + localOnly
    }
}
