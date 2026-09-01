package com.invoicely.backend.Service;

import com.invoicely.backend.entity.Business;
import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.repository.BusinessRepository;
import com.invoicely.backend.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private UUID businessId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        userEmail = "test@business.com";
    }

    @Test
    void testGetInvoicesForBusiness_Pagination() {
        Invoice invoice1 = Invoice.builder().id(UUID.randomUUID()).invoiceNumber("INV-1").build();
        Invoice invoice2 = Invoice.builder().id(UUID.randomUUID()).invoiceNumber("INV-2").build();
        Page<Invoice> mockPage = new PageImpl<>(List.of(invoice1, invoice2));

        when(invoiceRepository.findByBusinessId(eq(businessId), any(Pageable.class))).thenReturn(mockPage);

        Page<Invoice> result = invoiceService.getInvoicesForBusiness(businessId, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(invoiceRepository).findByBusinessId(eq(businessId), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals("createdAt: DESC", pageable.getSort().toString());
    }

    @Test
    void testGetInvoicesForUser_Pagination() {
        Business business = Business.builder().id(businessId).email(userEmail).build();
        Invoice invoice = Invoice.builder().id(UUID.randomUUID()).invoiceNumber("INV-100").build();
        Page<Invoice> mockPage = new PageImpl<>(List.of(invoice));

        when(businessRepository.findByEmail(userEmail)).thenReturn(Optional.of(business));
        when(invoiceRepository.findByBusinessId(eq(businessId), any(Pageable.class))).thenReturn(mockPage);

        Page<Invoice> result = invoiceService.getInvoicesForUser(userEmail, 1, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(invoiceRepository).findByBusinessId(eq(businessId), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
    }
}
