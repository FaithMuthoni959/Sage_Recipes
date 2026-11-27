package com.example.sagerecipes.ui.screens.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.R
import com.example.sagerecipes.data.model.RecipeSummary
import com.example.sagerecipes.data.repository.RecipeRepository
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*
import kotlinx.coroutines.delay

data class CategoryItem(val title: String, val iconRes: Int)

@Composable
fun HomeScreen(
    firstName: String = "",
    onAddIngredientsClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onRecipeClick: (Long) -> Unit = {},
    onAccountSettingsClick: () -> Unit,
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    onBottomNavSelected: (Int) -> Unit
) {
    val recipeRepo = remember { RecipeRepository() }
    val focusManager = LocalFocusManager.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<RecipeSummary>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            searchResults = emptyList()
            isSearching = false
            searchError = null
            return@LaunchedEffect
        }
        isSearching = true
        searchError = null
        delay(350)
        try {
            searchResults = recipeRepo.search(q)
        } catch (e: Exception) {
            Log.e("SageSearch", "Search failed for '$q'", e)
            searchError = e.message ?: "Search failed"
            searchResults = emptyList()
        } finally {
            isSearching = false
        }
    }

    val categories = listOf(
        CategoryItem("Vegan", R.drawable.category_vegan_food),
        CategoryItem("Quick", R.drawable.category_quick_meals),
        CategoryItem("Protein", R.drawable.category_high_protein),
        CategoryItem("Desserts", R.drawable.category_desserts)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // 1. Top Header (Olive Green Background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ButtonGreen)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sage Recipes",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Box {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Account Settings") },
                                    onClick = {
                                        showMenu = false
                                        onAccountSettingsClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("About Sage Recipes") },
                                    onClick = {
                                        showMenu = false
                                        onAboutClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Log Out") },
                                    onClick = {
                                        showMenu = false
                                        onLogoutClick()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Text(
                        text = if (firstName.isNotBlank()) "Hello, $firstName" else "Hello",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "What are we cooking today?",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search recipes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() },
                            onDone = { focusManager.clearFocus() }
                        ),
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

            // Live Search Results (when user types in search bar)
            if (searchQuery.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = when {
                            isSearching -> "Searching..."
                            searchError != null -> "Search Error"
                            else -> "Search Results (${searchResults.size})"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (searchError != null) {
                        Text(
                            text = searchError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (!isSearching && searchResults.isEmpty()) {
                        Text(
                            text = "No recipes found for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    searchResults.forEach { recipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRecipeClick(recipe.id) },
                            shape = RoundedCornerShape(Radius.card),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                val meta = listOfNotNull(
                                    recipe.cookTimeMinutes?.let { "$it min" },
                                    recipe.difficulty?.name
                                ).joinToString(" • ")
                                if (meta.isNotEmpty()) {
                                    Text(
                                        text = meta,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Categories Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg)
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(categories) { category ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.card))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { onCategoryClick(category.title) }
                                .padding(Spacing.md)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(Radius.input))
                                    .background(ButtonGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = category.iconRes),
                                    contentDescription = category.title,
                                    modifier = Modifier.size(32.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // 3. Leftovers Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                shape = RoundedCornerShape(Radius.card),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.background_image2),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.lg),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Turn your leftovers\ninto a feast",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        Button(
                            onClick = onAddIngredientsClick,
                            shape = RoundedCornerShape(Radius.button),
                            colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen)
                        ) {
                            Text("+ Add ingredients", color = Color.White)
                        }
                    }
                }
            }
        }

        // 4. SageBottomBar at bottom center
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            SageBottomBar(
                selectedIndex = 0,
                onTabSelected = onBottomNavSelected
            )
        }
    }
}