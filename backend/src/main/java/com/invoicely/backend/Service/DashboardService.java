package com.invoicely.backend.Service;


import com.invoicely.backend.dto.DashboardSummaryResponse;
import com.invoicely.backend.dto.RecentInvoiceDTO;
import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.enums.InvoiceStatus;
import com.invoicely.backend.repository.InvoiceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final InvoiceRepository invoiceRepository;

    // 🚀 REDIS CACHE: Yeh annotation result ko Redis me save kar legi.
    // Jab tak koi naya invoice nahi banta, yeh DB hit nahi karega!
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard_summary", key = "#businessId")
    public DashboardSummaryResponse getDashboardSummary(UUID businessId){

        // 1. Current month ki dates nikaalo (e.g., Sept 1 to Sept 30)
        LocalDate startMonth  = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // 2. Is month ke saare invoices fetch karo
        List<Invoice> monthlyInvoices =
                invoiceRepository.findByBusinessIdAndIssueDateBetween(businessId, startMonth, endOfMonth);


        // 3. MATH CALCULATIONS
        BigDecimal revenueThisMonth = BigDecimal.ZERO;
        BigDecimal receivedAmount = BigDecimal.ZERO;
        BigDecimal outstandingAmount = BigDecimal.ZERO;
        int receivedCount = 0;
        int outstandingCount = 0;
        int overdueCount = 0;

        for(Invoice inv: monthlyInvoices){
            // Draft ko revenue me count nahi karte
            if(inv.getStatus() != InvoiceStatus.DRAFT && inv.getStatus() != InvoiceStatus.VOID){
                revenueThisMonth = revenueThisMonth.add(inv.getTotalAmount());

                if(inv.getStatus() == InvoiceStatus.PAID){
                    receivedAmount = receivedAmount.add(inv.getTotalAmount());
                    receivedCount++;
                }else if(inv.getStatus() == InvoiceStatus.ISSUED || inv.getStatus() == InvoiceStatus.PARTIALLY_PAID){
                    // For simplicity right now, outstanding is full amount if not paid.
                    // (Later we can subtract partial payments here)
                    outstandingAmount = outstandingAmount.add(inv.getTotalAmount());
                    outstandingCount++;
                } else if (inv.getStatus() == InvoiceStatus.OVERDUE) {
                    overdueCount++;
                }
            }
        }

        // 4. TOP 5 RECENT INVOICES FETCH KARO
        List<Invoice> recent = invoiceRepository.findTop5ByBusinessIdOrderByIssueDateDesc(businessId);

        List<RecentInvoiceDTO> recentDTOs = recent.stream().map(inv -> RecentInvoiceDTO.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .customerName(inv.getCustomer() != null ? inv.getCustomer().getName() : "Unknown")
                .totalAmount(inv.getTotalAmount())
                .status(inv.getStatus() != null ? inv.getStatus().name() : "")
                .build()
        ).collect(Collectors.toList());

        // 5. RESPONSE BUILD KARO
        return DashboardSummaryResponse.builder()
                .revenueThisMonth(revenueThisMonth)
                .revenueGrowthPercentage(15.5) // Hardcoded for now, you can add Last Month math later
                .receivedAmount(receivedAmount)
                .receivedCount(receivedCount)
                .outstandingAmount(outstandingAmount)
                .outstandingCount(outstandingCount)
                .overdueCount(overdueCount)
                .recentInvoices(recentDTOs)
                .build();

    }

}
