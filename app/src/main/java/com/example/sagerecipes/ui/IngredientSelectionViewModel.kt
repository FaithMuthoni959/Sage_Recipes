package com.example.sagerecipes.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class IngredientSelectionViewModel : ViewModel() {

    var selectedIngredients by mutableStateOf(setOf<String>())
        private set

    fun toggleIngredient(name: String) {
        selectedIngredients = if (name in selectedIngredients) {
            selectedIngredients - name
        } else {
            selectedIngredients + name
        }
    }
}