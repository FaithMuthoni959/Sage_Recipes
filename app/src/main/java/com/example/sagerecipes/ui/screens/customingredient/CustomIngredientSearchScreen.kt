package com.example.sagerecipes.ui.screens.customingredient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sagerecipes.data.model.Ingredient
import com.example.sagerecipes.data.repository.IngredientRepository
import com.example.sagerecipes.data.repository.PantryRepository
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomIngredientViewModel : ViewModel() {
    private val ingredientRepo = IngredientRepository()
    private val pantryRepo = PantryRepository()

    private val _searchResults = MutableStateFlow<List<Ingredient>>(emptyList())
    val searchResults: StateFlow<List<Ingredient>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    fun searchIngredientInDatabase(query: String) {
        val q = query.trim()
        if (q.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = ingredientRepo.search(q)
                if (results.isNotEmpty()) {
                    _searchResults.value = results
                } else {
                    _searchResults.value = listOf(
                        Ingredient(id = -1, name = q, category = com.example.sagerecipes.data.model.IngredientCategory.OTHER)
                    )
                }
            } catch (_: Exception) {
                _searchResults.value = listOf(
                    Ingredient(id = -1, name = q, category = com.example.sagerecipes.data.model.IngredientCategory.OTHER)
                )
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun addToPantry(ingredient: Ingredient, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (ingredient.id > 0) {
                try {
                    pantryRepo.addOrUpdate(ingredient.id)
                } catch (_: Exception) {
                    // ignore
                }
            }
            onComplete()
        }
    }
}

@Composable
fun CustomIngredientSearchScreen(
    onBackClick: () -> Unit,
    onIngredientAdded: (String) -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val viewModel = remember { CustomIngredientViewModel() }
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Top Header
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
                            text = "Search Your Ingredient",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchIngredientInDatabase(it)
                        },
                        placeholder = { Text("Search ingredient...") },
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

            // Results List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                if (isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ButtonGreen)
                        }
                    }
                } else if (results.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Type an ingredient above to search the database.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    items(results) { ingredient ->
                        Card(
                            shape = RoundedCornerShape(Radius.card),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.lg),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ingredient.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Button(
                                    onClick = {
                                        viewModel.addToPantry(ingredient) {
                                            onIngredientAdded(ingredient.name)
                                        }
                                    },
                                    shape = RoundedCornerShape(Radius.button),
                                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                                    contentPadding = PaddingValues(horizontal = Spacing.md, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add")
                                }
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

@Preview(showBackground = true)
@Composable
fun CustomIngredientSearchScreenPreview() {
    SageRecipesTheme {
        CustomIngredientSearchScreen(onBackClick = {}, onIngredientAdded = {})
    }
}
