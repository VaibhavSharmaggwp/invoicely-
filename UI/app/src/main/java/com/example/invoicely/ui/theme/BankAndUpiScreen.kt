package com.example.invoicely.ui.theme

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.QrCode
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
import androidx.compose.ui.Alignment
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

data class BankAndUpiState(
    val accountHolderName: String? = "",
    val bankName: String? = "",
    val accountNumber: String? = "",
    val ifscCode: String? = "",
    val upiVpa: String? = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAndUpiScreen(
    initialState: BankAndUpiState = BankAndUpiState(),
    onBackClick: () -> Unit = {},
    onSavePaymentDetails: (BankAndUpiState) -> Unit = {}
) {
    val context = LocalContext.current

    var holderName by remember(initialState.accountHolderName) { mutableStateOf(initialState.accountHolderName ?: "") }
    var bankName by remember(initialState.bankName) { mutableStateOf(initialState.bankName ?: "") }
    var accountNumber by remember(initialState.accountNumber) { mutableStateOf(initialState.accountNumber ?: "") }
    var confirmAccountNum by remember(initialState.accountNumber) { mutableStateOf(initialState.accountNumber ?: "") }
    var ifscCode by remember(initialState.ifscCode) { mutableStateOf(initialState.ifscCode ?: "") }
    var upiVpa by remember(initialState.upiVpa) { mutableStateOf(initialState.upiVpa ?: "") }

    // IFSC validation regex: 4 letters + 0 + 6 alphanumeric
    val ifscPattern = remember { Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$") }
    val isIfscValid = remember(ifscCode) {
        ifscCode.isEmpty() || ifscPattern.matcher(ifscCode).matches()
    }
    val accountsMatch = remember(accountNumber, confirmAccountNum) {
        accountNumber.isNotEmpty() && accountNumber == confirmAccountNum
    }
    val isUpiValid = remember(upiVpa) {
        upiVpa.isEmpty() || (upiVpa.contains("@") && upiVpa.length >= 5)
    }

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
                            text = "Payout & Settlement Rail",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = inkColor,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Auto-injected into invoice memo & QR codes",
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
                            if (holderName.isBlank() || accountNumber.isBlank() || ifscCode.isBlank()) {
                                Toast.makeText(context, "Holder name, Account number & IFSC are mandatory", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!accountsMatch) {
                                Toast.makeText(context, "Account numbers do not match", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!isIfscValid) {
                                Toast.makeText(context, "Invalid IFSC code format", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!isUpiValid) {
                                Toast.makeText(context, "Invalid UPI VPA ID (missing @)", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            onSavePaymentDetails(
                                BankAndUpiState(
                                    accountHolderName = holderName.trim(),
                                    bankName = bankName.trim(),
                                    accountNumber = accountNumber.trim(),
                                    ifscCode = ifscCode.trim().uppercase(),
                                    upiVpa = upiVpa.trim()
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
                            text = "Save Settlement Credentials",
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // --- 1. DYNAMIC BANK CARD PREVIEW ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = inkColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (bankName.isNotBlank()) bankName.uppercase() else "PRIMARY SETTLEMENT ACC",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = chartreuseColor,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "NEFT / RTGS / IMPS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFFA0A59A)
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Text(
                        text = if (accountNumber.isNotBlank()) {
                            if (accountNumber.length > 4) {
                                "•••• •••• •••• " + accountNumber.takeLast(4)
                            } else accountNumber
                        } else "•••• •••• •••• ••••",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "A/C BENEFICIARY",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (holderName.isNotBlank()) holderName else "BENEFICIARY NAME",
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "IFSC ROUTING",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (ifscCode.isNotBlank()) ifscCode.uppercase() else "IFSC CODE",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = chartreuseColor
                            )
                        }
                    }
                }
            }

            // --- 2. BENEFICIARY BANK ACCOUNT INPUTS ---
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
                        text = "BANK ACCOUNT SPECIFICATIONS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = holderName,
                        onValueChange = { holderName = it },
                        label = { Text("Account Holder / Beneficiary Name *", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("As printed in Bank Passbook / Cheque") },
                        leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = inkColor) },
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
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name (Optional)", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("e.g. HDFC Bank, ICICI Bank, SBI") },
                        leadingIcon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = inkColor) },
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
                        value = accountNumber,
                        onValueChange = { accountNumber = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Bank Account Number *", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("e.g. 50100429182049") },
                        leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null, tint = inkColor) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                        value = confirmAccountNum,
                        onValueChange = { confirmAccountNum = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Confirm Account Number *", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("Re-enter bank account number") },
                        leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null, tint = inkColor) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = confirmAccountNum.isNotEmpty() && !accountsMatch,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorder,
                            cursorColor = inkColor,
                            errorBorderColor = alertRed
                        )
                    )

                    if (confirmAccountNum.isNotEmpty() && !accountsMatch) {
                        Text(
                            text = "Account numbers do not match",
                            color = alertRed,
                            fontSize = 11.sp,
                            fontFamily = OutfitFontFamily,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = ifscCode,
                        onValueChange = {
                            if (it.length <= 11) ifscCode = it.uppercase()
                        },
                        label = { Text("11-Digit RTGS/NEFT IFSC Code *", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("e.g. HDFC0001234") },
                        leadingIcon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = inkColor) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isIfscValid && ifscCode.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = inkColor,
                            unfocusedBorderColor = mutedBorder,
                            cursorColor = inkColor,
                            errorBorderColor = alertRed
                        )
                    )
                }
            }

            // --- 3. DIRECT UPI RAIL (Instant Mobile Settlements) ---
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
                        text = "VIRTUAL PAYMENT ADDRESS (UPI)",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtextColor,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = upiVpa,
                        onValueChange = { upiVpa = it },
                        label = { Text("UPI Virtual Payment ID (VPA)", fontFamily = OutfitFontFamily) },
                        placeholder = { Text("e.g. invoicely@okhdfcbank or business@upi") },
                        leadingIcon = { Icon(Icons.Outlined.QrCode, contentDescription = null, tint = inkColor) },
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
fun BankAndUpiScreenPreview() {
    MaterialTheme {
        BankAndUpiScreen()
    }
}
