package com.example.invoicely.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.invoicely.security.TokenManager
import com.example.invoicely.viewmodel.CreateInvoiceViewModel
import com.example.invoicely.viewmodel.CreateInvoiceViewModelFactory
import com.example.invoicely.viewmodel.UiLineItem
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CreateInvoiceScreen(
    viewModel: CreateInvoiceViewModel = viewModel(
        factory = CreateInvoiceViewModelFactory(TokenManager(LocalContext.current))
    ),
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    // 1. STATE BINDING FROM VIEWMODEL
    val customerName = viewModel.customerName.value
    val customerEmail = viewModel.customerEmail.value
    val customerAddress = viewModel.customerAddress.value
    val memoNotes = viewModel.memoNotes.value
    val items = viewModel.items
    val selectedTaxRate = viewModel.selectedTaxRate.intValue
    val selectedTermDays = viewModel.selectedTermDays.longValue

    // Financial Metrics computed in ViewModel
    val subtotal = viewModel.subtotal
    val taxAmount = viewModel.taxAmount
    val grandTotal = viewModel.grandTotal

    val taxOptions = listOf(0, 5, 12, 18)
    val termsList = listOf(
        Pair("On Receipt", 0L),
        Pair("Net 7", 7L),
        Pair("Net 15", 15L),
        Pair("Net 30", 30L)
    )

    // Calculated dates
    val todayDateFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.US))
    }
    val calculatedDueDate = remember(selectedTermDays) {
        LocalDate.now().plusDays(selectedTermDays).format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.US))
    }
    val invoiceNumber = remember { "INV-${LocalDate.now().year}-${(100..999).random()}" }

    // Toast Feedback States
    var toastVisible by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var isErrorToast by remember { mutableStateOf(false) }

    // Listen for Network Success
    LaunchedEffect(viewModel.isSuccess.value) {
        if (viewModel.isSuccess.value) {
            isErrorToast = false
            toastMessage = "Invoice $invoiceNumber issued successfully!"
            toastVisible = true
            delay(1200)
            toastVisible = false
            viewModel.resetState()
            onSaveSuccess()
        }
    }

    // Listen for Network Error
    LaunchedEffect(viewModel.errorMessage.value) {
        viewModel.errorMessage.value?.let { error ->
            isErrorToast = true
            toastMessage = error
            toastVisible = true
            delay(3500)
            toastVisible = false
            viewModel.resetState()
        }
    }

    // Design System Tokens
    val canvasBg = Color(0xFFF6F5EC)
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)
    val whiteCard = Color(0xFFFFFFFF)
    val mutedBorder = Color(0xFFE5E3D8)
    val subtextColor = Color(0xFF73786D)
    val chipBg = Color(0xFFEBE9DF)

    Scaffold(
        containerColor = canvasBg,
        bottomBar = {
            // --- LUXURY STICKY SUMMARY BOTTOM BAR ---
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
                            text = "TOTAL PAYABLE",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = subtextColor,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale("en", "IN"), "₹%,.2f", grandTotal),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = inkColor
                        )
                        Text(
                            text = if (selectedTaxRate > 0) "Incl. $selectedTaxRate% GST" else "Zero Tax Applied",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = subtextColor
                        )
                    }

                    Button(
                        onClick = {
                            if (!viewModel.isLoading.value && customerName.isNotBlank() && grandTotal > 0) {
                                viewModel.saveInvoice()
                            }
                        },
                        enabled = !viewModel.isLoading.value && customerName.isNotBlank() && grandTotal > 0,
                        modifier = Modifier
                            .height(54.dp)
                            .padding(start = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = inkColor,
                            contentColor = chartreuseColor,
                            disabledContainerColor = Color(0xFFD6D4C8),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        if (viewModel.isLoading.value) {
                            CircularProgressIndicator(
                                color = chartreuseColor,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Issue Invoice",
                                fontFamily = OutfitFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- 1. TOP APP BAR HEADER ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(whiteCard)
                                .clickable { onBackClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = inkColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Create Invoice",
                                fontFamily = OutfitFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = inkColor
                            )
                            Text(
                                text = "Official billing & statement draft",
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                color = subtextColor
                            )
                        }
                    }

                    // Pulsing Draft Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(inkColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(chartreuseColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DRAFT",
                                fontFamily = JetBrainsMonoFontFamily,
                                color = chartreuseColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                // --- 2. HERO STATEMENT VOUCHER (THE SIGNATURE LIVE MEMO) ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = inkColor),
                    border = BorderStroke(1.dp, Color(0xFF2B3324)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        // Ticket Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "INVOICELY // STATEMENT MEMO",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color.LightGray.copy(alpha = 0.7f)
                            )

                            Text(
                                text = invoiceNumber,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = chartreuseColor
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Client Recipient Row
                        Text(
                            text = "BILLED TO",
                            fontFamily = OutfitFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (customerName.isNotBlank()) customerName else "Client / Business Entity",
                            fontFamily = OutfitFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (customerEmail.isNotBlank()) {
                            Text(
                                text = customerEmail,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = Color.LightGray.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Date Metadata Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "ISSUE DATE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = todayDateFormatted,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "DUE DATE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = calculatedDueDate,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chartreuseColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Perforated Dashed Line Divider
                        Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                            drawLine(
                                color = Color(0xFF2E3827),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                                strokeWidth = 2f
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Items Miniature Snapshot
                        val validItems = items.filter { it.description.isNotBlank() || (it.unitPrice.toDoubleOrNull() ?: 0.0) > 0 }
                        if (validItems.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                validItems.take(3).forEach { item ->
                                    val q = item.quantity.toIntOrNull() ?: 1
                                    val p = item.unitPrice.toDoubleOrNull() ?: 0.0
                                    val lineTotal = q * p
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (item.description.isNotBlank()) item.description else "Line item",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 12.sp,
                                            color = Color.LightGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = String.format(Locale("en", "IN"), "%d × ₹%,.0f = ₹%,.2f", q, p, lineTotal),
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                if (validItems.size > 3) {
                                    Text(
                                        text = "+ ${validItems.size - 3} more items...",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Bottom Total Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "ESTIMATED NET PAYABLE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = String.format(Locale("en", "IN"), "₹%,.2f", grandTotal),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chartreuseColor
                                )
                            }

                            val selectedTermLabel = termsList.firstOrNull { it.second == selectedTermDays }?.first ?: "Net 15"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = selectedTermLabel.uppercase(),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // --- 3. SECTION 01: CLIENT SPECIFICATIONS ---
                BentoSectionCard(
                    stepNumber = "01",
                    title = "CLIENT DETAILS",
                    subtitle = "Designate the billing recipient",
                    icon = Icons.Outlined.Person,
                    inkColor = inkColor
                ) {
                    ProfessionalInputField(
                        value = customerName,
                        onValueChange = { viewModel.customerName.value = it },
                        label = "CLIENT OR COMPANY NAME",
                        placeholder = "e.g. Acme Studios, Halcyon Hotels",
                        leadingIcon = Icons.Outlined.Business,
                        inkColor = inkColor,
                        borderColor = mutedBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ProfessionalInputField(
                        value = customerEmail,
                        onValueChange = { viewModel.customerEmail.value = it },
                        label = "BILLING EMAIL ADDRESS",
                        placeholder = "e.g. accounts@acme.com",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email,
                        inkColor = inkColor,
                        borderColor = mutedBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ProfessionalInputField(
                        value = customerAddress,
                        onValueChange = { viewModel.customerAddress.value = it },
                        label = "BILLING LOCATION / GSTIN (OPTIONAL)",
                        placeholder = "e.g. Mumbai, MH or GSTIN 27AAAPL1234F1Z",
                        leadingIcon = Icons.Outlined.LocationOn,
                        inkColor = inkColor,
                        borderColor = mutedBorder
                    )
                }

                // --- 4. SECTION 02: DELIVERABLES & SERVICES (MULTI-ITEM) ---
                BentoSectionCard(
                    stepNumber = "02",
                    title = "BILLABLE ITEMS & SERVICES",
                    subtitle = "List your deliverables and commercial rates",
                    icon = Icons.Outlined.ReceiptLong,
                    inkColor = inkColor
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        items.forEachIndexed { index, item ->
                            val lineItemQty = item.quantity.toIntOrNull() ?: 1
                            val lineItemPrice = item.unitPrice.toDoubleOrNull() ?: 0.0
                            val lineTotal = lineItemQty * lineItemPrice

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAF7)),
                                border = BorderStroke(1.dp, mutedBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    // Item header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ITEM ${index + 1}",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = subtextColor,
                                            letterSpacing = 1.sp
                                        )

                                        if (items.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.removeItem(index)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.DeleteOutline,
                                                    contentDescription = "Remove item",
                                                    tint = Color(0xFFE53935),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ProfessionalInputField(
                                        value = item.description,
                                        onValueChange = { newDesc ->
                                            viewModel.updateItem(index, item.copy(description = newDesc))
                                        },
                                        label = "SERVICE OR DELIVERABLE",
                                        placeholder = "e.g. Android Architecture & Kotlin Sprint",
                                        leadingIcon = Icons.Outlined.Description,
                                        inkColor = inkColor,
                                        borderColor = mutedBorder
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            ProfessionalInputField(
                                                value = item.quantity,
                                                onValueChange = { newQty ->
                                                    val filtered = newQty.filter { it.isDigit() }
                                                    viewModel.updateItem(index, item.copy(quantity = filtered))
                                                },
                                                label = "QTY",
                                                placeholder = "1",
                                                keyboardType = KeyboardType.Number,
                                                inkColor = inkColor,
                                                borderColor = mutedBorder
                                            )
                                        }

                                        Box(modifier = Modifier.weight(2f)) {
                                            ProfessionalInputField(
                                                value = item.unitPrice,
                                                onValueChange = { newPrice ->
                                                    val filtered = newPrice.filter { it.isDigit() || it == '.' }
                                                    viewModel.updateItem(index, item.copy(unitPrice = filtered))
                                                },
                                                label = "UNIT RATE (₹)",
                                                placeholder = "0.00",
                                                keyboardType = KeyboardType.Decimal,
                                                inkColor = inkColor,
                                                borderColor = mutedBorder
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "Line Total: ",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 12.sp,
                                            color = subtextColor
                                        )
                                        Text(
                                            text = String.format(Locale("en", "IN"), "₹%,.2f", lineTotal),
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = inkColor
                                        )
                                    }
                                }
                            }
                        }

                        // Add Item Outlined Action
                        OutlinedButton(
                            onClick = {
                                viewModel.addItem()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, inkColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = whiteCard,
                                contentColor = inkColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Another Item",
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --- 5. SECTION 03: TAXATION & GST RATE ---
                BentoSectionCard(
                    stepNumber = "03",
                    title = "TAXATION & LEVIES",
                    subtitle = "Select statutory GST or tax exemption rate",
                    icon = Icons.Outlined.Percent,
                    inkColor = inkColor
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        taxOptions.forEach { rate ->
                            val isSelected = selectedTaxRate == rate
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) inkColor else chipBg)
                                    .clickable { viewModel.selectedTaxRate.intValue = rate }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (rate == 0) "0%" else "$rate%",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) chartreuseColor else inkColor
                                    )
                                    Text(
                                        text = if (rate == 0) "Exempt" else "GST",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else subtextColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation breakdown slip
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFAFAF7),
                        border = BorderStroke(1.dp, mutedBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtotal (Services)",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    color = subtextColor
                                )
                                Text(
                                    text = String.format(Locale("en", "IN"), "₹%,.2f", subtotal),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = inkColor
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "GST ($selectedTaxRate%)",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    color = subtextColor
                                )
                                Text(
                                    text = String.format(Locale("en", "IN"), "+ ₹%,.2f", taxAmount),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (taxAmount > 0) Color(0xFF2FA84F) else subtextColor
                                )
                            }
                        }
                    }
                }

                // --- 6. SECTION 04: PAYMENT TERMS & SCHEDULE ---
                BentoSectionCard(
                    stepNumber = "04",
                    title = "PAYMENT SCHEDULE",
                    subtitle = "Set the settlement deadline for this statement",
                    icon = Icons.Outlined.CalendarMonth,
                    inkColor = inkColor
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            termsList.take(2).forEach { (term, days) ->
                                val isSelected = selectedTermDays == days
                                TermCard(
                                    modifier = Modifier.weight(1f),
                                    term = term,
                                    days = days,
                                    isSelected = isSelected,
                                    inkColor = inkColor,
                                    chartreuseColor = chartreuseColor,
                                    onClick = { viewModel.selectedTermDays.longValue = days }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            termsList.drop(2).forEach { (term, days) ->
                                val isSelected = selectedTermDays == days
                                TermCard(
                                    modifier = Modifier.weight(1f),
                                    term = term,
                                    days = days,
                                    isSelected = isSelected,
                                    inkColor = inkColor,
                                    chartreuseColor = chartreuseColor,
                                    onClick = { viewModel.selectedTermDays.longValue = days }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF1EFE4)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = inkColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Settlement Deadline: $calculatedDueDate",
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = inkColor
                            )
                        }
                    }
                }

                // --- 7. SECTION 05: MEMO & PAYMENT INSTRUCTIONS ---
                BentoSectionCard(
                    stepNumber = "05",
                    title = "MEMO & INSTRUCTIONS",
                    subtitle = "Bank information or client thank-you note",
                    icon = Icons.Outlined.Description,
                    inkColor = inkColor
                ) {
                    ProfessionalInputField(
                        value = memoNotes,
                        onValueChange = { viewModel.memoNotes.value = it },
                        label = "PAYMENT INSTRUCTIONS (OPTIONAL)",
                        placeholder = "e.g. Bank: HDFC | A/C: 5010042918 | IFSC: HDFC0001234\nUPI: invoicely@okhdfcbank",
                        singleLine = false,
                        inkColor = inkColor,
                        borderColor = mutedBorder
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // Top Floating Toast Feedback
            AnimatedVisibility(
                visible = toastVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isErrorToast) Color(0xFFD32F2F) else inkColor,
                    shadowElevation = 10.dp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isErrorToast) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (isErrorToast) Color.White else chartreuseColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = toastMessage,
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// --- HELPER COMPONENT: PAYMENT TERM CARD ---
@Composable
private fun TermCard(
    modifier: Modifier = Modifier,
    term: String,
    days: Long,
    isSelected: Boolean,
    inkColor: Color,
    chartreuseColor: Color,
    onClick: () -> Unit
) {
    val termDate = remember(days) {
        LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern("MMM dd", Locale.US))
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) inkColor else Color.White
        ),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) inkColor else Color(0xFFE5E3D8)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = term,
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) chartreuseColor else inkColor
                )
                Text(
                    text = termDate,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = if (isSelected) Color.LightGray else Color.Gray
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(chartreuseColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = inkColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// --- REUSABLE BENTO SECTION WRAPPER ---
@Composable
private fun BentoSectionCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    inkColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E3D8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF6F5EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = inkColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = inkColor
                        )
                        Text(
                            text = subtitle,
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Step Indicator Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1EFE4))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stepNumber,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = inkColor
                    )
                }
            }

            content()
        }
    }
}

// --- REUSABLE FINTECH INPUT FIELD ---
@Composable
private fun ProfessionalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    inkColor: Color,
    borderColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = Color(0xFF6E7468),
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    color = Color.Gray.copy(alpha = 0.5f)
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = inkColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            singleLine = singleLine,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = inkColor,
                unfocusedTextColor = inkColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFFAFAF7),
                focusedBorderColor = inkColor,
                unfocusedBorderColor = borderColor,
                cursorColor = inkColor
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F5EC)
@Composable
fun CreateInvoiceScreenPreview() {
    MaterialTheme {
        CreateInvoiceScreen(
            onBackClick = {},
            onSaveSuccess = {}
        )
    }
}