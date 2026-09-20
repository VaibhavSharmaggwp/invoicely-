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
                if (token.isNullOrBlank()) {
                    uiState.value = HistoryUiState.Error("Session expired. Please log in.")
                    return@launch
                }
                val response = RetrofitClient.apiService.getActivityHistory("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    uiState.value = HistoryUiState.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to fetch ledger activity"
                    uiState.value = HistoryUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                uiState.value = HistoryUiState.Error("Network error: ${e.localizedMessage ?: "Unable to connect to server"}")
            }
        }
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
