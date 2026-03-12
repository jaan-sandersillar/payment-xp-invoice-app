package com.arrive.invoice_app.entities;

/**
 * Enum representing the different statuses an invoice can have.
 */
public enum InvoiceStatus {
    PENDING,
    PARTIALLY_PAID,
    PAID,
    REFUNDED,
    CANCELLED
}
