package com.example.sagerecipes.ui.screens.category

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.sagerecipes.ui.screens.recommendations.RecipeRecommendation
import com.example.sagerecipes.ui.theme.SageRecipesTheme

@Composable
fun CategoryRecipesScreen(
    categoryName: String,
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    val fallback = listOf(
        RecipeRecommendation("1", "Egg and Potato Curry", "A rich, flavorful Kenyan-style curry.", "98% Match", "40 mins", "Medium"),
        RecipeRecommendation("2", "Bhajias", "Crispy battered potatoes with herbs and spices.", "70% Match", "35 mins", "Easy"),
        RecipeRecommendation("3", "Spicy Egg Stew", "A hearty tomato and onion based stew.", "80% Match", "25 mins", "Easy"),
        RecipeRecommendation("4", "Kenyan Beef Pilau", "Authentic Swahili spiced rice cooked with tender beef chunks.", "95% Match", "45 mins", "Medium")
    )

    CategoryTemplateScreen(
        title = "$categoryName Recipes",
        tagFilter = categoryName.lowercase(),
        fallbackRecipes = fallback,
        onRecipeClick = onRecipeClick,
        onBackClick = onBackClick,
        selectedIndex = selectedIndex,
        onBottomNavSelected = onBottomNavSelected
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryRecipesScreenPreview() {
    SageRecipesTheme {
        CategoryRecipesScreen(
            categoryName = "Vegan",
            onRecipeClick = {},
            onBackClick = {}
        )
    }
}
