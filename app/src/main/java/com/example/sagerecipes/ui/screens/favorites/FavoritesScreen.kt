package com.example.sagerecipes.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sagerecipes.data.FavoritesManager
import com.example.sagerecipes.data.repository.FavoritesRepository
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.screens.recommendations.RecipeRecommendation
import com.example.sagerecipes.ui.theme.*

@Composable
fun FavoritesScreen(
    onRecipeClick: (String) -> Unit,
    onBottomNavSelected: (Int) -> Unit
) {
    val favoritesRepo = remember { FavoritesRepository() }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val remoteFavorites = favoritesRepo.getFavorites()
            if (remoteFavorites.isNotEmpty()) {
                val mapped = remoteFavorites.map { fav ->
                    val r = fav.recipe
                    val timeStr = r.cookTimeMinutes?.let { "$it mins" } ?: "30 mins"
                    val diffStr = r.difficulty?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Medium"
                    RecipeRecommendation(
                        id = r.id.toString(),
                        title = r.title,
                        description = r.description ?: "Saved favorite recipe",
                        matchPercentage = "Favorite",
                        time = timeStr,
                        difficulty = diffStr,
                        isFavorite = true
                    )
                }
                FavoritesManager.syncFromRemote(mapped)
            }
        } catch (_: Exception) {
            // keep local favorites
        } finally {
            isLoading = false
        }
    }

    val favoriteRecipes = FavoritesManager.favorites

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
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ButtonGreen)
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
                ) {
                    Text(
                        text = "Favorite Recipes",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isLoading && favoriteRecipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ButtonGreen)
                    }
                }
            } else if (favoriteRecipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No favorite recipes yet.\nTap the heart icon on recipes you like!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(favoriteRecipes) { recipe ->
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
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                IconButton(onClick = {
                                    FavoritesManager.toggleFavorite(recipe)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Favorite",
                                        tint = Color.Red
                                    )
                                }
                            }
                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
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
                selectedIndex = 1,
                onTabSelected = onBottomNavSelected
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    SageRecipesTheme {
        FavoritesScreen(onRecipeClick = {}, onBottomNavSelected = {})
    }
}
