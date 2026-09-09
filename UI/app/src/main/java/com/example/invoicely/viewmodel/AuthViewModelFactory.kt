package com.example.invoicely.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.invoicely.security.TokenManager


// Yeh factory Android ko batati hai ki AuthViewModel ko kaise instantiate karna hai
// Yeh factory Android ko batati hai ki AuthViewModel ko kaise instantiate karna hai
class AuthViewModelFactory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check karte hain ki kya Android actually AuthViewModel maang raha hai
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Hum apna TokenManager pass karke naya instance return kar dete hain
            return AuthViewModel(tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}