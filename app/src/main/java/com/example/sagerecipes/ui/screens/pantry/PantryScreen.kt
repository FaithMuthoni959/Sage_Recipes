package com.example.sagerecipes.ui.screens.pantry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.sagerecipes.data.model.PantryItem as DbPantryItem
import com.example.sagerecipes.data.repository.PantryRepository
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*
import kotlinx.coroutines.launch

data class UiPantryItem(val id: Long?, val name: String)

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

    val fallbackItems = remember {
        listOf(
            UiPantryItem(null, "Olive Oil"),
            UiPantryItem(null, "Flour"),
            UiPantryItem(null, "Rice"),
            UiPantryItem(null, "Spinach"),
            UiPantryItem(null, "Tomatoes"),
            UiPantryItem(null, "Chicken")
        )
    }

    val allUiItems: List<UiPantryItem> = remember(remotePantry, fallbackItems) {
        if (remotePantry.isNotEmpty()) {
            remotePantry.map { p ->
                UiPantryItem(id = p.ingredient.id, name = p.ingredient.name)
            }
        } else {
            fallbackItems
        }
    }

    val filteredUiItems = remember(allUiItems, searchQuery) {
        if (searchQuery.isBlank()) allUiItems
        else allUiItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

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
            .background(MaterialTheme.colorScheme.background)
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
                    PantryIngredientsSection(
                        title = "Ingredients available",
                        itemCount = "${filteredUiItems.size} items",
                        items = filteredUiItems,
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
fun PantryIngredientsSection(
    title: String,
    itemCount: String,
    items: List<UiPantryItem>,
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

        Spacer(modifier = Modifier.height(Spacing.md))

        if (items.isEmpty()) {
            Text(
                text = "No ingredients found in pantry.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(Spacing.md))
        } else {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    rowItems.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(Radius.card),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (item.id != null) {
                                    IconButton(
                                        onClick = { onRemoveClick(item) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = Color.Red.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
        }

        OutlinedButton(
            onClick = onAddCustomClick,
            shape = RoundedCornerShape(Radius.button),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ButtonGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = ButtonGreen)
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text("+ Add custom ingredient to pantry", fontWeight = FontWeight.Bold)
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
