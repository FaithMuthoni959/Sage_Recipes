package com.example.sagerecipes.ui.screens.herbs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.IngredientSelectionViewModel
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*

@Composable
fun HerbsScreen(
    selectedIngredients: Set<String>,
    onIngredientToggle: (String) -> Unit,
    onDisplayMealsClick: () -> Unit,
    onBackClick: () -> Unit,
    onCustomSearchClick: () -> Unit,
    selectedIndex: Int = 0,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(selectedIngredients) {
        if (selectedIngredients.any { it !in IngredientSelectionViewModel.SPICES_AND_HERBS }) {
            errorMessage = null
        }
    }

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
                .padding(bottom = 80.dp),
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
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
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

            // 3. White Card Container for Fresh Herbs Section
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
                            .padding(
                                horizontal = Spacing.lg,
                                vertical = Spacing.md
                            ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Fresh Herbs Section
                        HerbCategorySection(
                            title = "Fresh Herbs",
                            items = freshHerbs,
                            selectedItems = selectedIngredients,
                            onItemToggle = { item, _ ->
                                onIngredientToggle(item)
                            },
                            onCustomSearchClick = onCustomSearchClick
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        if (errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(Radius.card),
                                color = Color(0xFFFFEBEE),
                                border = BorderStroke(1.dp, Color(0xFFEF5350)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
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
                            Spacer(modifier = Modifier.height(Spacing.sm))
                        }

                        // Display Meals Button
                        Button(
                            onClick = {
                                val hasMain = selectedIngredients.any { it !in IngredientSelectionViewModel.SPICES_AND_HERBS }
                                if (!hasMain) {
                                    errorMessage = "Please select an ingredient to proceed"
                                    Toast.makeText(context, "Please select an ingredient to proceed", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = null
                                    onDisplayMealsClick()
                                }
                            },
                            shape = RoundedCornerShape(Radius.button),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                "Display meals",
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
                            onCheckedChange = null
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
            selectedIngredients = emptySet(),
            onIngredientToggle = {},
            onDisplayMealsClick = {},
            onBackClick = {},
            onCustomSearchClick = {}
        )
    }
}