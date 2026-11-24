package com.example.sagerecipes.data

import androidx.compose.runtime.mutableStateOf

object FavoritesManager {
    private var _favoriteRecipeIds = mutableStateOf(setOf("1", "2"))
    val favoriteRecipeIds: Set<String>
        get() = _favoriteRecipeIds.value

    fun toggleFavorite(recipeId: String) {
        _favoriteRecipeIds.value = if (_favoriteRecipeIds.value.contains(recipeId)) {
            _favoriteRecipeIds.value - recipeId
        } else {
            _favoriteRecipeIds.value + recipeId
        }
    }
}
