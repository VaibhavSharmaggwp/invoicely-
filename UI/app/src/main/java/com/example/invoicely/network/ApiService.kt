package com.example.invoicely.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/v1/auth/google")
    suspend fun googleLogin(@Body request: AuthRequest): Response<AuthResponse>

    @GET("/api/v1/dashboard/summary")
    suspend fun getDashboardSummary(
        @Header("Authorization") authHeader: String
    ): Response<DashboardSummaryResponse>

    // Creating new invoice endpoint
    @POST("/api/v1/invoices")
    suspend fun createInvoice(
        @Header("Authorization") authHeader: String,
        @Body request: CreateInvoiceRequest
    ): Response<Unit> // 'Unit' means hume return me koi JSON nahi chahiye, bas 200 OK status chahiye

    @GET("/api/v1/invoices/{id}")
    suspend fun getInvoiceDetails(
        @Header("Authorization") authHeader: String,
        @Path("id") invoiceId: String
    ): Response<InvoiceDetailResponse>

    @POST("/api/v1/invoices/{id}/payments")
    suspend fun recordPayment(
        @Header("Authorization") authHeader: String,
        @Path("id") invoiceId: String,
        @Body request: RecordPaymentRequest
    ): Response<Unit>
}