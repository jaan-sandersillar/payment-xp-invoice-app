package com.arrive.invoice_app.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InvoiceEventProducer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEventProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendInvoiceCreatedEvent(Long invoiceId, String customerName) {
        try {
            String message = "{\"event\":\"INVOICE_CREATED\",\"invoiceId\":" + invoiceId + ",\"customerName\":\"" + customerName + "\"}";
            kafkaTemplate.send("invoice-events", String.valueOf(invoiceId), message);
        } catch (Exception e) {
            log.error("Failed to send invoice created event for invoiceId={}", invoiceId, e);
        }
    }

    public void sendPaymentProcessedEvent(Long invoiceId, double amount, String status) {
        try {
            String message = "{\"event\":\"PAYMENT_PROCESSED\",\"invoiceId\":" + invoiceId + ",\"amount\":" + amount + ",\"status\":\"" + status + "\"}";
            kafkaTemplate.send("payment-notifications", String.valueOf(invoiceId), message);
        } catch (Exception e) {
            log.error("Failed to send payment event for invoiceId={}", invoiceId, e);
        }
    }
}
