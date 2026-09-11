package com.example.invoicely.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/v1/auth/google")
    suspend fun googleLogin(@Body request: AuthRequest): Response<AuthResponse>

    @GET("api/v1/dashboard/summary")
    suspend fun getDashboardSummary(@Header("Authorization") token: String): Response<DashboardSummaryResponse>
}