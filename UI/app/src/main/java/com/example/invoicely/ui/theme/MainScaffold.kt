package com.example.invoicely.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MainScaffold(
    currentRoute: String, // Tracks which screen the user is currently on
    onNavigate: (String) -> Unit, // Handles bottom bar tab clicks
    onNewInvoiceClick: (() -> Unit)? = null, // Optional FAB action
    content: @Composable () -> Unit // Screen's active content
) {
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)
    val canvasBg = Color(0xFFF6F5EC)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // 1. Dashboard Tab
                NavigationBarItem(
                    selected = currentRoute == "dashboard",
                    onClick = { onNavigate("dashboard") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = {
                        Text(
                            text = "Home",
                            fontFamily = OutfitFontFamily,
                            fontWeight = if (currentRoute == "dashboard") FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = inkColor,
                        selectedTextColor = inkColor,
                        indicatorColor = chartreuseColor.copy(alpha = 0.5f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // 2. Ledger / Invoices Tab
                NavigationBarItem(
                    selected = currentRoute == "ledger",
                    onClick = { onNavigate("ledger") },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Ledger") },
                    label = {
                        Text(
                            text = "Invoices",
                            fontFamily = OutfitFontFamily,
                            fontWeight = if (currentRoute == "ledger") FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = inkColor,
                        selectedTextColor = inkColor,
                        indicatorColor = chartreuseColor.copy(alpha = 0.5f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // 3. History Tab
                NavigationBarItem(
                    selected = currentRoute == "history",
                    onClick = { onNavigate("history") },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = {
                        Text(
                            text = "History",
                            fontFamily = OutfitFontFamily,
                            fontWeight = if (currentRoute == "history") FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = inkColor,
                        selectedTextColor = inkColor,
                        indicatorColor = chartreuseColor.copy(alpha = 0.5f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // 4. Settings Tab
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { onNavigate("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = {
                        Text(
                            text = "Settings",
                            fontFamily = OutfitFontFamily,
                            fontWeight = if (currentRoute == "settings") FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = inkColor,
                        selectedTextColor = inkColor,
                        indicatorColor = chartreuseColor.copy(alpha = 0.5f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        floatingActionButton = {
            if (onNewInvoiceClick != null) {
                FloatingActionButton(
                    onClick = onNewInvoiceClick,
                    containerColor = chartreuseColor,
                    contentColor = inkColor,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Invoice")
                }
            }
        },
        containerColor = canvasBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScaffoldPreview() {
    MaterialTheme {
        MainScaffold(
            currentRoute = "dashboard",
            onNavigate = {}
        ) {
            Text("Dashboard Content", modifier = Modifier.padding(16.dp))
        }
    }
}