package com.example.sagerecipes.ui.screens.recommendations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
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
import com.example.sagerecipes.data.FavoritesManager
import com.example.sagerecipes.data.repository.FavoritesRepository
import com.example.sagerecipes.ml.RecipeRecommenderEngine
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*
import kotlinx.coroutines.launch

data class RecipeRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val matchPercentage: String,
    val time: String,
    val difficulty: String,
    val matchedIngredients: List<String> = emptyList(),
    val missingIngredients: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

@Composable
fun RecommendationsScreen(
    selectedIngredients: List<String>,
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val favoritesRepo = remember { FavoritesRepository() }

    var searchQuery by remember { mutableStateOf("") }
    var allRecommendations by remember { mutableStateOf<List<RecipeRecommendation>>(emptyList()) }
    var favoriteIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedIngredients) {
        isLoading = true
        try {
            favoriteIds = favoritesRepo.getFavoriteIds()
        } catch (_: Exception) {}

        val activeIngredients = selectedIngredients.ifEmpty {
            listOf("Potatoes", "Onions", "Eggs")
        }
        val scored = RecipeRecommenderEngine.recommend(
            context = context,
            selectedIngredients = activeIngredients,
            topK = 20
        )

        allRecommendations = scored.map { item ->
            val r = item.recipe
            val totalTime = listOf(r.prepTime, r.cookTime)
                .filter { it.isNotBlank() }
                .joinToString(" + ")
                .ifBlank { "30 mins" }

            val difficultyLabel = when {
                r.ingredients.size <= 6 -> "Easy"
                r.ingredients.size <= 11 -> "Medium"
                else -> "Hard"
            }

            val summaryText = buildString {
                if (r.course.isNotBlank() || r.category.isNotBlank()) {
                    append(listOf(r.course, r.category).filter { it.isNotBlank() }.joinToString(" • "))
                    append(" — ")
                }
                append("Matched ${item.matchedIngredients.size}/${r.ingredients.size} ingredients")
            }

            val numId: Long = r.id.toLong()
            val isFav = favoriteIds.contains(numId) || FavoritesManager.favoriteRecipeIds.contains(numId.toString())

            RecipeRecommendation(
                id = r.id.toString(),
                title = r.title,
                description = summaryText,
                matchPercentage = "${item.matchPercent}% Match",
                time = totalTime,
                difficulty = difficultyLabel,
                isFavorite = isFav
            )
        }
        isLoading = false
    }

    val recommendations = remember(allRecommendations, searchQuery, favoriteIds) {
        val list = allRecommendations.map { r ->
            val numId = r.id.toLongOrNull() ?: -1L
            val isFav = numId in favoriteIds || r.id in FavoritesManager.favoriteRecipeIds
            r.copy(isFavorite = isFav)
        }
        if (searchQuery.isBlank()) {
            list
        } else {
            list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5F0))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // 1. Top Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ButtonGreen)
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
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
                            Text(
                                text = "Food recommendations",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search recipes...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                shape = RoundedCornerShape(Radius.input),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    disabledContainerColor = Color.White,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            )
                        }
                    }
                }
            }

            // 2. Subtitle
            item {
                val ingredientsText = if (selectedIngredients.isNotEmpty()) {
                    selectedIngredients.joinToString(" • ")
                } else {
                    "Potatoes, Onions, Eggs"
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                ) {
                    Text(
                        text = "Based on your:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = ButtonGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ingredientsText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // 3. Loading indicator or List
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ButtonGreen)
                    }
                }
            } else {
                items(recommendations) { recipe ->
                    Card(
                        shape = RoundedCornerShape(Radius.card),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                            .clickable { onRecipeClick(recipe.id) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.lg)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = ButtonGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = recipe.matchPercentage,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ButtonGreen,
                                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    FavoritesManager.toggleFavorite(recipe)
                                    val numId = recipe.id.toLongOrNull()
                                    if (numId != null) {
                                        favoriteIds = if (FavoritesManager.isFavorite(recipe.id)) {
                                            favoriteIds + numId
                                        } else {
                                            favoriteIds - numId
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (recipe.isFavorite) Color.Red else TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(Spacing.md))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                Text(text = "⏱ ${recipe.time}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(text = "⚡ ${recipe.difficulty}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
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

@Preview(showBackground = true)
@Composable
fun RecommendationsScreenPreview() {
    SageRecipesTheme {
        RecommendationsScreen(
            selectedIngredients = listOf("Potatoes", "Onions", "Eggs"),
            onRecipeClick = {},
            onBackClick = {}
        )
    }
}
