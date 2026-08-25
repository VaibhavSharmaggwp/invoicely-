package com.invoicely.backend.enums;

public enum InvoiceStatus {
    DRAFT,          // Invoice ban rahi hai, abhi send nahi ki (Editable)
    ISSUED,         // Customer ko bhej di gayi hai (Awaiting Payment)
    PARTIALLY_PAID, // Thoda payment aa gaya hai, thoda baaki hai
    PAID,           // Poora paisa aa gaya
    OVERDUE,        // Due date nikal gayi aur payment nahi aayi
    VOID            // Cancel kar di gayi (Galati se ban gayi thi)
}
