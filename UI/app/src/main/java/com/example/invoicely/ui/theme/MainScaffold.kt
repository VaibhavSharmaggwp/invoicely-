package com.example.invoicely.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.invoicely.network.DashboardSummaryResponse
import com.example.invoicely.network.RecentInvoiceDto
import com.example.invoicely.state.DashboardUiState

@Composable
fun MainScaffold(
    onNewInvoiceClick: () -> Unit = {},
    dashboardContent: @Composable () -> Unit = {
        Text("Dashboard Content", modifier = Modifier.padding(16.dp))
    }
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "Invoices", "Reports", "Settings")
    val icons = listOf(
        Icons.Filled.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.AutoMirrored.Filled.List,
        Icons.Filled.Settings
    )

    Scaffold(
        containerColor = Color(0xFFF6F5EC),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF6F5EC),
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Ink,
                            indicatorColor = Ink
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewInvoiceClick,
                containerColor = Chartreuse,
                contentColor = Ink,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Invoice")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (selectedItem) {
                0 -> dashboardContent()
                1 -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Invoices Screen", color = Ink)
                }
                2 -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Reports Screen", color = Ink)
                }
                3 -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Settings Screen", color = Ink)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F5EC)
@Composable
fun MainScaffoldPreview() {
    MaterialTheme {
        MainScaffold(
            dashboardContent = {
                val mockData = DashboardSummaryResponse(
                    revenueThisMonth = 384600.0,
                    revenueGrowthPercentage = 18.4,
                    receivedAmount = 268200.0,
                    receivedCount = 14,
                    outstandingAmount = 116400.0,
                    outstandingCount = 5,
                    overdueCount = 2,
                    recentInvoices = listOf(
                        RecentInvoiceDto("1", "INV-013", "Halcyon Hotels", 96400.0, "ISSUED"),
                        RecentInvoiceDto("2", "INV-012", "Nexus Tech", 45000.0, "PAID"),
                        RecentInvoiceDto("3", "INV-011", "Vertex Group", 12000.0, "OVERDUE")
                    )
                )
                DashboardScreen(uiState = DashboardUiState.Success(mockData))
            }
        )
    }
}