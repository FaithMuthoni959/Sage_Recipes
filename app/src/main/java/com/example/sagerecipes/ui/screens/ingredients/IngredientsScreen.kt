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
import com.example.sagerecipes.data.model.IngredientCategory
import com.example.sagerecipes.data.repository.IngredientRepository
import com.example.sagerecipes.ui.IngredientSelectionViewModel
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*

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
    val ingredientRepo = remember { IngredientRepository() }
    var searchQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    var remotePopular by remember { mutableStateOf<List<String>>(emptyList()) }
    var remoteVeggies by remember { mutableStateOf<List<String>>(emptyList()) }
    var remoteProteins by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val pop = ingredientRepo.getPopular(8)
            if (pop.isNotEmpty()) remotePopular = pop.map { it.name }
        } catch (_: Exception) {}

        try {
            val veg = ingredientRepo.getByCategories(IngredientCategory.VEGETABLE)
            if (veg.isNotEmpty()) remoteVeggies = veg.map { it.name } + "Other"
        } catch (_: Exception) {}

        try {
            val prot = ingredientRepo.getByCategories(IngredientCategory.PROTEIN)
            if (prot.isNotEmpty()) remoteProteins = prot.map { it.name } + "Other"
        } catch (_: Exception) {}
    }

    LaunchedEffect(selectedIngredients) {
        if (selectedIngredients.any { it !in IngredientSelectionViewModel.SPICES_AND_HERBS }) {
            errorMessage = null
        }
    }

    val popularIngredients = remember(remotePopular) {
        remotePopular.ifEmpty { listOf("Tomato", "Onion", "Garlic", "Potato") }
    }
    val rawVegetables = remember(remoteVeggies) {
        remoteVeggies.ifEmpty { listOf("Onions", "Tomato", "Potato", "Kales", "Other") }
    }
    val rawProteins = remember(remoteProteins) {
        remoteProteins.ifEmpty { listOf("Beef", "Eggs", "Chicken", "Dairy Products", "Other") }
    }

    val vegetables = remember(rawVegetables, searchQuery) {
        if (searchQuery.isBlank()) rawVegetables
        else rawVegetables.filter { it.contains(searchQuery, ignoreCase = true) || it == "Other" }
    }
    val proteins = remember(rawProteins, searchQuery) {
        if (searchQuery.isBlank()) rawProteins
        else rawProteins.filter { it.contains(searchQuery, ignoreCase = true) || it == "Other" }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5F0))
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search Ingredients") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
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
                            val isSelected = item in selectedIngredients

                            Surface(
                                shape = RoundedCornerShape(Radius.card),
                                color = Color.White,
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
                    title = "Vegetables",
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
                    title = "Proteins",
                    selectedCount = proteins.count { it in selectedIngredients },
                    items = proteins,
                    selectedItems = selectedIngredients,
                    onItemToggle = { item, _ ->
                        onIngredientToggle(item)
                    },
                    onCustomSearchClick = onCustomSearchClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(140.dp))
            }
        }

        // 5. Bottom Fixed Container (Button + SageBottomBar)
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
            selectedIngredients = emptySet(),
            onIngredientToggle = {},
            onAddSpicesClick = {},
            onBackClick = {},
            onCustomSearchClick = {}
        )
    }
}
