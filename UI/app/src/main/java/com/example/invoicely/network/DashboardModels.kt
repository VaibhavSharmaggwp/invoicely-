package com.example.invoicely.network

import com.google.gson.annotations.SerializedName

// 1. DASHBOARD SUMMARY RESPONSE
// Yeh main object hai jo Spring Boot return karega. Isme dashboard ka saara math hoga.

data class DashboardSummaryResponse(
    val revenueThisMonth: Double,   // e.g., 384600.00
    val revenueGrowthPercentage: Double, // e.g., 12.5
    val receivedAmount: Double,  // e.g., 268200.00
    val receivedCount: Int,  // e.g., 12
    val outstandingAmount: Double, // e.g., 126000.00
    val outstandingCount: Int, // e.g 5 due
    val overdueCount: Int, // e.g., 2 late
    @SerializedName("recentInvoices")
    val recentInvoices: List<RecentInvoiceDto> = emptyList()  // Bottom list ke liye data
) {
    // Backward compatibility property for existing UI calls
    val recentInvoice: List<RecentInvoiceDto>
        get() = recentInvoices
}

// 2. RECENT INVOICE DTO
// Dashboard par poora invoice nahi chahiye, sirf list row dikhane ke liye zaroori details.
data class RecentInvoiceDto(
    val id: String, // uuid from backend
    val invoiceNumber: String, // eg INV - 332
    val customerName: String,   // e.g., "Halcyon Hotels"
    val totalAmount: Double,   // e.g., 96400.00
    val status: String  // e.g., "Paid", "Due", "Overdue"
)