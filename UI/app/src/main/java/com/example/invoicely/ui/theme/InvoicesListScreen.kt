package com.example.invoicely.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invoicely.network.RecentInvoiceDto
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesListScreen(
    invoices: List<RecentInvoiceDto>,
    onInvoiceClick: (String) -> Unit
) {
    // 1. State Variables
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    // Design Tokens & Typography
    val canvasBg = Color(0xFFF6F5EC)
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)
    val whiteCard = Color(0xFFFFFFFF)
    val mutedBorder = Color(0xFFE5E3D8)
    val subtextColor = Color(0xFF73786D)

    // 2. Filtering Logic
    val filteredInvoices = invoices.filter { invoice ->
        val matchesSearch = invoice.customerName.contains(searchQuery, ignoreCase = true) ||
                invoice.invoiceNumber.contains(searchQuery, ignoreCase = true)

        val matchesFilter = if (selectedFilter == "ALL") true else invoice.status.equals(selectedFilter, ignoreCase = true)

        matchesSearch && matchesFilter
    }

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Invoices",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = inkColor,
                            letterSpacing = (-0.5).sp
                        )
                        // Count Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(inkColor)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${invoices.size}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = chartreuseColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = canvasBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- 1. SEARCH BAR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search client or invoice #...",
                        fontFamily = OutfitFontFamily,
                        color = subtextColor,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = subtextColor,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = inkColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = inkColor
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = whiteCard,
                    unfocusedContainerColor = whiteCard,
                    focusedBorderColor = inkColor,
                    unfocusedBorderColor = mutedBorder,
                    cursorColor = inkColor
                )
            )

            // --- 2. FILTER CHIPS ---
            val filters = listOf("ALL", "ISSUED", "OVERDUE", "PAID")

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    val count = when (filter) {
                        "ALL" -> invoices.size
                        else -> invoices.count { it.status.equals(filter, ignoreCase = true) }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) inkColor else whiteCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) inkColor else mutedBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = filter,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) chartreuseColor else inkColor,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) chartreuseColor.copy(alpha = 0.25f)
                                        else Color(0xFFF1EFE6)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) chartreuseColor else subtextColor
                                )
                            }
                        }
                    }
                }
            }

            // --- 3. SECTION SUBHEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedFilter == "ALL") "ALL INVOICES" else "$selectedFilter INVOICES",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subtextColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${filteredInvoices.size} of ${invoices.size}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = subtextColor
                )
            }

            // --- 4. THE MASTER LIST ---
            if (filteredInvoices.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFEBE9DF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = subtextColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No invoices found",
                        fontFamily = OutfitFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = inkColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No results for \"$searchQuery\""
                               else "No invoices with status \"$selectedFilter\"",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        color = subtextColor,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredInvoices) { invoice ->
                        InvoiceListItem(
                            invoice = invoice,
                            onClick = { onInvoiceClick(invoice.id) }
                        )
                    }
                }
            }
        }
    }
}

// --- PREMIUM INVOICE ROW ITEM ---
@Composable
fun InvoiceListItem(
    invoice: RecentInvoiceDto,
    onClick: () -> Unit
) {
    val inkColor = Color(0xFF151A11)
    val whiteCard = Color(0xFFFFFFFF)
    val mutedBorder = Color(0xFFE5E3D8)

    val (statusBg, statusTextColor) = when (invoice.status.uppercase()) {
        "PAID" -> Pair(Color(0xFFE8F5E9), Color(0xFF2FA84F))
        "OVERDUE" -> Pair(Color(0xFFFFEBEE), Color(0xFFD32F2F))
        "PARTIALLY_PAID" -> Pair(Color(0xFFFFF3E0), Color(0xFFF57C00))
        else -> Pair(Color(0xFFF1EFE6), Color(0xFF73786D)) // ISSUED / DRAFT
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(whiteCard)
            .border(1.dp, mutedBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // --- LEFT SIDE: Avatar + Customer Details ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1EFE6)),
                contentAlignment = Alignment.Center
            ) {
                val initial = invoice.customerName.take(1).uppercase()
                Text(
                    text = initial,
                    color = inkColor,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = invoice.customerName,
                    color = inkColor,
                    fontFamily = OutfitFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = invoice.invoiceNumber,
                    color = Color(0xFF73786D),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- RIGHT SIDE: Amount + Status Pill ---
        Column(horizontalAlignment = Alignment.End) {
            val formattedAmount = String.format(Locale.forLanguageTag("en-IN"), "%,.0f", invoice.totalAmount)
            Text(
                text = "₹$formattedAmount",
                color = inkColor,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Status Pill with colored dot
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(statusTextColor)
                    )
                    Text(
                        text = invoice.status.uppercase(),
                        color = statusTextColor,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// --- PREVIEW BLOCK ---
@Preview(showBackground = true)
@Composable
fun InvoicesListScreenPreview() {
    MaterialTheme {
        val mockData = listOf(
            RecentInvoiceDto("1", "INV-013", "Halcyon Hotels", 96400.0, "ISSUED"),
            RecentInvoiceDto("2", "INV-012", "Nexus Tech", 45000.0, "PAID"),
            RecentInvoiceDto("3", "INV-011", "Vertex Group", 12000.0, "OVERDUE"),
            RecentInvoiceDto("4", "INV-010", "Acme Corp", 150000.0, "PAID")
        )
        InvoicesListScreen(invoices = mockData, onInvoiceClick = {})
    }
}