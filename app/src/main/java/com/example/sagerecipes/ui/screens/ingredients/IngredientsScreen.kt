package com.example.sagerecipes.ui.screens.ingredients

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.data.repository.IngredientRepository
import com.example.sagerecipes.ui.IngredientSelectionViewModel
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun IngredientsScreen(
    selectedIngredients: Set<String>,
    onIngredientToggle: (String) -> Unit,
    onAddSpicesClick: () -> Unit,
    onBackClick: () -> Unit,
    onCustomSearchClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val ingredientRepo = remember { IngredientRepository() }

    var searchQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var remoteSearchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val popularIngredients = remember {
        listOf("Tomato", "Onion", "Garlic", "Potato", "Chicken", "Eggs", "Beef", "Rice")
    }
    val vegetables = remember {
        listOf("Onions", "Tomato", "Potato", "Kales", "Spinach", "Carrots", "Other")
    }
    val proteins = remember {
        listOf("Beef", "Eggs", "Chicken", "Dairy Products", "Fish", "Other")
    }

    // Combine local items for instant search matching
    val localMatches = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) emptyList()
        else (popularIngredients + vegetables + proteins)
            .filter { it != "Other" && it.contains(q, ignoreCase = true) }
    }

    // Debounced remote search
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            remoteSearchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(250)
        try {
            val remote = ingredientRepo.search(q, limit = 15)
            remoteSearchResults = remote.map { it.name }
        } catch (_: Exception) {
            remoteSearchResults = emptyList()
        } finally {
            isSearching = false
        }
    }

    val displaySearchResults = remember(localMatches, remoteSearchResults) {
        (localMatches + remoteSearchResults)
            .map { it.trim().replaceFirstChar { c -> c.uppercase() } }
            .distinctBy { it.lowercase() }
    }

    LaunchedEffect(selectedIngredients) {
        if (selectedIngredients.any { it !in IngredientSelectionViewModel.SPICES_AND_HERBS }) {
            errorMessage = null
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
                .padding(bottom = 140.dp),
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
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary
                                )
                            }
                            Text(
                                text = "Ingredients",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // Search Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search Ingredients...") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
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
            }

            // Live Search Results
            if (searchQuery.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                    ) {
                        Text(
                            text = if (isSearching && displaySearchResults.isEmpty()) "SEARCHING..." else "SEARCH RESULTS (${displaySearchResults.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = ButtonGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))

                        if (displaySearchResults.isEmpty() && !isSearching) {
                            Text(
                                text = "No matching ingredients found for \"$searchQuery\".",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            OutlinedButton(
                                onClick = onCustomSearchClick,
                                shape = RoundedCornerShape(Radius.button),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ButtonGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Search full database for \"$searchQuery\"")
                            }
                        } else {
                            displaySearchResults.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                ) {
                                    rowItems.forEach { ingredient ->
                                        val isSelected = selectedIngredients.contains(ingredient)
                                        Surface(
                                            shape = RoundedCornerShape(Radius.card),
                                            color = MaterialTheme.colorScheme.surface,
                                            shadowElevation = 1.dp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onIngredientToggle(ingredient) }
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
            } else {
                // 2. Popular Section
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
                                val isSelected = item in selectedIngredients

                                Surface(
                                    shape = RoundedCornerShape(Radius.card),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.clickable {
                                        onIngredientToggle(item)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = Spacing.md,
                                            vertical = Spacing.sm
                                        ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
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
                        title = "Vegetables & Greens",
                        selectedCount = vegetables.count { it in selectedIngredients },
                        items = vegetables,
                        selectedItems = selectedIngredients,
                        onItemToggle = { item, _ ->
                            onIngredientToggle(item)
                        },
                        onCustomSearchClick = onCustomSearchClick
                    )
                }

                // 4. Proteins Section
                item {
                    IngredientCategorySection(
                        title = "Proteins & Meats",
                        selectedCount = proteins.count { it in selectedIngredients },
                        items = proteins,
                        selectedItems = selectedIngredients,
                        onItemToggle = { item, _ ->
                            onIngredientToggle(item)
                        },
                        onCustomSearchClick = onCustomSearchClick
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(140.dp))
            }
        }

        // Bottom Action Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(Radius.card),
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, Color(0xFFEF5350)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F)
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val hasMain = selectedIngredients.any { it !in IngredientSelectionViewModel.SPICES_AND_HERBS }
                    if (!hasMain) {
                        errorMessage = "Please select an ingredient to proceed"
                        Toast.makeText(context, "Please select an ingredient to proceed", Toast.LENGTH_SHORT).show()
                    } else {
                        errorMessage = null
                        onAddSpicesClick()
                    }
                },
                shape = RoundedCornerShape(Radius.button),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                    .height(50.dp)
            ) {
                Text(
                    "Add Spices →",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            SageBottomBar(
                selectedIndex = selectedIndex,
                onTabSelected = onBottomNavSelected
            )
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
                    modifier = Modifier.padding(
                        horizontal = Spacing.sm,
                        vertical = 4.dp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

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
                            color = MaterialTheme.colorScheme.surface,
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
            selectedIngredients = emptySet(),
            onIngredientToggle = {},
            onAddSpicesClick = {},
            onBackClick = {},
            onCustomSearchClick = {}
        )
    }
}
