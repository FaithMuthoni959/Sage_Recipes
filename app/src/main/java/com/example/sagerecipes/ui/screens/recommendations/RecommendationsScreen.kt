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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sagerecipes.data.FavoritesManager
import com.example.sagerecipes.ui.theme.*

data class RecipeRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val matchPercentage: String,
    val time: String,
    val difficulty: String,
    val isFavorite: Boolean = false
)

@Composable
fun RecommendationsScreen(
    selectedIngredients: List<String>,
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val recommendations = listOf(
        RecipeRecommendation(
            id = "1",
            title = "Egg and Potato Curry",
            description = "A rich, flavorful Kenyan-style curry.",
            matchPercentage = "98% Match",
            time = "40 mins",
            difficulty = "Medium",
            isFavorite = true
        ),
        RecipeRecommendation(
            id = "2",
            title = "Bhajias",
            description = "Crispy battered potatoes with herbs and spices.",
            matchPercentage = "70% Match",
            time = "35 mins",
            difficulty = "Easy"
        ),
        RecipeRecommendation(
            id = "3",
            title = "Spicy Egg Stew",
            description = "A hearty tomato and onion based stew.",
            matchPercentage = "80% Match",
            time = "25 mins",
            difficulty = "Easy"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5F0)) // Cream background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
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

                        // Search Bar & Filter
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

            // 2. Based on your ingredients subtitle (Dynamic)
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

            // 3. Recommendation Cards List
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
                            // Match percentage badge
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
                            val isFavorite = FavoritesManager.favoriteRecipeIds.contains(recipe.id)
                            IconButton(onClick = { FavoritesManager.toggleFavorite(recipe.id) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) Color.Red else TextSecondary
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
