package com.invoicely.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Serializable zaroori hai taaki Redis is object ko store kar sake
public class DashboardSummaryDTO {
    private BigDecimal totalOutstanding;
    private BigDecimal dueThisWeek;
    private long pendingInvoicesCount;
    private long overdueInvoiceCount;
}
