package com.example.sagerecipes.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.data.FavoritesManager
import com.example.sagerecipes.ml.RecipeRecommenderEngine
import com.example.sagerecipes.ui.theme.*
import java.util.Locale

data class IngredientItem(val name: String, val note: String)
data class InstructionStep(val stepNumber: String, val title: String, val detail: String)
data class RecipeDetail(
    val title: String,
    val description: String,
    val time: String,
    val heat: String,
    val servings: String,
    val ingredients: List<IngredientItem>,
    val instructions: List<InstructionStep>
)

fun getRecipeDetails(recipeId: String): RecipeDetail {
    // 1. Check the loaded ML dataset first
    val engineRecipe = RecipeRecommenderEngine.getRecipeById(recipeId)
    if (engineRecipe != null) {
        val totalTime = listOf(engineRecipe.prepTime, engineRecipe.cookTime)
            .filter { it.isNotBlank() }
            .joinToString(" + ")
            .ifBlank { "30 mins" }

        val desc = listOf(engineRecipe.course, engineRecipe.category)
            .filter { it.isNotBlank() }
            .joinToString(" • ")
            .ifBlank { "Recommended recipe tailored to your selected ingredients." }

        val mappedIngredients = engineRecipe.ingredients.map { rawIng ->
            val formattedName = rawIng.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            IngredientItem(name = formattedName, note = "Ingredient")
        }

        val mappedInstructions = if (engineRecipe.instructions.isNotEmpty()) {
            engineRecipe.instructions.mapIndexed { idx, stepText ->
                InstructionStep(
                    stepNumber = String.format(Locale.getDefault(), "%02d", idx + 1),
                    title = "Step ${idx + 1}",
                    detail = stepText
                )
            }
        } else {
            listOf(
                InstructionStep(
                    stepNumber = "01",
                    title = "Prepare & Cook",
                    detail = "Prepare all ${engineRecipe.ingredients.size} ingredients and cook according to standard ${engineRecipe.course.ifBlank { "dish" }} preparation."
                )
            )
        }

        return RecipeDetail(
            title = engineRecipe.title,
            description = desc,
            time = totalTime,
            heat = "Medium Heat",
            servings = "4 Servings",
            ingredients = mappedIngredients,
            instructions = mappedInstructions
        )
    }

    // 2. Fallback recipes (used in Preview or if ID is from static list)
    return when (recipeId) {
        "2" -> RecipeDetail(
            title = "Kenyan Bhajias",
            description = "Crispy, golden-brown potato slices coated in a spiced gram flour batter, served hot with chutney.",
            time = "35 mins",
            heat = "Medium Heat",
            servings = "3 Servings",
            ingredients = listOf(
                IngredientItem("4 Large Potatoes", "Sliced Thinly"),
                IngredientItem("1.5 Cups Gram Flour", "Besan"),
                IngredientItem("1 tsp Turmeric Powder", "Spice"),
                IngredientItem("1 tsp Cumin Powder", "Spice"),
                IngredientItem("1/2 tsp Garlic & Ginger Paste", "Aromatic"),
                IngredientItem("Fresh Coriander", "Chopped"),
                IngredientItem("Salt & Chili", "To Taste"),
                IngredientItem("Vegetable Oil", "For Frying")
            ),
            instructions = listOf(
                InstructionStep("01", "Slice and Prep", "Peel and slice potatoes into thin, uniform round slices. Wash and pat dry thoroughly."),
                InstructionStep("02", "Make the Batter", "In a bowl, combine gram flour, turmeric, cumin, garlic & ginger paste, chopped coriander, salt, and water to form a thick, smooth batter."),
                InstructionStep("03", "Coat Potatoes", "Dip the potato slices into the spiced batter, ensuring each slice is evenly coated."),
                InstructionStep("04", "Fry Until Golden", "Heat vegetable oil in a deep pan. Fry coated potato slices in batches until golden brown and crispy. Serve hot.")
            )
        )
        "3" -> RecipeDetail(
            title = "Spicy Egg Stew",
            description = "A hearty tomato and onion based stew with perfectly boiled eggs simmered in rich spices.",
            time = "25 mins",
            heat = "Medium Heat",
            servings = "4 Servings",
            ingredients = listOf(
                IngredientItem("4 Hard-boiled Eggs", "Peeled"),
                IngredientItem("3 Tomatoes", "Blended / Pureed"),
                IngredientItem("2 Red Onions", "Chopped"),
                IngredientItem("Garlic & Ginger", "Minced"),
                IngredientItem("Curry Powder & Paprika", "1 tsp each")
            ),
            instructions = listOf(
                InstructionStep("01", "Sauté Onions", "Fry chopped onions in oil until golden. Add minced garlic and ginger."),
                InstructionStep("02", "Make Tomato Base", "Pour in blended tomatoes, curry powder, paprika, and salt. Simmer until oil separates."),
                InstructionStep("03", "Add Eggs", "Make shallow slits on the hard-boiled eggs and gently place them into the simmering tomato stew."),
                InstructionStep("04", "Simmer & Garnish", "Cover and simmer for 5 minutes. Garnish with fresh coriander and serve.")
            )
        )
        else -> RecipeDetail(
            title = "Kenyan Spiced Potato & Egg Curry",
            description = "A flavorful and hearty Kenyan staple featuring tender potatoes and boiled eggs in a rich, aromatic spice blend.",
            time = "45 min",
            heat = "High Heat",
            servings = "4 Servings",
            ingredients = listOf(
                IngredientItem("6 Large Hard-boiled Eggs", "Peeled"),
                IngredientItem("4 Large Potatoes", "Cubed"),
                IngredientItem("2 Red Onions", "Finely Sliced"),
                IngredientItem("Fresh Coriander & Parsley", "Finely Chopped"),
                IngredientItem("Spices & Herbs Mix", "1 tsp each")
            ),
            instructions = listOf(
                InstructionStep("01", "Sauté the Base", "Sauté the sliced red onions in a pan until golden brown. Add garlic powder, turmeric, paprika, and oregano, stirring until fragrant."),
                InstructionStep("02", "Cook the Potatoes", "Add the cubed potatoes to the pan with a splash of water. Cover and simmer until the potatoes are tender and the sauce has thickened."),
                InstructionStep("03", "Incorporate Eggs", "Gently add the peeled, hard-boiled eggs to the mixture. Stir carefully to coat the eggs in the aromatic spice blend without breaking them."),
                InstructionStep("04", "Garnish & Serve", "Toss in the fresh coriander and parsley. Let it sit for a minute to allow the flavors to meld, then serve hot with chapati or rice.")
            )
        )
    }
}

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var recipe by remember(recipeId) { mutableStateOf(getRecipeDetails(recipeId)) }

    // Ensure engine is initialized even if user opened this screen directly (e.g. from Favorites)
    LaunchedEffect(recipeId) {
        RecipeRecommenderEngine.initialize(context)
        recipe = getRecipeDetails(recipeId)
    }

    val scrollState = rememberScrollState()
    val isFavorite = FavoritesManager.favoriteRecipeIds.contains(recipeId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ButtonGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 40.dp)
        ) {
            // 1. Top Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    IconButton(
                        onClick = { FavoritesManager.toggleFavorite(recipeId) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // 2. Recipe Title & Overview Card
            Surface(
                shape = RoundedCornerShape(topStart = Radius.card, topEnd = Radius.card),
                color = Color(0xFFFDFBF7),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    // Title & Description
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            lineHeight = 34.sp
                        )
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    // Badges Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        RecipeBadge(label = recipe.time)
                        RecipeBadge(label = recipe.heat)
                        RecipeBadge(label = recipe.servings)
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    // Ingredients Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ingredients",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${recipe.ingredients.size} ITEMS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Ingredients List
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        recipe.ingredients.forEach { ingredient ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(ButtonGreen, RoundedCornerShape(50.dp))
                                    ) { }
                                    Text(
                                        text = ingredient.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = ingredient.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    // Instructions Section Header
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Instructions List
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        recipe.instructions.forEach { step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = ButtonGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = step.stepNumber,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ButtonGreen
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = step.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = step.detail,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun RecipeBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(Radius.card),
        color = Color(0xFFF0EFE6),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    SageRecipesTheme {
        RecipeDetailScreen(
            recipeId = "1",
            onBackClick = {}
        )
    }
}