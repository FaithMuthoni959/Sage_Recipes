package com.example.sagerecipes.ui.screens.herbs


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.theme.*

@Composable
fun HerbsScreen(
    onDisplayMealsClick: () -> Unit,
    onBackClick: () -> Unit,
    onCustomSearchClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // State tracking selected herbs
    val selectedHerbs = remember { mutableStateOf(setOf("Oregano", "Sage", "Coriander", "Parsley")) }

    val freshHerbs = listOf(
        "Oregano", "Basil",
        "Sage", "Coriander",
        "Parsley", "Green Garlic",
        "Thyme", "Rosemary",
        "Mint", "Chives",
        "Cilantro", "Other"
    )

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
                .padding(bottom = 24.dp), // Space for button
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // 2. Top Header (< Herbs, Search, Filter)
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Text(
                            text = "Herbs",
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

            // 3. White Card Container for Fresh Herbs Section
            item {
                Surface(
                    shape = RoundedCornerShape(topStart = Radius.card, topEnd = Radius.card),
                    color = Color(0xFFFDFBF7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Fresh Herbs Section
                        HerbCategorySection(
                            title = "Fresh Herbs",
                            items = freshHerbs,
                            selectedItems = selectedHerbs.value,
                            onItemToggle = { item, checked ->
                                selectedHerbs.value = if (checked) {
                                    selectedHerbs.value + item
                                } else {
                                    selectedHerbs.value - item
                                }
                            },
                            onCustomSearchClick = onCustomSearchClick
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // Display Meals Button
                        Button(
                            onClick = onDisplayMealsClick,
                            shape = RoundedCornerShape(Radius.button),
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Display meals", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HerbCategorySection(
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

        // 2-column grid for herbs
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                rowItems.forEach { herb ->
                    val isSelected = selectedItems.contains(herb)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (herb == "Other") {
                                    onCustomSearchClick()
                                } else {
                                    onItemToggle(herb, !isSelected)
                                }
                            }
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null // Handled by row click
                        )
                        Text(
                            text = herb,
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
fun HerbsScreenPreview() {
    SageRecipesTheme {
        HerbsScreen(
            onDisplayMealsClick = {},
            onBackClick = {},
            onCustomSearchClick = {}
        )
    }
}
