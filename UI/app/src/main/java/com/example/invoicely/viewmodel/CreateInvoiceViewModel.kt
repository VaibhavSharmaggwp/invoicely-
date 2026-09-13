package com.example.invoicely.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.invoicely.network.CreateInvoiceRequest
import com.example.invoicely.network.LineItemDto
import com.example.invoicely.network.RetrofitClient
import com.example.invoicely.security.TokenManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// We need an internal data class for the UI to handle the String inputs before parsing
data class UiLineItem(
    val description: String = "",
    val quantity: String = "1",
    val unitPrice: String = ""
)

class CreateInvoiceViewModel(private val tokenManager: TokenManager) : ViewModel() {
    // 1. FORM STATE (Clean reactive states for Composable binding)
    var customerName = mutableStateOf("")
    var customerEmail = mutableStateOf("")
    var customerAddress = mutableStateOf("")
    var memoNotes = mutableStateOf("")

    // For lists in Jetpack Compose ViewModels, we use mutableStateListOf
    var items = mutableStateListOf<UiLineItem>()

    var selectedTaxRate = mutableIntStateOf(18)
    var selectedTermDays = mutableLongStateOf(15L) // Default Net 15

    init {
        if (items.isEmpty()) {
            items.add(UiLineItem())
        }
    }

    // 2. DERIVED FINANCIAL METRICS
    val subtotal: Double
        get() = items.sumOf {
            val q = it.quantity.toIntOrNull() ?: 1
            val p = it.unitPrice.toDoubleOrNull() ?: 0.0
            q * p
        }

    val taxAmount: Double
        get() = subtotal * (selectedTaxRate.intValue / 100.0)

    val grandTotal: Double
        get() = subtotal + taxAmount

    // 3. NETWORK STATE
    var isLoading = mutableStateOf(false)
        private set

    var isSuccess = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun resetState() {
        isSuccess.value = false
        errorMessage.value = null
        isLoading.value = false
    }

    // 4. HELPER FUNCTIONS (To update the list from the UI)
    fun updateItem(index: Int, newItem: UiLineItem) {
        if (index in items.indices) {
            items[index] = newItem
        }
    }

    fun addItem() {
        items.add(UiLineItem())
    }

    fun removeItem(index: Int) {
        if (items.size > 1 && index in items.indices) {
            items.removeAt(index)
        }
    }

    // 5. THE SUBMIT ACTION
    fun saveInvoice() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    errorMessage.value = "Authentication error. Please log in again."
                    return@launch
                }

                if (customerName.value.isBlank()) {
                    errorMessage.value = "Customer name is required"
                    return@launch
                }

                // Convert UI String items to strictly typed Network DTOs
                val lineItemDtos = items.map {
                    LineItemDto(
                        description = it.description.trim(),
                        quantity = it.quantity.toIntOrNull() ?: 1,
                        unitPrice = it.unitPrice.toDoubleOrNull() ?: 0.0
                    )
                }.filter { it.description.isNotBlank() || it.unitPrice > 0.0 } // Ignore empty rows

                if (lineItemDtos.isEmpty()) {
                    errorMessage.value = "Please add at least one line item"
                    return@launch
                }

                // Calculate Date
                val calculatedDueDate = LocalDate.now().plusDays(selectedTermDays.longValue)
                    .format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.US))

                // Build the payload
                val request = CreateInvoiceRequest(
                    customerName = customerName.value.trim(),
                    customerEmail = customerEmail.value.trim(),
                    customerAddress = customerAddress.value.trim(),
                    items = lineItemDtos,
                    taxRate = selectedTaxRate.intValue,
                    dueDate = calculatedDueDate,
                    memoNotes = memoNotes.value.trim()
                )

                // Fire the API Call
                val response = RetrofitClient.apiService.createInvoice("Bearer $token", request)

                if (response.isSuccessful) {
                    isSuccess.value = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage.value = if (!errorBody.isNullOrBlank()) {
                        "Server Error: $errorBody"
                    } else {
                        "Failed to create invoice (Code: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                errorMessage.value = "Network Error: ${e.localizedMessage ?: "Could not connect to server."}"
            } finally {
                isLoading.value = false
            }
        }
    }
}

// 6. FACTORY
class CreateInvoiceViewModelFactory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateInvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateInvoiceViewModel(tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}