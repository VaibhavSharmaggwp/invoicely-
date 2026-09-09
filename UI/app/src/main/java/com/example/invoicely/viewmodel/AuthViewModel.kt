package com.example.invoicely.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invoicely.network.AuthRequest
import com.example.invoicely.network.RetrofitClient
import com.example.invoicely.security.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(private val tokenManager: TokenManager) : ViewModel() {

    var isLoading = mutableStateOf(false)
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set
    var isSuccess = mutableStateOf(false)
        private set

    fun authenticate(isLoginMode: Boolean, email: String, pass: String, name: String, phone: String = "") {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val request = AuthRequest(
                    email = email.trim(),
                    password = pass,
                    businessName = if (isLoginMode) null else name.trim(),
                    phone = if (isLoginMode) null else phone.trim().ifEmpty { null }
                )

                val response = if (isLoginMode) {
                    RetrofitClient.apiService.login(request)
                } else {
                    RetrofitClient.apiService.register(request)
                }

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()?.token

                    if (token != null) {
                        tokenManager.saveToken(token)
                        isSuccess.value = true
                    } else {
                        errorMessage.value = "Server did not return a token."
                    }
                } else {
                    val errorJson = response.errorBody()?.string()
                    val parsedError = parseBackendError(errorJson)
                    errorMessage.value = parsedError ?: "Authentication failed. Please check your inputs."
                }

            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage ?: "Network error. Is the Spring Boot server running?"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun authenticateWithGoogle(googleToken: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val request = AuthRequest(googleIdToken = googleToken)
                val response = RetrofitClient.apiService.googleLogin(request)

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()?.token
                    if (token != null) {
                        tokenManager.saveToken(token)
                        isSuccess.value = true
                    } else {
                        errorMessage.value = "Server did not return a token."
                    }
                } else {
                    val errorJson = response.errorBody()?.string()
                    val parsedError = parseBackendError(errorJson)
                    errorMessage.value = parsedError ?: "Google authentication failed."
                }
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage ?: "Network error. Is the Spring Boot server running?"
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun parseBackendError(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            if (obj.has("error")) obj.getString("error")
            else if (obj.has("message")) obj.getString("message")
            else null
        } catch (_: Exception) {
            null
        }
    }
}