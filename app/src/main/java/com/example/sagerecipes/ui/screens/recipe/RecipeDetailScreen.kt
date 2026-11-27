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
import com.example.sagerecipes.data.model.Difficulty
import com.example.sagerecipes.data.model.RecipeDetail as SupabaseRecipeDetail
import com.example.sagerecipes.data.model.RecipeIngredientLine
import com.example.sagerecipes.data.model.RecipeStep
import com.example.sagerecipes.data.repository.RecipeRepository
import com.example.sagerecipes.ml.RecipeRecommenderEngine
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*
import java.util.Locale

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBackClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val recipeRepo = remember { RecipeRepository() }
    var recipe by remember { mutableStateOf<SupabaseRecipeDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(recipeId) {
        isLoading = true
        errorMessage = null
        RecipeRecommenderEngine.initialize(context)

        val numericId = recipeId.toLongOrNull()
        if (numericId != null) {
            try {
                recipe = recipeRepo.getRecipe(numericId)
            } catch (_: Exception) {
                recipe = createFallbackRecipeDetail(recipeId)
            }
        } else {
            recipe = createFallbackRecipeDetail(recipeId)
        }
        isLoading = false
    }

    val scrollState = rememberScrollState()
    val isFavorite = FavoritesManager.isFavorite(recipeId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ButtonGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
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
                        onClick = {
                            val current = recipe
                            if (current != null) {
                                FavoritesManager.toggleFavoriteById(
                                    recipeId = recipeId,
                                    title = current.title,
                                    description = current.description ?: "Saved recipe",
                                    time = current.cookTimeMinutes?.let { "$it mins" } ?: "30 mins",
                                    difficulty = current.difficulty?.name ?: "Medium"
                                )
                            } else {
                                FavoritesManager.toggleFavoriteById(recipeId)
                            }
                        },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 600.dp)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ButtonGreen)
                        }
                    }
                    errorMessage != null || recipe == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.xl),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Recipe details unavailable.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        val current = recipe!!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                        ) {
                            // Title & Description
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Text(
                                    text = current.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    lineHeight = 34.sp
                                )
                                if (!current.description.isNullOrBlank()) {
                                    Text(
                                        text = current.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Badges Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                RecipeBadge(label = "${current.cookTimeMinutes ?: 30} mins")
                                RecipeBadge(label = current.heatLevel ?: "Medium Heat")
                                RecipeBadge(label = "${current.servings ?: 4} Servings")
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
                                    text = "${current.ingredients.size} ITEMS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Ingredients List
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                current.ingredients.forEach { ingredient ->
                                    val displayName = ingredient.name.replaceFirstChar { it.uppercase() }
                                    val noteText = listOfNotNull(ingredient.quantity, ingredient.prepNote)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" • ")

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
                                            )
                                            Text(
                                                text = displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary
                                            )
                                        }
                                        if (noteText.isNotEmpty()) {
                                            Text(
                                                text = noteText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
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
                                current.steps.forEach { step ->
                                    val stepNumberText = remember(step.number) {
                                        String.format(Locale.getDefault(), "%02d", step.number)
                                    }
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
                                                    text = stepNumberText,
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
                                                text = step.body,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(120.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            SageBottomBar(
                selectedIndex = selectedIndex,
                onTabSelected = onBottomNavSelected
            )
        }
    }
}

fun createFallbackRecipeDetail(recipeId: String): SupabaseRecipeDetail {
    val contextEngineRecipe = RecipeRecommenderEngine.getRecipeById(recipeId)
    val numId = recipeId.toLongOrNull() ?: 1L
    if (contextEngineRecipe != null) {
        val totalMins = contextEngineRecipe.cookTime.filter { it.isDigit() }.toIntOrNull() ?: 30
        return SupabaseRecipeDetail(
            id = numId,
            title = contextEngineRecipe.title,
            description = listOf(contextEngineRecipe.course, contextEngineRecipe.category)
                .filter { it.isNotBlank() }
                .joinToString(" • ")
                .ifBlank { "Delicious home recipe" },
            imageUrl = null,
            cookTimeMinutes = totalMins,
            difficulty = if (contextEngineRecipe.ingredients.size <= 6) Difficulty.Easy else Difficulty.Medium,
            heatLevel = "Medium Heat",
            servings = 4,
            avgRating = 4.8,
            tags = listOf(contextEngineRecipe.category, contextEngineRecipe.course),
            ingredients = contextEngineRecipe.ingredients.map { ingName ->
                RecipeIngredientLine(ingredientId = 0, name = ingName, quantity = "1 unit", prepNote = null)
            },
            steps = if (contextEngineRecipe.instructions.isNotEmpty()) {
                contextEngineRecipe.instructions.mapIndexed { idx, body ->
                    RecipeStep(number = idx + 1, title = "Step ${idx + 1}", body = body)
                }
            } else {
                listOf(
                    RecipeStep(number = 1, title = "Prepare", body = "Prepare all ingredients and cook thoroughly."),
                    RecipeStep(number = 2, title = "Serve", body = "Serve hot and enjoy!")
                )
            }
        )
    }

    val fav = FavoritesManager.favorites.find { it.id == recipeId }
    val title = fav?.title ?: when (recipeId) {
        "2" -> "Kenyan Bhajias"
        "3" -> "Spicy Egg Stew"
        "4" -> "Kenyan Beef Pilau"
        else -> "Egg and Potato Curry"
    }
    val desc = fav?.description ?: "A rich, aromatic Kenyan dish made with fresh ingredients and spices."

    return SupabaseRecipeDetail(
        id = numId,
        title = title,
        description = desc,
        imageUrl = null,
        cookTimeMinutes = 35,
        difficulty = Difficulty.Medium,
        heatLevel = "Medium Heat",
        servings = 4,
        avgRating = 4.7,
        tags = listOf("Kenyan", "Curry"),
        ingredients = listOf(
            RecipeIngredientLine(0, "Potatoes", "4 large", "Sliced"),
            RecipeIngredientLine(0, "Eggs", "4 boiled", "Peeled"),
            RecipeIngredientLine(0, "Onions", "2 red", "Chopped"),
            RecipeIngredientLine(0, "Tomatoes", "3 fresh", "Pureed"),
            RecipeIngredientLine(0, "Spices", "1 tsp", "Mixed")
        ),
        steps = listOf(
            RecipeStep(1, "Sauté Base", "Sauté chopped onions and spices until golden brown and aromatic."),
            RecipeStep(2, "Add Sauce", "Pour in tomato puree and simmer until thickened."),
            RecipeStep(3, "Cook & Simmer", "Add potatoes and boiled eggs, simmer until fully tender."),
            RecipeStep(4, "Garnish & Serve", "Garnish with fresh coriander and serve hot.")
        )
    )
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
