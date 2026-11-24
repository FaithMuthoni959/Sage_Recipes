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

    fun hasMainIngredients(): Boolean {
        return selectedIngredients.any { it !in SPICES_AND_HERBS }
    }

    companion object {
        val SPICES_AND_HERBS = setOf(
            // Spices & Aromatics
            "Paprika", "Black pepper", "Cumin", "Turmeric",
            "Garlic Powder", "Ginger", "Cinnamon", "Cloves",
            "Garam masala", "Pilau masala", "Curry Powder",
            // Herbs
            "Oregano", "Basil", "Sage", "Coriander",
            "Parsley", "Green Garlic", "Thyme", "Rosemary",
            "Mint", "Chives", "Cilantro",
        )
    }
}