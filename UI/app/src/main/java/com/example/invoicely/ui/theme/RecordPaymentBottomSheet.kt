package com.example.invoicely.ui.theme

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.invoicely.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 1. PAYMENT STATES (State Machine for the Bottom Sheet)
enum class PaymentSheetState {
    INPUT,       // User is typing the amount
    PROCESSING,  // Network call is happening (Lottie plays here)
    SUCCESS      // Payment recorded! (Success Lottie plays)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentBottomSheet(
    invoiceTotal: Double,
    onDismiss: () -> Unit,
    onPaymentSaved: (amount: Double, method: String) -> Unit = { _, _ -> },
    onConfirmPayment: (suspend (amount: Double, method: String) -> Boolean)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentState by remember { mutableStateOf(PaymentSheetState.INPUT) }
    val coroutineScope = rememberCoroutineScope()

    // Preload Lottie Composition immediately when bottom sheet opens
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.payment_success))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = currentState == PaymentSheetState.SUCCESS,
        restartOnPlay = true,
        speed = 1.25f,
        iterations = 1
    )

    // Form State
    var amountText by remember { mutableStateOf(invoiceTotal.toString()) } // Default to full amount
    var selectedMethod by remember { mutableStateOf("UPI") }
    val paymentMethods = listOf("UPI", "Bank Transfer", "Cash")
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Design Tokens
    val inkColor = Color(0xFF151A11)
    val chartreuseColor = Color(0xFFDCEF3C)
    val canvasBg = Color(0xFFF6F5EC)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = canvasBg,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        // We use Crossfade so transitioning between Input and Success is buttery smooth
        Crossfade(targetState = currentState, label = "sheet_state") { state ->
            when (state) {
                PaymentSheetState.INPUT -> {
                    // --- THE INPUT FORM ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Record Payment",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = inkColor
                        )

                        // 1. Amount Field
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { 
                                amountText = it 
                                errorMessage = null
                            },
                            label = { Text("Amount Received (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = inkColor,
                                cursorColor = inkColor
                            )
                        )

                        // 2. Payment Method Chips
                        Text("PAYMENT METHOD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            paymentMethods.forEach { method ->
                                val isSelected = selectedMethod == method
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) inkColor else Color.White)
                                        .border(1.dp, if (isSelected) inkColor else Color.LightGray, RoundedCornerShape(12.dp))
                                        .clickable { 
                                            selectedMethod = method 
                                            errorMessage = null
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = method,
                                        color = if (isSelected) chartreuseColor else inkColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Error Message (if API failed)
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFD32F2F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        val isAmountValid = parsedAmount > 0.0
                        val isButtonEnabled = currentState == PaymentSheetState.INPUT && isAmountValid

                        // 3. Submit Button
                        Button(
                            onClick = {
                                if (!isButtonEnabled) return@Button
                                errorMessage = null
                                coroutineScope.launch {
                                    currentState = PaymentSheetState.PROCESSING
                                    val isSuccess = if (onConfirmPayment != null) {
                                        onConfirmPayment(parsedAmount, selectedMethod)
                                    } else {
                                        delay(1500) // Fake 1.5 second API call
                                        true
                                    }

                                    if (isSuccess) {
                                        currentState = PaymentSheetState.SUCCESS
                                        delay(2400) // Let user see the full Lottie success checkmark
                                        onPaymentSaved(parsedAmount, selectedMethod)
                                        onDismiss()
                                    } else {
                                        currentState = PaymentSheetState.INPUT
                                        errorMessage = "Failed to record payment. Please check your network and try again."
                                    }
                                }
                            },
                            enabled = isButtonEnabled,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = inkColor,
                                contentColor = chartreuseColor,
                                disabledContainerColor = Color(0xFFE0E0E0),
                                disabledContentColor = Color.Gray
                            )
                        ) {
                            Text("Confirm Payment", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(32.dp)) // Safe area for navigation bar
                    }
                }

                PaymentSheetState.PROCESSING -> {
                    // --- THE PROCESSING STATE ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = inkColor,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Updating Ledger...", 
                            color = inkColor, 
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Recording transaction & updating invoice",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }

                PaymentSheetState.SUCCESS -> {
                    // --- THE SUCCESS LOTTIE STATE ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.size(160.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Payment Recorded!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = inkColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The dashboard & ledger have been updated.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// PREVIEW BLOCK
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun RecordPaymentPreview() {
    MaterialTheme {
        RecordPaymentBottomSheet(
            invoiceTotal = 283200.0,
            onDismiss = {},
            onPaymentSaved = { _, _ -> }
        )
    }
}