package com.arrive.invoice_app.services;

import com.arrive.invoice_app.entities.Invoice;
import com.arrive.invoice_app.repositories.InvoiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InvoiceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEventConsumer.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "invoice-events-notifications", groupId = "invoice-app-group")
    public void handleInvoiceEvent(String message) {
        log.info("Received invoice event: {}", message);

        JsonNode json = parseMessage(message);
        String event = json.get("event").asText();

        if ("PAYMENT_PROCESSED".equals(event)) {
            Long invoiceId = json.get("invoiceId").asLong();
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

            log.info("Updating invoice {} after payment event", invoiceId);
            invoiceRepository.save(invoice);
        }
    }

    private JsonNode parseMessage(String message) {
        try {
            return objectMapper.readTree(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Kafka message: " + message, e);
        }
    }
}
