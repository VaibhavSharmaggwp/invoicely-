package com.example.invoicely.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.invoicely.network.RetrofitClient
import com.example.invoicely.security.TokenManager
import com.example.invoicely.state.DashboardUiState
import kotlinx.coroutines.launch

class DashboardViewModel(private val tokenManager: TokenManager): ViewModel(){
    // 1. UI STATE
    // By default, state ko 'Loading' rakha hai taaki shimmer effect sabse pehle dikhe
    var uiState = mutableStateOf<DashboardUiState>(DashboardUiState.Loading)
        private set

    // INIT BLOCK
    // Jaise hi yeh ViewModel memory me banega, yeh block automatically chal jayega
    init {
        fetchDashboardData()
    }

    // 3. THE NETWORK CALL
    fun fetchDashboardData(){
        viewModelScope.launch {
            // UI ko Loading state me daalo (in case of manual refresh)
            uiState.value = DashboardUiState.Loading

            try{
                // TokenManager se encrypted JWT nikaalo
                val token = tokenManager.getToken()
                if(token == null){
                    uiState.value = DashboardUiState.Error("Session expired. Please log in again")
                    return@launch // stop the call here
                }
                // Spring Boot requires "Bearer " before the actual token
                val authHeader = "Bearer $token"

                // API hit karo
                val response = RetrofitClient.apiService.getDashboardSummary(authHeader)

                if(response.isSuccessful && response.body() != null){
                    val data = response.body()!!

                    // Agar account naya hai aur ek bhi invoice nahi hai, toh Empty state dikhao
                    if(data.revenueThisMonth == 0.0
                        && data.recentInvoices.isEmpty()){
                        uiState.value = DashboardUiState.Empty
                    }else{
                        // Data mil gaya, Success state me data pass karo!
                        uiState.value = DashboardUiState.Success(data)
                    }

                    }else{
                        uiState.value = DashboardUiState.Error("Error: ${response.code()}")
                }
            }catch (e: Exception){
                // Agar Spring Boot server band hai ya internet nahi chal raha
                uiState.value = DashboardUiState.Error("Network Error. Check your connection.")
        }
        }
    }
}