package com.example.invoicely.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.invoicely.network.InvoiceDetailResponse
import com.example.invoicely.network.RecordPaymentRequest
import com.example.invoicely.network.RetrofitClient
import com.example.invoicely.security.TokenManager
import kotlinx.coroutines.launch

sealed class InvoiceDetailUiState {
    object Loading : InvoiceDetailUiState()
    data class Success(val data: InvoiceDetailResponse) : InvoiceDetailUiState()
    data class Error(val message: String) : InvoiceDetailUiState()
}

class InvoiceDetailViewModel(private val tokenManager: TokenManager): ViewModel() {
    var uistate = mutableStateOf<InvoiceDetailUiState>(InvoiceDetailUiState.Loading)

    // 2. FETCH DATA FUNCTION
    fun fetchInvoiceDetails(invoiceId: String) {
        viewModelScope.launch {
            uistate.value = InvoiceDetailUiState.Loading
            try {
                val token = tokenManager.getToken()
                if (token == null) {
                    uistate.value = InvoiceDetailUiState.Error("Session expired. Please log in again.")
                    return@launch
                }

                val response = RetrofitClient.apiService.getInvoiceDetails("Bearer $token", invoiceId)
                if (response.isSuccessful && response.body() != null) {
                    // Data mil gaya! Success state update karo
                    uistate.value = InvoiceDetailUiState.Success(response.body()!!)
                } else {
                    uistate.value = InvoiceDetailUiState.Error("Failed to load invoice: ${response.code()}")
                }
            } catch (e: Exception) {
                uistate.value = InvoiceDetailUiState.Error("Network Error. Check your connection.")
            }
        }
    }

    // 🚀 The REAL network call for the payment sheet
    // We return a Boolean so the Bottom Sheet knows whether to play the Success Lottie or not
    suspend fun submitPayment(invoiceId: String, amount: Double, method: String): Boolean {
        return try {
            val token = tokenManager.getToken()
            if (token == null) {
                android.util.Log.e("InvoiceDetailVM", "Payment submission failed: tokenManager.getToken() is null")
                return false
            }

            android.util.Log.d("InvoiceDetailVM", "Submitting payment: invoiceId=$invoiceId, amount=$amount, method=$method")
            val request = RecordPaymentRequest(amount, method)
            val response = RetrofitClient.apiService.recordPayment("Bearer $token", invoiceId, request)

            if (response.isSuccessful) {
                android.util.Log.d("InvoiceDetailVM", "Payment API success: 200 OK")
                true // Return true to trigger the Lottie animation
            } else {
                android.util.Log.e("InvoiceDetailVM", "Payment API failed with code ${response.code()}: ${response.errorBody()?.string()}")
                false // API failed
            }
        } catch (e: Exception) {
            android.util.Log.e("InvoiceDetailVM", "Network or parsing exception during submitPayment", e)
            false // Network crashed
        }
    }
}

// 3. FACTORY
class InvoiceDetailViewModelFactory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoiceDetailViewModel(tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}