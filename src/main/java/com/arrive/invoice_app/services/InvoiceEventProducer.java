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
        String message = "{\"event\":\"INVOICE_CREATED\",\"invoiceId\":" + invoiceId + ",\"customerName\":\"" + customerName + "\"}";
        kafkaTemplate.send("invoice-events", String.valueOf(invoiceId), message);
    }

    public void sendPaymentProcessedEvent(Long invoiceId, double amount, String status) {
        String message = "{\"event\":\"PAYMENT_PROCESSED\",\"invoiceId\":" + invoiceId + ",\"amount\":" + amount + ",\"status\":\"" + status + "\"}";
        kafkaTemplate.send("payment-notifications", String.valueOf(invoiceId), message);
    }
}
