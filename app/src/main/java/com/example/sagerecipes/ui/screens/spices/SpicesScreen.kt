package com.example.sagerecipes.ui.screens.spices

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*

@Composable
fun SpicesScreen(
    selectedIngredients: Set<String>,
    onIngredientToggle: (String) -> Unit,
    onAddHerbsClick: () -> Unit,
    onBackClick: () -> Unit,
    onCustomSearchClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val commonSpices = listOf("Paprika", "Black pepper", "Cumin", "Turmeric")
    val aromatics = listOf("Garlic Powder", "Ginger", "Cinnamon", "Cloves")
    val specialtyBlends = listOf("Garam masala", "Pilau masala", "Curry Powder", "Other")

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background Image (background_image2)
        Image(
            painter = painterResource(id = R.drawable.background_image2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // 2. Top Header (< Spices, Search, Filter)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg)
                ) {
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
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                        Text(
                            text = "Spices",
                            style = MaterialTheme.typography.headlineMedium,
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
                            placeholder = { Text("Search Spices") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
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

            // 3. White Card Container for Spice Sections
            item {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = Radius.card,
                        topEnd = Radius.card
                    ),
                    color = Color(0xFFFDFBF7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Common Spices Section
                        SpiceCategorySection(
                            title = "Common Spices",
                            items = commonSpices,
                            selectedItems = selectedIngredients,
                            onItemToggle = { item, _ ->
                                onIngredientToggle(item)
                            },
                            onCustomSearchClick = onCustomSearchClick
                        )

                        // Aromatics Section
                        SpiceCategorySection(
                            title = "Aromatics",
                            items = aromatics,
                            selectedItems = selectedIngredients,
                            onItemToggle = { item, _ ->
                                onIngredientToggle(item)
                            },
                            onCustomSearchClick = onCustomSearchClick
                        )

                        // Specialty Blends Section
                        SpiceCategorySection(
                            title = "Specialty Blends",
                            items = specialtyBlends,
                            selectedItems = selectedIngredients,
                            onItemToggle = { item, _ ->
                                onIngredientToggle(item)
                            },
                            onCustomSearchClick = onCustomSearchClick
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // Add Herbs Button
                        Button(
                            onClick = onAddHerbsClick,
                            shape = RoundedCornerShape(Radius.button),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                "Add Herbs +",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))
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

@Composable
fun SpiceCategorySection(
    title: String,
    items: List<String>,
    selectedItems: Set<String>,
    onItemToggle: (String, Boolean) -> Unit,
    onCustomSearchClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ButtonGreen
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        // 2-column grid for spices
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                rowItems.forEach { spice ->
                    val isSelected = selectedItems.contains(spice)

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (spice == "Other") {
                                    onCustomSearchClick()
                                } else {
                                    onItemToggle(spice, !isSelected)
                                }
                            }
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null
                        )
                        Text(
                            text = spice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpicesScreenPreview() {
    SageRecipesTheme {
        SpicesScreen(
            selectedIngredients = emptySet(),
            onIngredientToggle = {},
            onAddHerbsClick = {},
            onBackClick = {},
            onCustomSearchClick = {}
        )
    }
}