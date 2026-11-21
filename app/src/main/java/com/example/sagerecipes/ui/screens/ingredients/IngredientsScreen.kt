package com.example.sagerecipes.ui.screens.ingredients

import com.example.sagerecipes.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.ui.theme.*

@Composable
fun IngredientsScreen(
    onAddSpicesClick: () -> Unit,
    onBackClick: () -> Unit,
    onCustomSearchClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Sample state tracking selected ingredients by name
    val selectedIngredients = remember { mutableStateOf(setOf("Onions", "Potato")) }

    val popularIngredients = listOf("Tomato", "Onion", "Garlic", "Potato")
    val vegetables = listOf("Onions", "Tomato", "Potato", "Kales", "Other")
    val proteins = listOf("Beef", "Eggs", "Chicken", "Dairy Products", "Other")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5F0)) // Cream background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Space for button
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
                                text = "Ingredients",
                                style = MaterialTheme.typography.headlineMedium,
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
                                placeholder = { Text("Search Ingredients") },
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

            // 2. Popular Ingredients Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                ) {
                    Text(
                        text = "POPULAR INGREDIENTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(popularIngredients) { item ->
                            val isSelected = selectedIngredients.value.contains(item)
                            Surface(
                                shape = RoundedCornerShape(Radius.card),
                                color = Color.White,
                                shadowElevation = 2.dp,
                                modifier = Modifier.clickable {
                                    selectedIngredients.value = if (isSelected) {
                                        selectedIngredients.value - item
                                    } else {
                                        selectedIngredients.value + item
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    Text(text = item, style = MaterialTheme.typography.bodyMedium)
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Vegetables Section
            item {
                IngredientCategorySection(
                    title = "Vegetables",
                    selectedCount = vegetables.count { selectedIngredients.value.contains(it) },
                    items = vegetables,
                    selectedItems = selectedIngredients.value,
                    onItemToggle = { item, checked ->
                        selectedIngredients.value = if (checked) {
                            selectedIngredients.value + item
                        } else {
                            selectedIngredients.value - item
                        }
                    },
                    onCustomSearchClick = onCustomSearchClick
                )
            }

            // 4. Proteins Section
            item {
                IngredientCategorySection(
                    title = "Proteins",
                    selectedCount = proteins.count { selectedIngredients.value.contains(it) },
                    items = proteins,
                    selectedItems = selectedIngredients.value,
                    onItemToggle = { item, checked ->
                        selectedIngredients.value = if (checked) {
                            selectedIngredients.value + item
                        } else {
                            selectedIngredients.value - item
                        }
                    },
                    onCustomSearchClick = onCustomSearchClick
                )
            }

            // Bottom Spacing for button
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // 5. Bottom Fixed Button ("Add Spices ->")
        Button(
            onClick = onAddSpicesClick,
            shape = RoundedCornerShape(Radius.button),
            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = Spacing.lg, end = Spacing.lg)
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Add Spices →", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun IngredientCategorySection(
    title: String,
    selectedCount: Int,
    items: List<String>,
    selectedItems: Set<String>,
    onItemToggle: (String, Boolean) -> Unit,
    onCustomSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = ButtonGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = ButtonGreen,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Grid/Card list of items
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    rowItems.forEach { ingredient ->
                        val isSelected = selectedItems.contains(ingredient)
                        Surface(
                            shape = RoundedCornerShape(Radius.card),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (ingredient == "Other") {
                                        onCustomSearchClick()
                                    } else {
                                        onItemToggle(ingredient, !isSelected)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(Spacing.md)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ingredient,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IngredientsScreenPreview() {
    SageRecipesTheme {
        IngredientsScreen(
            onAddSpicesClick = {},
            onBackClick = {},
            onCustomSearchClick = {}
        )
    }
}
