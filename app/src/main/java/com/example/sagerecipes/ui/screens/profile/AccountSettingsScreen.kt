package com.example.sagerecipes.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.ui.components.SageBottomBar
import com.example.sagerecipes.ui.theme.*

@Composable
fun AccountSettingsScreen(
    initialName: String = "",
    initialEmail: String = "",
    onBackClick: () -> Unit,
    onSaveClick: (newName: String) -> Unit,
    onDeleteAccountClick: () -> Unit,
    selectedIndex: Int = 3,
    onBottomNavSelected: (Int) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember(initialName) { mutableStateOf((initialName)) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val isDarkMode = ThemeManager.isDarkMode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            // Top Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ButtonGreen)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
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
                        text = "Account Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Form Fields & Settings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Profile Section
                Text(text = "Profile Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ButtonGreen)
                
                Text(text = "Name", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    shape = RoundedCornerShape(Radius.input),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "E-mail", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    shape = RoundedCornerShape(Radius.input),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Security Section
                Text(text = "Change Password", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ButtonGreen)

                Text(text = "Current Password", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("••••••••") },
                    shape = RoundedCornerShape(Radius.input),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "New Password", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("••••••••") },
                    shape = RoundedCornerShape(Radius.input),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Appearance Section
                Text(text = "Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ButtonGreen)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Radius.card))
                        .padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isDarkMode) "Dark Theme" else "Light Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { enabled ->
                            ThemeManager.setDarkMode(context, enabled)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ButtonGreen, checkedTrackColor = ButtonGreen.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Save Changes Button
                Button(
                    onClick = { onSaveClick(name) } ,
                    shape = RoundedCornerShape(Radius.button),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Save Changes", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Delete Account Button
                OutlinedButton(
                    onClick = onDeleteAccountClick,
                    shape = RoundedCornerShape(Radius.button),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text("Delete Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
fun AccountSettingsScreenPreview() {
    SageRecipesTheme {
        AccountSettingsScreen(onBackClick = {}, onSaveClick = {}, onDeleteAccountClick = {})
    }
}
