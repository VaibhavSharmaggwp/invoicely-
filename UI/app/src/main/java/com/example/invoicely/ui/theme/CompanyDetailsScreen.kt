package com.example.invoicely.ui.theme

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

data class CompanyProfileState(
    val legalEntityName: String? = "",
    val tradeName: String? = "",
    val gstin: String? = "",
    val contactEmail: String? = "",
    val contactPhone: String? = "",
    val registeredAddress: String? = "",
    val stateName: String? = "Himachal Pradesh",
    val pinCode: String? = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreen(
    initialState: CompanyProfileState = CompanyProfileState(),
    onBackClick: () -> Unit = {},
    onSaveProfile: (CompanyProfileState) -> Unit = {}
) {
    val context = LocalContext.current
    // Local form state
    var legalName by remember(initialState.legalEntityName) { mutableStateOf(initialState.legalEntityName ?: "") }
    var tradeName by remember(initialState.tradeName) { mutableStateOf(initialState.tradeName ?: "") }
    var gstin by remember(initialState.gstin) { mutableStateOf(initialState.gstin ?: "") }
    var email by remember(initialState.contactEmail) { mutableStateOf(initialState.contactEmail ?: "") }
    var phone by remember(initialState.contactPhone) { mutableStateOf(initialState.contactPhone ?: "") }
    var address by remember(initialState.registeredAddress) { mutableStateOf(initialState.registeredAddress ?: "") }
    var pinCode by remember(initialState.pinCode) { mutableStateOf(initialState.pinCode ?: "") }

    // GSTIN Validation (Indian Statutory Standard: 2 digits + 10 PAN chars + 1 entity + Z + 1 check digit)
    val gstinPattern = remember {
        Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")
    }

    val isGstinValid = remember(gstin) {
        gstin.isEmpty() || gstinPattern.matcher(gstin).matches()
    }

    // Design Tokens
    val canvasBg = Color(0xFFF6F5EC)
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)
    val whiteCard = Color(0xFFFFFFFF)
    val mutedBorder = Color(0xFFE5E3D8)
    val subtextColor = Color(0xFF73786D)
    val alertRed = Color(0xFFD32F2F)

    Scaffold(
        containerColor = canvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Company Profile",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = inkColor,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Legal entity & statutory tax filing details",
                            fontFamily = OutfitFontFamily,
                            color = subtextColor,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = inkColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = canvasBg)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteCard,
                border = BorderStroke(1.dp, mutedBorder),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (legalName.isBlank()) {
                                Toast.makeText(context, "Legal Business Name is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!isGstinValid) {
                                Toast.makeText(context, "Please enter a valid 15-character GSTIN", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onSaveProfile(
                                CompanyProfileState(
                                    legalEntityName = legalName.trim(),
                                    tradeName = if (tradeName.isNotBlank()) tradeName.trim() else legalName.trim(),
                                    gstin = gstin.trim().uppercase(),
                                    contactEmail = email.trim(),
                                    contactPhone = phone.trim(),
                                    registeredAddress = address.trim(),
                                    pinCode = pinCode.trim()
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = inkColor,
                            contentColor = chartreuseColor
                        )
                    ) {
                        Text(
                            text = "Save Profile & Update Invoices",
                            fontFamily = OutfitFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: LEGAL IDENTITY ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = whiteCard),
                border = BorderStroke(1.dp, mutedBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "LEGAL IDENTIFICATION",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = legalName,
                        onValueChange = { legalName = it },
                        label = { Text("Registered Business / Entity Name *", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("e.g. Acme Tech Solutions Private Limited") },
                        leadingIcon = { Icon(Icons.Outlined.Apartment, contentDescription = null, tint = inkColor) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorder,
                            cursorColor = inkColor
                        )
                    )

                    OutlinedTextField(
                        value = tradeName,
                        onValueChange = { tradeName = it },
                        label = { Text("Trade Name / Brand (Optional)", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("e.g. Acme Studio") },
                        leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = inkColor) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorder,
                            cursorColor = inkColor
                        )
                    )

                    Column {
                        OutlinedTextField(
                            value = gstin,
                            onValueChange = {
                                if (it.length <= 15) gstin = it.uppercase()
                            },
                            label = { Text("GSTIN (Tax Registration ID)", fontFamily = OutfitFontFamily) },
                            placeholder = { Text("e.g. 02AAACA1234A1Z5") },
                            leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null, tint = inkColor) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            modifier = Modifier.fillMaxWidth(),
                            isError = !isGstinValid && gstin.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = inkColor,
                                unfocusedBorderColor = mutedBorder,
                                cursorColor = inkColor,
                                errorBorderColor = alertRed
                            )
                        )
                        if (!isGstinValid && gstin.isNotEmpty()) {
                            Text(
                                text = "Invalid statutory GSTIN structure (15 characters alphanumeric required)",
                                color = alertRed,
                                fontSize = 11.sp,
                                fontFamily = OutfitFontFamily,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }
                }
            }

            // --- SECTION 2: COMMUNICATIONS & BILLING ADDRESS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = whiteCard),
                border = BorderStroke(1.dp, mutedBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "COMMUNICATION & TAX DISPATCH",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Official Billing Email", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("billing@acmetech.in") },
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = inkColor) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorder,
                            cursorColor = inkColor
                        )
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Business Phone", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("9876543210") },
                        leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = inkColor) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorder,
                            cursorColor = inkColor
                        )
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Registered Physical Office Address", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("Unit 4B, The Mall Road...") },
                        leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = inkColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorder,
                            cursorColor = inkColor
                        )
                    )

                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { if (it.length <= 6) pinCode = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Postal PIN Code", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("171001") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorder,
                            cursorColor = inkColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Preview
@Composable
fun CompanyDetailsScreenPreview() {
    MaterialTheme {
        CompanyDetailsScreen()
    }
}