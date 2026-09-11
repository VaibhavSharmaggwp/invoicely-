package com.invoicely.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    // 1. Hero Card Math
    private BigDecimal revenueThisMonth;
    private Double revenueGrowthPercentage;

    // 2. Pair Card Math (Received vs Outstanding)
    private BigDecimal receivedAmount;
    private Integer receivedCount;
    private BigDecimal outstandingAmount;
    private Integer outstandingCount;

    // 3. Status Math
    private Integer overdueCount;

    // 4. List Data
    private List<RecentInvoiceDTO> recentInvoices;
}
