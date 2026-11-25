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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sagerecipes.data.model.IngredientCategory
import com.example.sagerecipes.data.model.PantryItem as DbPantryItem
import com.example.sagerecipes.data.repository.PantryRepository
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*
import kotlinx.coroutines.launch

data class UiPantryItem(val id: Long?, val name: String, val category: String)

@Composable
fun PantryScreen(
    onAddCustomClick: () -> Unit,
    onBottomNavSelected: (Int) -> Unit
) {
    val pantryRepo = remember { PantryRepository() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var remotePantry by remember { mutableStateOf<List<DbPantryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            remotePantry = pantryRepo.getPantry()
        } catch (_: Exception) {
            remotePantry = emptyList()
        } finally {
            isLoading = false
        }
    }

    val fallbackEssentials = remember {
        listOf(
            UiPantryItem(null, "Olive Oil", "Essentials"),
            UiPantryItem(null, "Flour", "Essentials"),
            UiPantryItem(null, "Rice", "Essentials")
        )
    }
    val fallbackVeggies = remember {
        listOf(
            UiPantryItem(null, "Spinach", "Vegetables"),
            UiPantryItem(null, "Tomatoes", "Vegetables")
        )
    }
    val fallbackMeats = remember {
        listOf(
            UiPantryItem(null, "Chicken", "Meats")
        )
    }

    val allUiItems: List<UiPantryItem> = remember(remotePantry, fallbackEssentials, fallbackVeggies, fallbackMeats) {
        if (remotePantry.isNotEmpty()) {
            remotePantry.map { p ->
                val catName = when (p.ingredient.category) {
                    IngredientCategory.VEGETABLE -> "Vegetables"
                    IngredientCategory.PROTEIN -> "Meats"
                    IngredientCategory.SPICE, IngredientCategory.HERB, IngredientCategory.AROMATIC, IngredientCategory.BLEND -> "Spices & Herbs"
                    IngredientCategory.STAPLE, IngredientCategory.GRAIN, IngredientCategory.DAIRY, IngredientCategory.LEGUME -> "Essentials"
                    else -> "Other Essentials"
                }
                UiPantryItem(id = p.ingredient.id, name = p.ingredient.name, category = catName)
            }
        } else {
            fallbackEssentials + fallbackVeggies + fallbackMeats
        }
    }

    val filteredUiItems = remember(allUiItems, searchQuery) {
        if (searchQuery.isBlank()) allUiItems
        else allUiItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val essentials = filteredUiItems.filter { it.category == "Essentials" || it.category == "Other Essentials" }
    val vegetables = filteredUiItems.filter { it.category == "Vegetables" }
    val meats = filteredUiItems.filter { it.category == "Meats" || it.category == "Spices & Herbs" }

    fun removeItem(item: UiPantryItem) {
        if (item.id != null) {
            scope.launch {
                try {
                    pantryRepo.remove(item.id)
                    remotePantry = remotePantry.filterNot { it.ingredient.id == item.id }
                } catch (_: Exception) {
                    // ignore
                }
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
                        Text(
                            text = "Pantry",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

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

            if (isLoading) {
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
            } else {
                item {
                    PantryCategoryRow(
                        title = "Pantry Essentials",
                        itemCount = "${essentials.size} items",
                        items = essentials,
                        showAddButton = true,
                        onAddCustomClick = onAddCustomClick,
                        onRemoveClick = ::removeItem
                    )
                }

                item {
                    PantryCategoryRow(
                        title = "Vegetables and greens",
                        itemCount = "${vegetables.size} items",
                        items = vegetables,
                        showAddButton = true,
                        onAddCustomClick = onAddCustomClick,
                        onRemoveClick = ::removeItem
                    )
                }

                item {
                    PantryCategoryRow(
                        title = "Proteins and Spices",
                        itemCount = "${meats.size} items",
                        items = meats,
                        showAddButton = true,
                        onAddCustomClick = onAddCustomClick,
                        onRemoveClick = ::removeItem
                    )
                }
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
    items: List<UiPantryItem>,
    showAddButton: Boolean,
    onAddCustomClick: () -> Unit,
    onRemoveClick: (UiPantryItem) -> Unit = {}
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
                    modifier = Modifier.size(width = 90.dp, height = 95.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Spacing.xs),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 2
                            )
                        }

                        if (item.id != null) {
                            IconButton(
                                onClick = { onRemoveClick(item) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .padding(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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
                            .size(width = 90.dp, height = 95.dp)
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
