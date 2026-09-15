package com.example.invoicely.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.invoicely.network.InvoiceDetailResponse
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