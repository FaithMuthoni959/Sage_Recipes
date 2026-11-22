package com.example.sagerecipes.ui.screens.auth

import com.example.sagerecipes.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sagerecipes.ui.theme.*

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterLinkClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(ButtonGreen)) {
        
        // 1. Bottom section: Background Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .align(Alignment.BottomCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.background_image2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

        }

        // 2. Top Olive Green Header with Logo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.28f)
                .background(ButtonGreen)
                .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sage_recipes_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(80.dp)
                )
                Text(
                    text = "Sage Recipes",
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                )
            }
        }

        // 3. Floating Card in Center
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = Spacing.lg)
                .fillMaxWidth(),
            shape = RoundedCornerShape(Radius.card),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(Spacing.lg)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "E-mail",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("name@example.com", color = TextSecondary) },
                        shape = RoundedCornerShape(Radius.input),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE2F0D9),
                            unfocusedContainerColor = Color(0xFFE2F0D9),
                            disabledContainerColor = Color(0xFFE2F0D9),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = TextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(Radius.input),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE2F0D9),
                            unfocusedContainerColor = Color(0xFFE2F0D9),
                            disabledContainerColor = Color(0xFFE2F0D9),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                Button(
                    onClick = { onLoginClick(email, password) },
                    shape = RoundedCornerShape(Radius.button),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Login", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Row {
                    Text("Don't have an account? ", color = TextSecondary)
                    Text(
                        "Register here",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onRegisterLinkClick() }
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(ButtonGreen.copy(alpha = 0.3f), RoundedCornerShape(50.dp)),
                    contentAlignment = Alignment.Center
                ) {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SageRecipesTheme {
        LoginScreen(onLoginClick = { _, _ -> }, onRegisterLinkClick = {})
    }
}
