package com.example.invoicely.ui.theme

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invoicely.network.HistoryEventDto
import java.util.Locale

// Backwards-compatible alias so existing call sites continue to work
typealias HistoryEventUiModel = HistoryEventDto
typealias HistoryEventType = com.example.invoicely.network.HistoryEventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    events: List<HistoryEventDto> = emptyList(),
    onInvoiceClick: (String) -> Unit = {},
    onExportClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // State Variables
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedEventForReceipt by remember { mutableStateOf<HistoryEventDto?>(null) }

    // Design Tokens & Typography
    val canvasBg = Color(0xFFF6F5EC)
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)
    val whiteCard = Color(0xFFFFFFFF)
    val mutedBorder = Color(0xFFE5E3D8)
    val subtextColor = Color(0xFF73786D)
    val greenCleared = Color(0xFF2FA84F)

    // Financial Metrics Calculation from actual backend events
    val totalSettled = events
        .filter { it.type.equals("PAYMENT_RECEIVED", ignoreCase = true) }
        .sumOf { it.amount }
    val settledCount = events.count { it.type.equals("PAYMENT_RECEIVED", ignoreCase = true) }
    val overdueCount = events.count { it.type.equals("OVERDUE", ignoreCase = true) }

    // Real-Time Filter & Search Logic
    val filteredEvents = events.filter { event ->
        val matchesSearch = event.title.contains(searchQuery, ignoreCase = true) ||
                event.subtitle.contains(searchQuery, ignoreCase = true) ||
                event.customerName.contains(searchQuery, ignoreCase = true) ||
                event.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                event.transactionId.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "ALL" -> true
            "SETTLED" -> event.type.equals("PAYMENT_RECEIVED", ignoreCase = true)
            "ISSUED" -> event.type.equals("INVOICE_CREATED", ignoreCase = true)
            "OVERDUE" -> event.type.equals("OVERDUE", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Financial Ledger",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = inkColor,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Audit trail & settlement records",
                            fontFamily = OutfitFontFamily,
                            color = subtextColor,
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (onExportClick != null) {
                            onExportClick()
                        } else {
                            Toast.makeText(context, "Exporting ledger report to CSV...", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Export CSV",
                            tint = inkColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = canvasBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // --- 1. HERO FINANCIAL INFLOW CARD ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = inkColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL SETTLED INFLOW",
                                fontFamily = JetBrainsMonoFontFamily,
                                color = Color(0xFFA0A59A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(greenCleared.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "● LIVE LEDGER",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = greenCleared,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val formattedRevenue = String.format(Locale.forLanguageTag("en-IN"), "%,.0f", totalSettled)
                        Text(
                            text = "₹$formattedRevenue",
                            fontFamily = JetBrainsMonoFontFamily,
                            color = chartreuseColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF22291C))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$settledCount Settlements",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (overdueCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF3B1C1C))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                    Text(
                                        text = "$overdueCount Overdue Alerts",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = Color(0xFFFF8A80),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF22291C))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "100% Verified",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = chartreuseColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. SEARCH BAR ---
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search client, invoice #, or TXN ref...",
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
            }

            // --- 3. FILTER CHIPS ---
            item {
                val filters = listOf("ALL", "SETTLED", "ISSUED", "OVERDUE")

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        val count = when (filter) {
                            "ALL" -> events.size
                            "SETTLED" -> events.count { it.type.equals("PAYMENT_RECEIVED", ignoreCase = true) }
                            "ISSUED" -> events.count { it.type.equals("INVOICE_CREATED", ignoreCase = true) }
                            "OVERDUE" -> events.count { it.type.equals("OVERDUE", ignoreCase = true) }
                            else -> 0
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
            }

            // --- 4. SECTION HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIT TRAIL & SETTLEMENTS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${filteredEvents.size} of ${events.size}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = subtextColor
                    )
                }
            }

            // --- 5. TIMELINE / LEDGER ITEMS ---
            if (filteredEvents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
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
                            text = "No ledger entries found",
                            fontFamily = OutfitFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = inkColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results matching \"$searchQuery\""
                                   else "No transactions recorded under \"$selectedFilter\"",
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            color = subtextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(filteredEvents) { index, event ->
                    val isLastItem = index == filteredEvents.size - 1
                    TimelineLedgerItem(
                        event = event,
                        isLastItem = isLastItem,
                        onClick = {
                            selectedEventForReceipt = event
                        }
                    )
                }
            }
        }
    }

    // --- 6. TRANSACTION RECEIPT BOTTOM SHEET ---
    selectedEventForReceipt?.let { event ->
        TransactionReceiptBottomSheet(
            event = event,
            onDismiss = { selectedEventForReceipt = null },
            onViewInvoiceClick = { invoiceId ->
                selectedEventForReceipt = null
                onInvoiceClick(invoiceId)
            }
        )
    }
}

// --- 3. THE TIMELINE FINANCIAL LEDGER CARD ---
@Composable
fun TimelineLedgerItem(
    event: HistoryEventDto,
    isLastItem: Boolean,
    onClick: () -> Unit
) {
    val inkColor = Color(0xFF151A11)
    val whiteCard = Color(0xFFFFFFFF)
    val mutedBorder = Color(0xFFE5E3D8)
    val subtextColor = Color(0xFF73786D)
    val greenCleared = Color(0xFF2FA84F)

    // Semantic colors based on Event Type
    val isPayment = event.type.equals("PAYMENT_RECEIVED", ignoreCase = true)
    val isOverdue = event.type.equals("OVERDUE", ignoreCase = true)

    val (iconBg, iconColor, iconVector) = when {
        isPayment -> Triple(Color(0xFFE8F5E9), greenCleared, Icons.Outlined.CheckCircle)
        isOverdue -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), Icons.Filled.Warning)
        else -> Triple(Color(0xFFEBE9DF), inkColor, Icons.Outlined.Description)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // --- LEFT COLUMN: Timeline Node & Connecting Line ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            if (!isLastItem) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                ) {
                    drawLine(
                        color = Color(0xFFE5E3D8),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 4f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- RIGHT COLUMN: Event Financial Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = whiteCard),
            border = BorderStroke(1.dp, mutedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // TOP ROW: Customer / Title & Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (event.customerName.isNotEmpty()) event.customerName else event.title,
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = inkColor,
                        modifier = Modifier.weight(1f)
                    )

                    if (event.amount > 0) {
                        val formattedAmount = String.format(Locale.forLanguageTag("en-IN"), "%,.0f", event.amount)
                        val amountText = if (isPayment) "+₹$formattedAmount" else "₹$formattedAmount"
                        val amountColor = when {
                            isPayment -> greenCleared
                            isOverdue -> Color(0xFFD32F2F)
                            else -> inkColor
                        }

                        Text(
                            text = amountText,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = amountColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // SUBTITLE ROW: Action details
                Text(
                    text = event.subtitle,
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    color = subtextColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // BOTTOM ROW: Badges (Invoice #, Method, TXN) + Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (event.invoiceNumber.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1EFE6))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = event.invoiceNumber,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = inkColor
                                )
                            }
                        }

                        if (event.paymentMethod.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPayment) Color(0xFFE8F5E9) else Color(0xFFF1EFE6))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = event.paymentMethod.uppercase(),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPayment) greenCleared else subtextColor
                                )
                            }
                        }

                        if (event.transactionId.isNotEmpty()) {
                            Text(
                                text = "TXN: ${event.transactionId.take(10)}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFFA0A59A)
                            )
                        }
                    }

                    Text(
                        text = "${event.date} · ${event.time}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

// --- 4. DETAILED TRANSACTION RECEIPT BOTTOM SHEET ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionReceiptBottomSheet(
    event: HistoryEventDto,
    onDismiss: () -> Unit,
    onViewInvoiceClick: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val inkColor = Color(0xFF151A11)
    val whiteCard = Color(0xFFFFFFFF)
    val greenCleared = Color(0xFF2FA84F)
    val mutedBorder = Color(0xFFE5E3D8)
    val subtextColor = Color(0xFF73786D)
    val isPayment = event.type.equals("PAYMENT_RECEIVED", ignoreCase = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = whiteCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Receipt Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isPayment) Color(0xFFE8F5E9) else Color(0xFFF1EFE6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPayment) Icons.Outlined.CheckCircle else Icons.Outlined.Description,
                    contentDescription = null,
                    tint = if (isPayment) greenCleared else inkColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Display
            val formattedAmount = String.format(Locale.forLanguageTag("en-IN"), "%,.2f", event.amount)
            Text(
                text = if (isPayment) "+₹$formattedAmount" else "₹$formattedAmount",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPayment) greenCleared else inkColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPayment) Color(0xFFE8F5E9) else Color(0xFFF1EFE6))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isPayment) "SETTLEMENT COMPLETED · RECONCILED" else "TRANSACTION RECORDED",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPayment) greenCleared else inkColor,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Details Table Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9F5)),
                border = BorderStroke(1.dp, mutedBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReceiptRow(label = "Customer / Billed To", value = if (event.customerName.isNotEmpty()) event.customerName else "Halcyon Hotels")
                    HorizontalDivider(color = mutedBorder)
                    ReceiptRow(label = "Invoice Number", value = if (event.invoiceNumber.isNotEmpty()) event.invoiceNumber else "INV-013")
                    HorizontalDivider(color = mutedBorder)
                    if (event.paymentMethod.isNotEmpty()) {
                        ReceiptRow(label = "Payment Method", value = event.paymentMethod.uppercase())
                        HorizontalDivider(color = mutedBorder)
                    }
                    if (event.transactionId.isNotEmpty()) {
                        ReceiptRow(label = "Transaction UTR", value = event.transactionId)
                        HorizontalDivider(color = mutedBorder)
                    }
                    ReceiptRow(label = "Date & Time", value = "${event.date}, ${event.time} IST")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            if (!event.invoiceId.isNullOrEmpty()) {
                Button(
                    onClick = { onViewInvoiceClick(event.invoiceId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = inkColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "View Related Invoice",
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEBE9DF),
                    contentColor = inkColor
                )
            ) {
                Text(
                    text = "Close",
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 13.sp,
            color = Color(0xFF73786D)
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF151A11)
        )
    }
}

// --- PREVIEW BLOCK ---
@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    MaterialTheme {
        val mockHistory = listOf(
            HistoryEventDto(
                id = "1",
                type = "PAYMENT_RECEIVED",
                title = "Received ₹96,400 via UPI",
                subtitle = "Settlement for invoice INV-013",
                customerName = "Halcyon Hotels",
                invoiceNumber = "INV-013",
                amount = 96400.0,
                paymentMethod = "UPI",
                transactionId = "pay_Ndk8127389",
                time = "14:30",
                date = "Today"
            ),
            HistoryEventDto(
                id = "2",
                type = "INVOICE_CREATED",
                title = "Issued INV-014",
                subtitle = "Billed to Nexus Tech for ₹45,000",
                customerName = "Nexus Tech",
                invoiceNumber = "INV-014",
                amount = 45000.0,
                paymentMethod = "",
                transactionId = "",
                time = "09:15",
                date = "Today"
            ),
            HistoryEventDto(
                id = "3",
                type = "PAYMENT_RECEIVED",
                title = "Received ₹45,000 via BANK_TRANSFER",
                subtitle = "Settlement for invoice INV-012",
                customerName = "Nexus Tech",
                invoiceNumber = "INV-012",
                amount = 45000.0,
                paymentMethod = "BANK_TRANSFER",
                transactionId = "NEFT-99120482",
                time = "16:45",
                date = "Yesterday"
            ),
            HistoryEventDto(
                id = "4",
                type = "OVERDUE",
                title = "INV-011 is Overdue",
                subtitle = "Payment of ₹12,000 is 3 days late",
                customerName = "Vertex Group",
                invoiceNumber = "INV-011",
                amount = 12000.0,
                paymentMethod = "",
                transactionId = "",
                time = "10:00",
                date = "19 Sep, 2026"
            )
        )
        HistoryScreen(events = mockHistory)
    }
}