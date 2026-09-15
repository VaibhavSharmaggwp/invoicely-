package com.example.invoicely.ui.theme

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invoicely.network.InvoiceDetailResponse
import com.example.invoicely.network.LineItemDto
import com.example.invoicely.viewmodel.InvoiceDetailUiState
import java.util.Locale

// 1. THE SCREEN WRAPPER (Handles Loading vs Error vs Success)
@Composable
fun InvoiceDetailScreen(
    uiState: InvoiceDetailUiState,
    onBackClick: () -> Unit,
    onDownloadPdfClick: () -> Unit,
    onRecordPaymentClick: () -> Unit
) {
    val canvasBg = Color(0xFFF6F5EC)
    val inkColor = Color(0xFF151A11)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBg)
    ) {
        when (uiState) {
            is InvoiceDetailUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = inkColor
                )
            }
            is InvoiceDetailUiState.Error -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = uiState.message,
                        color = Color(0xFFD32F2F),
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = inkColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Go Back", color = Color.White, fontFamily = OutfitFontFamily)
                    }
                }
            }
            is InvoiceDetailUiState.Success -> {
                InvoiceDetailSuccessLayout(
                    invoice = uiState.data,
                    onBackClick = onBackClick,
                    onDownloadPdfClick = onDownloadPdfClick,
                    onRecordPaymentClick = onRecordPaymentClick
                )
            }
        }
    }
}

// 2. THE SUCCESS LAYOUT (Premium Digital Paper UI)
@Composable
fun InvoiceDetailSuccessLayout(
    invoice: InvoiceDetailResponse,
    onBackClick: () -> Unit,
    onDownloadPdfClick: () -> Unit,
    onRecordPaymentClick: () -> Unit
) {
    // 🚀 SHARE INTENT SETUP
    val context = LocalContext.current

    val onShareClick = {
        // 1. Create public link
        val shareableLink = "https://invoicely.app/pay/${invoice.id}"
        val shareMessage = "Here is your invoice ${invoice.invoiceNumber} for ₹${String.format(Locale("en", "IN"), "%,.2f", invoice.grandTotal)}. Pay securely here: $shareableLink"

        // 2. Trigger native Android Share Sheet
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Invoice via...")
        context.startActivity(shareIntent)
    }

    // --- DESIGN TOKENS ---
    val canvasBg = Color(0xFFF6F5EC)
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)
    val whiteCard = Color(0xFFFFFFFF)
    val mutedBorder = Color(0xFFE5E3D8)
    val subtextColor = Color(0xFF73786D)

    // Status Colors
    val statusBg = when (invoice.status.uppercase()) {
        "PAID" -> Color(0xFFE8F5E9)
        "OVERDUE" -> Color(0xFFFFEBEE)
        else -> Color(0xFFF1EFE4) // ISSUED
    }
    val statusTextCol = when (invoice.status.uppercase()) {
        "PAID" -> Color(0xFF2FA84F)
        "OVERDUE" -> Color(0xFFD32F2F)
        else -> inkColor // ISSUED
    }

    val outfitFont = OutfitFontFamily
    val jetBrainsFont = JetBrainsMonoFontFamily

    Scaffold(
        containerColor = canvasBg,
        bottomBar = {
            // --- LUXURY STICKY BOTTOM BAR ---
            if (invoice.status.uppercase() != "PAID") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = whiteCard,
                    shadowElevation = 16.dp,
                    border = BorderStroke(1.dp, mutedBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PENDING AMOUNT",
                                fontFamily = outfitFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = subtextColor,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale("en", "IN"), "₹%,.2f", invoice.grandTotal),
                                fontFamily = jetBrainsFont,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = inkColor
                            )
                        }

                        Button(
                            onClick = onRecordPaymentClick,
                            modifier = Modifier
                                .height(54.dp)
                                .padding(start = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = inkColor,
                                contentColor = chartreuseColor
                            )
                        ) {
                            Text(
                                "Record Payment",
                                fontFamily = outfitFont,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- 1. TOP APP BAR ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(whiteCard)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = inkColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Action Icons (Share & Download)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(whiteCard)
                            .clickable { onShareClick() }, // 🚀 Trigger Share Sheet
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = inkColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(whiteCard)
                            .clickable { onDownloadPdfClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Download,
                            contentDescription = "Download",
                            tint = inkColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // --- 2. STATUS HEADER ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(statusBg)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusTextCol)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = invoice.status,
                            fontFamily = jetBrainsFont,
                            color = statusTextCol,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Invoice ${invoice.invoiceNumber}",
                    fontFamily = outfitFont,
                    fontSize = 14.sp,
                    color = subtextColor
                )

                Text(
                    text = String.format(Locale("en", "IN"), "₹%,.2f", invoice.grandTotal),
                    fontFamily = jetBrainsFont,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = inkColor
                )
            }

            // --- 3. DIGITAL RECEIPT CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = whiteCard),
                border = BorderStroke(1.dp, mutedBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {

                    // Client Details
                    Text(
                        "BILLED TO",
                        fontFamily = outfitFont,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        invoice.customerName,
                        fontFamily = outfitFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = inkColor
                    )
                    if (invoice.customerEmail.isNotBlank()) {
                        Text(
                            invoice.customerEmail,
                            fontFamily = outfitFont,
                            fontSize = 13.sp,
                            color = subtextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dates Matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "ISSUE DATE",
                                fontFamily = outfitFont,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                invoice.issueDate,
                                fontFamily = jetBrainsFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = inkColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "DUE DATE",
                                fontFamily = outfitFont,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                invoice.dueDate,
                                fontFamily = jetBrainsFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = inkColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ✂️ PERFORATED CUT LINE
                    Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                        drawLine(
                            color = mutedBorder,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                            strokeWidth = 2f
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- ITEMS TABLE ---
                    Text(
                        "SERVICES & ITEMS",
                        fontFamily = outfitFont,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        invoice.items.forEach { item ->
                            val lineTotal = item.quantity * item.unitPrice
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.description,
                                        fontFamily = outfitFont,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = inkColor
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "${item.quantity} × ₹${String.format(Locale("en", "IN"), "%,.0f", item.unitPrice)}",
                                        fontFamily = jetBrainsFont,
                                        fontSize = 12.sp,
                                        color = subtextColor
                                    )
                                }
                                Text(
                                    text = String.format(Locale("en", "IN"), "₹%,.2f", lineTotal),
                                    fontFamily = jetBrainsFont,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = inkColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ✂️ SECOND PERFORATED LINE
                    Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                        drawLine(
                            color = mutedBorder,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                            strokeWidth = 2f
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- SUBTOTAL & GRAND TOTAL MATH ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontFamily = outfitFont, fontSize = 13.sp, color = subtextColor)
                            Text(
                                String.format(Locale("en", "IN"), "₹%,.2f", invoice.subtotal),
                                fontFamily = jetBrainsFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = inkColor
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("GST Tax", fontFamily = outfitFont, fontSize = 13.sp, color = subtextColor)
                            Text(
                                String.format(Locale("en", "IN"), "+ ₹%,.2f", invoice.taxAmount),
                                fontFamily = jetBrainsFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2FA84F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final Highlighted Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(canvasBg)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "GRAND TOTAL",
                                fontFamily = outfitFont,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = inkColor,
                                letterSpacing = 1.sp
                            )
                            Text(
                                String.format(Locale("en", "IN"), "₹%,.2f", invoice.grandTotal),
                                fontFamily = jetBrainsFont,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = inkColor
                            )
                        }
                    }
                }
            }

            // --- 4. MEMO/NOTES SECTION ---
            if (invoice.memoNotes.isNotBlank()) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Text(
                        "MEMO & INSTRUCTIONS",
                        fontFamily = outfitFont,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        invoice.memoNotes,
                        fontFamily = outfitFont,
                        fontSize = 13.sp,
                        color = subtextColor,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- PREVIEW BLOCK ---
@Preview(showBackground = true)
@Composable
fun InvoiceDetailScreenPreview() {
    MaterialTheme {
        InvoiceDetailSuccessLayout(
            invoice = InvoiceDetailResponse(
                id = "550e8400-e29b-41d4-a716-446655440000",
                invoiceNumber = "INV-2026-891",
                status = "ISSUED", // Change to "PAID" to preview green pill & hidden bottom bar
                customerName = "Acme Studios Pvt Ltd",
                customerEmail = "accounts@acmeco.in",
                issueDate = "16 Sep, 2026",
                dueDate = "01 Oct, 2026",
                items = listOf(
                    LineItemDto("Android Architecture Sprint", 1, 150000.0),
                    LineItemDto("Backend API Setup", 2, 45000.0)
                ),
                subtotal = 240000.0,
                taxAmount = 43200.0,
                grandTotal = 283200.0,
                memoNotes = "Please process the payment to the HDFC Current Account ending in 4918. Thank you for your business!"
            ),
            onBackClick = {},
            onDownloadPdfClick = {},
            onRecordPaymentClick = {}
        )
    }
}