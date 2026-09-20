package com.example.invoicely.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invoicely.network.RecentInvoiceDto
import com.example.invoicely.network.RetrofitClient
import com.example.invoicely.security.TokenManager
import kotlinx.coroutines.launch

sealed class InvoicesListUiState{
    object Loading: InvoicesListUiState()
    data class Success(val data: List<RecentInvoiceDto>): InvoicesListUiState()
    data class Error(val message: String): InvoicesListUiState()
}

class InvoicesListViewModel(private val tokenManager: TokenManager): ViewModel(){
    var uiState = mutableStateOf<InvoicesListUiState>(InvoicesListUiState.Loading)
        private set

    fun fetchAllInvoices(){
        viewModelScope.launch {
            uiState.value = InvoicesListUiState.Loading
            try{
                val token = tokenManager.getToken() ?: return@launch
                val response = RetrofitClient.apiService.getAllInvoices("Bearer $token")

                if(response.isSuccessful && response.body() != null){
                    uiState.value = InvoicesListUiState.Success(response.body()!!)
                }else{
                    uiState.value = InvoicesListUiState.Error("Failed to fetch ledger")
                }
            }catch (e: Exception){
                uiState.value = InvoicesListUiState.Error("Network Error")
            }
        }
    }
}