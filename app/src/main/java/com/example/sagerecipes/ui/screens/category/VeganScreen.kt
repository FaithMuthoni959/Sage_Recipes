package com.example.sagerecipes.ui.screens.category

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
import androidx.compose.ui.unit.dp
import com.example.sagerecipes.data.FavoritesManager
import com.example.sagerecipes.data.model.RecipeSummary
import com.example.sagerecipes.data.repository.FavoritesRepository
import com.example.sagerecipes.data.repository.RecipeRepository
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.screens.recommendations.RecipeRecommendation
import com.example.sagerecipes.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun VeganScreen(
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    CategoryTemplateScreen(
        title = "Vegan Recipes",
        tagFilter = "vegan",
        fallbackRecipes = listOf(
            RecipeRecommendation("2", "Bhajias", "Crispy battered potatoes with herbs and spices.", "70% Match", "35 mins", "Easy")
        ),
        onRecipeClick = onRecipeClick,
        onBackClick = onBackClick,
        selectedIndex = selectedIndex,
        onBottomNavSelected = onBottomNavSelected
    )
}

@Composable
fun QuickScreen(
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    CategoryTemplateScreen(
        title = "Quick Recipes",
        isQuick = true,
        fallbackRecipes = listOf(
            RecipeRecommendation("3", "Spicy Egg Stew", "A hearty tomato and onion based stew.", "80% Match", "25 mins", "Easy")
        ),
        onRecipeClick = onRecipeClick,
        onBackClick = onBackClick,
        selectedIndex = selectedIndex,
        onBottomNavSelected = onBottomNavSelected
    )
}

@Composable
fun ProteinScreen(
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    CategoryTemplateScreen(
        title = "Protein Recipes",
        tagFilter = "protein",
        fallbackRecipes = listOf(
            RecipeRecommendation("1", "Egg and Potato Curry", "A rich, flavorful Kenyan-style curry.", "98% Match", "40 mins", "Medium"),
            RecipeRecommendation("4", "Kenyan Beef Pilau", "Authentic Swahili spiced rice cooked with tender beef chunks.", "95% Match", "45 mins", "Medium")
        ),
        onRecipeClick = onRecipeClick,
        onBackClick = onBackClick,
        selectedIndex = selectedIndex,
        onBottomNavSelected = onBottomNavSelected
    )
}

@Composable
fun DessertsScreen(
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    CategoryTemplateScreen(
        title = "Desserts Recipes",
        tagFilter = "dessert",
        fallbackRecipes = emptyList(),
        onRecipeClick = onRecipeClick,
        onBackClick = onBackClick,
        selectedIndex = selectedIndex,
        onBottomNavSelected = onBottomNavSelected
    )
}

@Composable
fun CategoryTemplateScreen(
    title: String,
    tagFilter: String? = null,
    isQuick: Boolean = false,
    fallbackRecipes: List<RecipeRecommendation> = emptyList(),
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    val recipeRepo = remember { RecipeRepository() }
    val favoritesRepo = remember { FavoritesRepository() }

    var searchQuery by remember { mutableStateOf("") }
    var remoteRecipes by remember { mutableStateOf<List<RecipeSummary>>(emptyList()) }
    var favoriteIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(title, tagFilter) {
        isLoading = true
        try {
            favoriteIds = favoritesRepo.getFavoriteIds()
        } catch (_: Exception) {
            // ignore
        }
        try {
            remoteRecipes = when {
                isQuick -> recipeRepo.getQuick(maxMinutes = 30)
                tagFilter != null -> recipeRepo.getByTag(tagFilter)
                else -> recipeRepo.getAll()
            }
        } catch (_: Exception) {
            remoteRecipes = emptyList()
        } finally {
            isLoading = false
        }
    }

    val displayRecipes: List<RecipeRecommendation> = remember(remoteRecipes, fallbackRecipes, searchQuery, favoriteIds) {
        val list = if (remoteRecipes.isNotEmpty()) {
            remoteRecipes.map { r ->
                val timeStr = r.cookTimeMinutes?.let { "$it mins" } ?: "30 mins"
                val diffStr = r.difficulty?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Medium"
                RecipeRecommendation(
                    id = r.id.toString(),
                    title = r.title,
                    description = r.description ?: "Delicious $title recipe",
                    matchPercentage = "Featured",
                    time = timeStr,
                    difficulty = diffStr,
                    isFavorite = favoriteIds.contains(r.id) || FavoritesManager.favoriteRecipeIds.contains(r.id.toString())
                )
            }
        } else {
            fallbackRecipes
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
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search $title...") },
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
                                .fillMaxWidth()
                                .height(56.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ButtonGreen)
                    }
                }
            } else if (displayRecipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recipes found in this category yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(displayRecipes) { recipe ->
                    val isFav = favoriteIds.contains(recipe.id.toLongOrNull() ?: -1L) || FavoritesManager.favoriteRecipeIds.contains(recipe.id)
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
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) Color.Red else TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(Spacing.sm))

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
