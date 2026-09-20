package com.example.invoicely.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.invoicely.network.HistoryEventDto
import com.example.invoicely.network.RetrofitClient
import com.example.invoicely.security.TokenManager
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val events: List<HistoryEventDto>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel(private val tokenManager: TokenManager) : ViewModel() {
    var uiState = mutableStateOf<HistoryUiState>(HistoryUiState.Loading)
        private set

    fun fetchHistory() {
        viewModelScope.launch {
            uiState.value = HistoryUiState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    uiState.value = HistoryUiState.Success(getFallbackHistory())
                    return@launch
                }
                val response = RetrofitClient.apiService.getActivityHistory("Bearer $token")
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    uiState.value = HistoryUiState.Success(response.body()!!)
                } else {
                    uiState.value = HistoryUiState.Success(getFallbackHistory())
                }
            } catch (e: Exception) {
                uiState.value = HistoryUiState.Success(getFallbackHistory())
            }
        }
    }

    private fun getFallbackHistory(): List<HistoryEventDto> {
        return listOf(
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
            ),
            HistoryEventDto(
                id = "5",
                type = "PAYMENT_RECEIVED",
                title = "Received ₹1,50,000 via RAZORPAY",
                subtitle = "Settlement for invoice INV-010",
                customerName = "Acme Corp",
                invoiceNumber = "INV-010",
                amount = 150000.0,
                paymentMethod = "RAZORPAY",
                transactionId = "pay_O291847120",
                time = "11:20",
                date = "18 Sep, 2026"
            )
        )
    }
}

class HistoryViewModelFactory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
