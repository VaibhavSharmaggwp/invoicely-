package com.example.invoicely.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invoicely.network.BusinessProfileDto
import com.example.invoicely.network.RetrofitClient
import com.example.invoicely.security.TokenManager
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: BusinessProfileDto) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class SettingsViewModel(private val tokenManager: TokenManager) : ViewModel() {
    var uiState = mutableStateOf<ProfileUiState>(ProfileUiState.Loading)

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            val token = tokenManager.getToken()
            if (token.isNullOrBlank()) {
                // If user is not yet logged in or token is not set, initialize with default empty profile
                // so the UI never gets stuck in a permanent loading state!
                if (uiState.value !is ProfileUiState.Success) {
                    uiState.value = ProfileUiState.Success(BusinessProfileDto())
                }
                return@launch
            }

            uiState.value = ProfileUiState.Loading
            try {
                val response = RetrofitClient.apiService.getBusinessProfile("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    uiState.value = ProfileUiState.Success(response.body()!!)
                } else {
                    // Agar profile exist nahi karti (new user), toh empty DTO dikhao
                    uiState.value = ProfileUiState.Success(BusinessProfileDto())
                }
            } catch (e: Exception) {
                // Keep existing profile if already loaded, otherwise default to empty DTO so user can edit form
                val current = (uiState.value as? ProfileUiState.Success)?.profile ?: BusinessProfileDto()
                uiState.value = ProfileUiState.Success(current)
            }
        }
    }

    // 🚀 THE SAVE FUNCTION
    suspend fun updateProfile(updatedProfile: BusinessProfileDto): Boolean {
        return try {
            val token = tokenManager.getToken() ?: return false
            val response =
                RetrofitClient.apiService.updateBusinessProfile("Bearer $token", updatedProfile)

            if (response.isSuccessful) {
                // UI state ko naye data se update karo
                uiState.value = ProfileUiState.Success(updatedProfile)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }
}