package com.example.invoicely.network

data class AuthRequest(
    val email: String? = null,
    val password: String? = null,
    val businessName: String? = null,
    val phone: String? = null,
    val googleIdToken: String? = null
)

data class AuthResponse(
    val token: String,
    val businessId: String? = null,
    val name: String? = null,
    val email: String? = null
)