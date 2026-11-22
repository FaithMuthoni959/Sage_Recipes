package com.example.sagerecipes.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*

@Composable
fun ProfileScreen(
    onFavoriteRecipesClick: () -> Unit,
    onPantryInventoryClick: () -> Unit,
    onAccountSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBottomNavSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5F0)) // Cream background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Space for bottom
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
                        Text(
                            text = "My Profile",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. User Info Card (Mock Database Details)
            item {
                Card(
                    shape = RoundedCornerShape(Radius.card),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        // Avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(ButtonGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = ButtonGreen
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Sarah",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "chef@sagerecipes.com",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = ButtonGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Culinary Community Member",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ButtonGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Settings & Options List
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = "Account & Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = Spacing.xs)
                    )

                    ProfileOptionRow(icon = Icons.Default.Favorite, title = "Favorite Recipes", onClick = onFavoriteRecipesClick)
                    ProfileOptionRow(icon = Icons.Default.ShoppingCart, title = "Pantry Inventory", onClick = onPantryInventoryClick)
                    ProfileOptionRow(icon = Icons.Default.Star, title = "Dietary Preferences") { /* TODO */ }
                    ProfileOptionRow(icon = Icons.Default.Settings, title = "Account Settings", onClick = onAccountSettingsClick)
                    ProfileOptionRow(icon = Icons.AutoMirrored.Filled.ExitToApp, title = "Log Out", tint = Color.Red, onClick = onLogoutClick)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            SageBottomBar(
                selectedIndex = 3,
                onTabSelected = onBottomNavSelected
            )
        }
    }
}

@Composable
fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    tint: Color = TextPrimary,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(Radius.card),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = tint)
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = tint
                )
            }
            Text(text = "›", fontSize = 20.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    SageRecipesTheme {
        ProfileScreen(
            onFavoriteRecipesClick = {},
            onPantryInventoryClick = {},
            onAccountSettingsClick = {},
            onLogoutClick = {},
            onBottomNavSelected = {}
        )
    }
}
