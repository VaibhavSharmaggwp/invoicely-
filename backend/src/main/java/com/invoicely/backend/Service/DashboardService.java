package com.invoicely.backend.Service;

import com.invoicely.backend.dto.DashboardSummaryResponse;
import com.invoicely.backend.dto.RecentInvoiceDTO;
import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.enums.InvoiceStatus;
import com.invoicely.backend.repository.InvoiceRepository;
import com.invoicely.backend.repository.PaymentHistoryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final InvoiceRepository invoiceRepository;
    private final PaymentHistoryRepository paymentRepository;

    // 🚀 REDIS CACHE: Results cached per businessId
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard_summary", key = "#businessId")
    public DashboardSummaryResponse getDashboardSummary(UUID businessId) {
        LocalDate today = LocalDate.now();
        LocalDate startMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate startLastMonth = startMonth.minusMonths(1);
        LocalDate endLastMonth = startMonth.minusDays(1);

        // Fetch all invoices for the authenticated business
        List<Invoice> allInvoices = invoiceRepository.findByBusinessId(businessId);

        BigDecimal revenueThisMonth = BigDecimal.ZERO;
        BigDecimal revenueLastMonth = BigDecimal.ZERO;
        BigDecimal receivedAmount = BigDecimal.ZERO;
        BigDecimal outstandingAmount = BigDecimal.ZERO;
        int receivedCount = 0;
        int outstandingCount = 0;
        int overdueCount = 0;

        for (Invoice inv : allInvoices) {
            if (inv.getStatus() == InvoiceStatus.VOID) {
                continue;
            }

            BigDecimal total = inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO;
            LocalDate invDate = inv.getIssueDate() != null ? inv.getIssueDate()
                    : (inv.getCreatedAt() != null ? inv.getCreatedAt().toLocalDate() : today);

            // 1. Monthly Revenue Math
            if (!invDate.isBefore(startMonth) && !invDate.isAfter(endOfMonth)) {
                revenueThisMonth = revenueThisMonth.add(total);
            } else if (!invDate.isBefore(startLastMonth) && !invDate.isAfter(endLastMonth)) {
                revenueLastMonth = revenueLastMonth.add(total);
            }

            // 2. Real Payment & Outstanding Balances across all active invoices
            if (inv.getStatus() == InvoiceStatus.PAID) {
                receivedAmount = receivedAmount.add(total);
                receivedCount++;
            } else if (inv.getStatus() == InvoiceStatus.PARTIALLY_PAID) {
                BigDecimal paid = paymentRepository != null ? paymentRepository.getTotalPaidForInvoice(inv.getId()) : null;
                if (paid == null) paid = BigDecimal.ZERO;
                receivedAmount = receivedAmount.add(paid);
                if (paid.compareTo(BigDecimal.ZERO) > 0) {
                    receivedCount++;
                }

                BigDecimal remaining = total.subtract(paid);
                if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                    outstandingAmount = outstandingAmount.add(remaining);
                    outstandingCount++;
                }
            } else if (inv.getStatus() == InvoiceStatus.OVERDUE) {
                outstandingAmount = outstandingAmount.add(total);
                outstandingCount++;
                overdueCount++;
            } else { // ISSUED, DRAFT (with value), etc.
                outstandingAmount = outstandingAmount.add(total);
                outstandingCount++;
                if (inv.getDueDate() != null && inv.getDueDate().isBefore(today)) {
                    overdueCount++;
                }
            }
        }

        // 3. Dynamic Revenue Growth Calculation
        double growthPercentage = 0.0;
        if (revenueLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = revenueThisMonth.subtract(revenueLastMonth);
            growthPercentage = diff.multiply(BigDecimal.valueOf(100))
                    .divide(revenueLastMonth, 1, java.math.RoundingMode.HALF_UP)
                    .doubleValue();
        } else if (revenueThisMonth.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = 100.0;
        }

        // 4. TOP 5 RECENT INVOICES (Chronologically sorted newest first)
        List<RecentInvoiceDTO> recentDTOs = allInvoices.stream()
                .sorted((a, b) -> {
                    LocalDateTime aTime = a.getCreatedAt() != null ? a.getCreatedAt() :
                            (a.getIssueDate() != null ? a.getIssueDate().atStartOfDay() : LocalDateTime.MIN);
                    LocalDateTime bTime = b.getCreatedAt() != null ? b.getCreatedAt() :
                            (b.getIssueDate() != null ? b.getIssueDate().atStartOfDay() : LocalDateTime.MIN);
                    return bTime.compareTo(aTime);
                })
                .limit(5)
                .map(inv -> RecentInvoiceDTO.builder()
                        .id(inv.getId())
                        .invoiceNumber(inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "INV")
                        .customerName(inv.getCustomer() != null && inv.getCustomer().getName() != null
                                ? inv.getCustomer().getName() : "Customer")
                        .totalAmount(inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO)
                        .status(inv.getStatus() != null ? inv.getStatus().name() : "ISSUED")
                        .build()
                )
                .collect(Collectors.toList());

        // 5. Build and return live analytics payload
        return DashboardSummaryResponse.builder()
                .revenueThisMonth(revenueThisMonth)
                .revenueGrowthPercentage(growthPercentage)
                .receivedAmount(receivedAmount)
                .receivedCount(receivedCount)
                .outstandingAmount(outstandingAmount)
                .outstandingCount(outstandingCount)
                .overdueCount(overdueCount)
                .recentInvoices(recentDTOs)
                .build();
    }
}
