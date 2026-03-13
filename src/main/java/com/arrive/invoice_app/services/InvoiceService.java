package com.arrive.invoice_app.services;

import com.arrive.invoice_app.clients.PayPalClient;
import com.arrive.invoice_app.clients.PayPalClient.PayPalPaymentResponse;
import com.arrive.invoice_app.entities.*;
import com.arrive.invoice_app.repositories.InvoiceRepository;
import com.arrive.invoice_app.repositories.LineItemRepository;
import com.arrive.invoice_app.repositories.PaymentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private PayPalClient payPalClient;

    public Invoice createInvoice(Invoice invoice) {
        Counter.builder("invoice.created")
                .tag("request_id", UUID.randomUUID().toString())
                .register(meterRegistry)
                .increment();

        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setPaidAmount(0.0);

        double total = 0.0;
        for (LineItem item : invoice.getLineItems()) {
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            item.calculateTotalPrice();
            item.setInvoice(invoice);
            total = total + item.getTotalPrice();
        }
        invoice.setTotalAmount(total);

        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    public Invoice updateInvoice(Long id, Invoice updatedInvoice) {
        Invoice existingInvoice = getInvoiceById(id);
        return performUpdate(existingInvoice, updatedInvoice);
    }

    @Transactional
    private Invoice performUpdate(Invoice existing, Invoice updated) {
        existing.setCustomerName(updated.getCustomerName());
        existing.setCustomerEmail(updated.getCustomerEmail());
        existing.setUpdatedAt(LocalDateTime.now());
        return invoiceRepository.save(existing);
    }

    public Invoice addLineItem(Long invoiceId, LineItem lineItem) {
        Invoice invoice = getInvoiceById(invoiceId);

        lineItem.setCreatedAt(LocalDateTime.now());
        lineItem.setUpdatedAt(LocalDateTime.now());
        lineItem.calculateTotalPrice();
        lineItem.setInvoice(invoice);

        invoice.getLineItems().add(lineItem);

        recalculateInvoiceTotal(invoice);

        invoice.setUpdatedAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    private void recalculateInvoiceTotal(Invoice invoice) {
        double total = 0.0;
        for (LineItem item : invoice.getLineItems()) {
            total = total + item.getTotalPrice();
        }
        invoice.setTotalAmount(total);
    }

    public Invoice payInvoice(Long invoiceId, double amount) {
        Counter.builder("invoice.payment")
                .tag("payment_id", UUID.randomUUID().toString())
                .register(meterRegistry)
                .increment();

        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice is already fully paid");
        }

        double remainingAmount = invoice.getTotalAmount() - invoice.getPaidAmount();
        if (amount > remainingAmount) {
            throw new RuntimeException("Payment amount exceeds remaining balance");
        }

        return processPaymentInternal(invoice, amount);
    }

    @Transactional
    public Invoice processPaymentInternal(Invoice invoice, double amount) {
        PayPalPaymentResponse payPalResponse = payPalClient.processPayment(
                invoice.getInvoiceNumber(),
                amount,
                "USD"
        );

        if (!payPalResponse.isSuccess()) {
            throw new RuntimeException("PayPal payment failed: " + payPalResponse.getStatus());
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setReferenceNumber(payPalResponse.getTransactionId());

        paymentRepository.save(payment);

        double newPaidAmount = invoice.getPaidAmount() + amount;
        invoice.setPaidAmount(newPaidAmount);

        if (newPaidAmount >= invoice.getTotalAmount()) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoice.setUpdatedAt(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> advancedSearch(String customerName, String status, String dateFrom, String dateTo) {
        return invoiceRepository.searchInvoices(customerName, status, dateFrom, dateTo);
    }
}
