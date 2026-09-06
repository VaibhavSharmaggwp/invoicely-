package com.example.invoicely.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(onAuthSuccess: () -> Unit = {}) {
    // 1. State Variables: Yeh screen ka data hold karenge
    var isLoginMode by remember { mutableStateOf(true) } // Toggle between Login and Signup
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") } // Sirf signup me dikhega
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Design System Colors
    val canvasColor = Color(0xFFF6F5EC)
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)
    val mutedBorderColor = Color(0xFFE2E0D4)

    // 2. Main Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Brand Logo Badge
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(chartreuseColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = "Invoicely Logo",
                    tint = inkColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Title & Tagline
            Text(
                text = "Invoicely",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = inkColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isLoginMode) "Welcome back to your workspace" else "Start managing invoices effortlessly",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Segmented Pill Switcher (Login / Sign Up)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEBEADF))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isLoginMode) inkColor else Color.Transparent)
                        .clickable { isLoginMode = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log In",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLoginMode) chartreuseColor else inkColor.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (!isLoginMode) inkColor else Color.Transparent)
                        .clickable { isLoginMode = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isLoginMode) chartreuseColor else inkColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Conditional Field: Business Name for Signup
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business Name") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Business, contentDescription = null, tint = inkColor.copy(alpha = 0.6f))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = inkColor,
                                unfocusedBorderColor = mutedBorderColor,
                                cursorColor = inkColor
                            )
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = inkColor.copy(alpha = 0.6f))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorderColor,
                            cursorColor = inkColor
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = inkColor.copy(alpha = 0.6f))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = inkColor.copy(alpha = 0.6f)
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorderColor,
                            cursorColor = inkColor
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            onAuthSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = chartreuseColor,
                            contentColor = inkColor
                        )
                    ) {
                        Text(
                            text = if (isLoginMode) "Log In" else "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Switcher Text
            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                Text(
                    text = if (isLoginMode) "Don't have an account? Sign up" else "Already have an account? Log in",
                    color = inkColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// 6. Preview
@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    MaterialTheme {
        AuthScreen()
    }
}