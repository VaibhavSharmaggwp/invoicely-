package com.example.invoicely.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invoicely.network.DashboardSummaryResponse
import com.example.invoicely.network.RecentInvoiceDto
import java.util.Locale

@Composable
fun HeroCard(revenue: Double, growthPercentage: Double){
    // 1. Design System Colors[cite: 1]
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)

    // 2. Main Box (The Ink Card)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(color = inkColor, shape = RoundedCornerShape(24.dp)) // 24dp radius design ke hisaab se[cite: 1]
            .padding(24.dp)
    ){
        // 3. Column to separate Top (Title) and Bottom (Money)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- TOP ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                // Title
                Text(
                    text = "REVENUE · THIS MONTH",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                // Growth Pill (Thoda transparent Chartreuse background)
                Box(
                    modifier = Modifier
                        .background(
                            color = chartreuseColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ){
                    Text(
                        text = "▲ $growthPercentage%",
                        color = chartreuseColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- BOTTOM ROW ---
            // Currency formatting (e.g., 384600.0 -> "3,84,600")
            val formattedRevenue = String.format(Locale("en", "IN"), "%,.0f", revenue)

            Text(
                text = "₹$formattedRevenue",
                color = chartreuseColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                // Fallback to monospace for now. Jab tum JetBrains Mono add karoge, yahan map karna[cite: 1]
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun PairCardsRow(
    receivedAmount: Double,
    receivedCount: Int,
    outstandingAmount: Double,
    outstandingCount: Int,
    overdueCount: Int
){
    // 1. Semantic Colors
    val inkColor = Color(0xFF151A11)
    val clearedGreen = Color(0xFF2FA84F)
    val errorRed = Color(0xFFD32F2F)     // Late/Overdue ke liye
    val warningAmber = Color(0xFFF57C00) // Due ke liye
    val whiteCard = Color(0xFFFFFFFF)

    // 2. Row to hold both cards side-by-side
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ){
        // --- CARD 1: RECEIVED ---
        // weight(1f) ensures yeh screen ka exactly 50% width lega (minus gap)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(140.dp)
                .background(color = whiteCard, shape = RoundedCornerShape(24.dp))
                .padding(16.dp)
        ){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "RECEIVED",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Column{
                    Text(
                        text = "₹${String.format(Locale("en", "IN"), "%,.0f", receivedAmount)}",
                        color = inkColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$receivedCount cleared",
                        color = clearedGreen, // Green indicates success
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- CARD 2: OUTSTANDING ---
        Box(
            modifier = Modifier
                .weight(1f)
                .height(140.dp)
                .background(color = whiteCard, shape = RoundedCornerShape(24.dp))
                .padding(16.dp)
        ){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween){
                Text(
                    text = "OUTSTANDING",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Column{
                    Text(
                        text = "₹${String.format(Locale("en", "IN"), "%,.0f", outstandingAmount)}",
                        color = inkColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Conditionally show Late vs Due
                    if(overdueCount > 0){
                        Text(
                            text = "$outstandingCount due · $overdueCount late",
                            color = errorRed, // Red if someone is late
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }else{
                        Text(
                            text = "$outstandingCount due",
                            color = warningAmber, // Amber if just waiting for payment
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun QuickActionStrip(
    onNewInvoiceClick: () -> Unit,
    onQuickLinkClick: () -> Unit,
    onExportClick: () -> Unit
){
    // Row to hold the three action buttons evenly
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ){
        // Har button screen ka exactly 1/3rd space lega due to weight(1f)
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Add,
            label = "New\nInvoice",
            onClick = onNewInvoiceClick
        )
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Share,
            label = "Quick\nPay Link",
            onClick = onQuickLinkClick
        )
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Download,
            label = "Export\nReport",
            onClick = onExportClick
        )
    }
}

// 2. REUSABLE BUTTON COMPONENT
// Ek generic component bana liya taaki code repeat na ho

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
){
    val whiteCard = Color(0xFFFFFFFF)
    val inkColor = Color(0xFF151A11)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp)) // Design system radius
            .background(whiteCard)
            .clickable { onClick() } // Clickable banaya
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        // Icon Box (Thoda light background highlight ke liye)
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ){
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = inkColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label (Multiline handle karne ke liye text alignment center rakha hai)
        Text(
            text = label,
            color = inkColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp, // Line height set ki taaki "\n" achha dikhe
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}


@Composable
fun RecentInvoiceRow(
    invoice: RecentInvoiceDto,
    onClick: () -> Unit
){
    val inkColor = Color(0xFF151A11)
    val whiteCard = Color(0xFFFFFFFF)

    // Main Row Card
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(whiteCard)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        // --- LEFT SIDE: Avatar & Details ---
        Row(verticalAlignment = Alignment.CenterVertically){
            // 1. Customer Avatar (Circle with 1st Letter)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0)), // Light Gray
                contentAlignment = Alignment.Center
            ){
                // Customer name ka pehla letter nikal rahe hain
                val initial = invoice.customerName.take(1).uppercase()
                Text(
                    text = initial,
                    color = inkColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // 2. Name & Invoice Number
            Column {
                Text(
                    text = invoice.customerName,
                    color = inkColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = invoice.invoiceNumber,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- RIGHT SIDE: Amount & Status Pill ---
        Column(horizontalAlignment = Alignment.End){
            // Amount
            Text(
                text = "₹${String.format(Locale("en", "IN"), "%,.0f", invoice.totalAmount)}",
                color = inkColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))

            // 3. Status Pill (Dynamic Colors)
            StatusPill(status = invoice.status)
        }
    }
}

// Helper Composable to dynamically color the status pill
@Composable
fun StatusPill(status: String) {
    // Determine colors based on the text[cite: 1]
    val (bgColor, textColor) = when (status.uppercase()) {
        "PAID" -> Pair(Color(0xFFE8F5E9), Color(0xFF2FA84F))       // Light Green / Dark Green
        "OVERDUE" -> Pair(Color(0xFFFFEBEE), Color(0xFFD32F2F))    // Light Red / Dark Red
        "PARTIALLY_PAID" -> Pair(Color(0xFFFFF3E0), Color(0xFFF57C00)) // Light Orange / Orange
        else -> Pair(Color(0xFFF5F5F5), Color(0xFF757575))         // ISSUED/DRAFT: Light Gray / Gray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}


@Composable
fun DashboardContent(
    data: DashboardSummaryResponse,
    modifier: Modifier = Modifier,
    onNewInvoiceClick: () -> Unit = {},
    onQuickLinkClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    onInvoiceClick: (RecentInvoiceDto) -> Unit = {},
    onSeeAllClick: () -> Unit = {}
) {
    val inkColor = Color(0xFF151A11)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO CARD (Revenue & Growth)
        HeroCard(
            revenue = data.revenueThisMonth,
            growthPercentage = data.revenueGrowthPercentage
        )

        // 2. PAIR CARDS ROW (Received & Outstanding)
        PairCardsRow(
            receivedAmount = data.receivedAmount,
            receivedCount = data.receivedCount,
            outstandingAmount = data.outstandingAmount,
            outstandingCount = data.outstandingCount,
            overdueCount = data.overdueCount
        )

        // 3. QUICK ACTION STRIP
        QuickActionStrip(
            onNewInvoiceClick = onNewInvoiceClick,
            onQuickLinkClick = onQuickLinkClick,
            onExportClick = onExportClick
        )

        // 4. RECENT INVOICES HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT INVOICES",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "See all",
                color = inkColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        // 5. RECENT INVOICES LIST
        if (data.recentInvoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent invoices",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                data.recentInvoices.forEach { invoice ->
                    RecentInvoiceRow(
                        invoice = invoice,
                        onClick = { onInvoiceClick(invoice) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(
    name = "Dashboard Full Screen",
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFFF6F5EC
)
@Composable
fun DashboardFullScreenPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F5EC))
                .padding(16.dp)
        ) {
            DashboardContent(
                data = DashboardSummaryResponse(
                    revenueThisMonth = 384600.0,
                    revenueGrowthPercentage = 12.5,
                    receivedAmount = 268200.0,
                    receivedCount = 12,
                    outstandingAmount = 126000.0,
                    outstandingCount = 5,
                    overdueCount = 2,
                    recentInvoices = listOf(
                        RecentInvoiceDto(
                            id = "1",
                            invoiceNumber = "INV-013",
                            customerName = "Halcyon Hotels",
                            totalAmount = 96400.0,
                            status = "ISSUED"
                        ),
                        RecentInvoiceDto(
                            id = "2",
                            invoiceNumber = "INV-012",
                            customerName = "Acme Studios",
                            totalAmount = 128500.0,
                            status = "PAID"
                        ),
                        RecentInvoiceDto(
                            id = "3",
                            invoiceNumber = "INV-011",
                            customerName = "Nexus Media",
                            totalAmount = 45000.0,
                            status = "OVERDUE"
                        ),
                        RecentInvoiceDto(
                            id = "4",
                            invoiceNumber = "INV-010",
                            customerName = "Stark Industries",
                            totalAmount = 67200.0,
                            status = "PARTIALLY_PAID"
                        )
                    )
                )
            )
        }
    }
}