package com.example.invoicely.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invoicely.network.DashboardSummaryResponse
import com.example.invoicely.network.RecentInvoiceDto
import com.example.invoicely.state.DashboardUiState

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onNewInvoiceClick: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    // 🚀 Trigger data fetch whenever DashboardScreen enters composition / re-opens
    LaunchedEffect(Unit) {
        onRefresh()
    }

    val canvasColor = Color(0xFFF6F5EC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasColor)
    ) {
        when (uiState) {
            is DashboardUiState.Loading -> DashboardLoadingSkeleton()
            is DashboardUiState.Empty -> DashboardEmptyState(
                onCreateInvoiceClick = onNewInvoiceClick
            )
            is DashboardUiState.Error -> Text("Error: ${uiState.message}", modifier = Modifier.padding(16.dp), color = Color.Red)
            is DashboardUiState.Success -> DashboardSuccessLayout(
                data = uiState.data,
                onNewInvoiceClick = onNewInvoiceClick
            )
        }
    }
}

// 🚀 Success Layout using LazyColumn
@Composable
fun DashboardSuccessLayout(
    data: DashboardSummaryResponse,
    onNewInvoiceClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. HERO SECTION
        item {
            HeroCard(
                revenue = data.revenueThisMonth,
                growthPercentage = data.revenueGrowthPercentage
            )
        }

        // 2. PAIR CARDS SECTION
        item {
            PairCardsRow(
                receivedAmount = data.receivedAmount,
                receivedCount = data.receivedCount,
                outstandingAmount = data.outstandingAmount,
                outstandingCount = data.outstandingCount,
                overdueCount = data.overdueCount
            )
        }

        // 3. QUICK ACTIONS SECTION
        item {
            Column {
                Text(
                    text = "QUICK ACTIONS",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
                QuickActionStrip(
                    onNewInvoiceClick = onNewInvoiceClick,
                    onQuickLinkClick = { /* TODO: Open Share Intent */ },
                    onExportClick = { /* TODO: Trigger CSV Download */ }
                )
            }
        }

        // 4. RECENT INVOICES SECTION
        item {
            Text(
                text = "RECENT INVOICES",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }

        items(data.recentInvoices) { invoice ->
            RecentInvoiceRow(
                invoice = invoice,
                onClick = { /* TODO: Navigate to Invoice Detail Screen */ }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// Shimmer skeleton for loading state
@Composable
fun DashboardLoadingSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val shimmerColor = Color.LightGray.copy(alpha = alpha)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(shimmerColor)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(shimmerColor)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(shimmerColor)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(shimmerColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(shimmerColor)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F5EC)
@Composable
fun DashboardSuccessPreview() {
    MaterialTheme {
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
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F5EC)
@Composable
fun DashboardEmptyPreview() {
    MaterialTheme {
        DashboardScreen(uiState = DashboardUiState.Empty)
    }
}