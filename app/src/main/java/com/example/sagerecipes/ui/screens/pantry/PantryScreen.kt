package com.example.sagerecipes.ui.screens.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*

data class PantryItem(val name: String)
data class PantryCategory(val title: String, val itemCount: String, val items: List<PantryItem>)

@Composable
fun PantryScreen(
    onAddCustomClick: () -> Unit,
    onBottomNavSelected: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val essentials = listOf(
        PantryItem("Olive Oil"),
        PantryItem("Flour"),
        PantryItem("Rice")
    )

    val vegetables = listOf(
        PantryItem("Spinach"),
        PantryItem("Tomatoes")
    )

    val meats = listOf(
        PantryItem("Chicken")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5F0)) // Cream background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Space for bottom nav
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // 1. Top Header ("Pantry" & Search)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ButtonGreen)
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
                ) {
                    Column {
                        Text(
                            text = "Pantry",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

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
                                placeholder = { Text("Search your pantry...") },
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

            // 2. Pantry Essentials Section
            item {
                PantryCategoryRow(
                    title = "Pantry Essentials",
                    itemCount = "24 items",
                    items = essentials,
                    showAddButton = true,
                    onAddCustomClick = onAddCustomClick
                )
            }

            // 3. Vegetables and Greens Section
            item {
                PantryCategoryRow(
                    title = "Vegetables and greens",
                    itemCount = "12 items",
                    items = vegetables,
                    showAddButton = true,
                    onAddCustomClick = onAddCustomClick
                )
            }

            // 4. Meats Section
            item {
                PantryCategoryRow(
                    title = "Meats",
                    itemCount = "5 items",
                    items = meats,
                    showAddButton = true,
                    onAddCustomClick = onAddCustomClick
                )
            }

        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            SageBottomBar(
                selectedIndex = 2,
                onTabSelected = onBottomNavSelected
            )
        }
    }
}

@Composable
fun PantryCategoryRow(
    title: String,
    itemCount: String,
    items: List<PantryItem>,
    showAddButton: Boolean,
    onAddCustomClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg)
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
                    text = itemCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = ButtonGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(items) { item ->
                Surface(
                    shape = RoundedCornerShape(Radius.card),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(width = 80.dp, height = 90.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.xs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            if (showAddButton) {
                item {
                    Surface(
                        shape = RoundedCornerShape(Radius.card),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(width = 80.dp, height = 90.dp)
                            .clickable { onAddCustomClick() }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = ButtonGreen.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", tint = ButtonGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantryScreenPreview() {
    SageRecipesTheme {
        PantryScreen(onAddCustomClick = {}, onBottomNavSelected = {})
    }
}
