package com.example.invoicely.state

import com.example.invoicely.network.DashboardSummaryResponse

// Sealed class ka matlab hai ki in 4 states ke alawa aur koi state exist nahi kar sakti.
// Yeh Compose UI ko batata hai ki screen par exactly kya draw karna hai.

sealed class DashboardUiState{
    // State 1: Shimmer Loading (API call chal rahi hai)
    object Loading: DashboardUiState()

    // State 2: Success (Data mil gaya, ab Bento UI draw karo)
    // Data apne andar 'DashboardSummaryResponse' hold karta hai
    data class Success(val data: DashboardSummaryResponse) : DashboardUiState();

    // State 3: Empty (Naya account hai, abhi tak koi invoice nahi banaya)
    object Empty: DashboardUiState()

    // State 4: Error (Internet band hai, ya backend webhook timeout ho gaya)
    data class Error(val message: String): DashboardUiState()
}