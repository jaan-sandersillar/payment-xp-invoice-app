package com.arrive.invoice_app.controllers;

import com.arrive.invoice_app.entities.Invoice;
import com.arrive.invoice_app.entities.InvoiceStatus;
import com.arrive.invoice_app.entities.LineItem;
import com.arrive.invoice_app.services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        Invoice createdInvoice = invoiceService.createInvoice(invoice);
        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }

    @PostMapping("/list")
    public ResponseEntity<List<Invoice>> getAllInvoices(@RequestBody(required = false) Map<String, Object> body) {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @PostMapping("/get")
    public ResponseEntity<Invoice> getInvoiceById(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        Invoice invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/get-by-number")
    public ResponseEntity<Invoice> getInvoiceByNumber(@RequestBody Map<String, String> body) {
        String invoiceNumber = body.get("invoiceNumber");
        Invoice invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/search")
    public ResponseEntity<List<Invoice>> searchByCustomerName(@RequestBody Map<String, String> body) {
        String customerName = body.get("customerName");
        List<Invoice> invoices = invoiceService.searchByCustomerName(customerName);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping("/by-status")
    public ResponseEntity<List<Invoice>> getInvoicesByStatus(@RequestBody Map<String, String> body) {
        InvoiceStatus status = InvoiceStatus.valueOf(body.get("status"));
        List<Invoice> invoices = invoiceService.getInvoicesByStatus(status);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping("/update")
    public ResponseEntity<Invoice> updateInvoice(@RequestBody Map<String, Object> body) {
        Long id = ((Number) body.get("id")).longValue();
        Invoice invoice = new Invoice();
        invoice.setCustomerName((String) body.get("customerName"));
        invoice.setCustomerEmail((String) body.get("customerEmail"));
        Invoice updatedInvoice = invoiceService.updateInvoice(id, invoice);
        return ResponseEntity.ok(updatedInvoice);
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteInvoice(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{invoiceId}/line-items")
    public ResponseEntity<Invoice> addLineItem(@PathVariable Long invoiceId, @RequestBody LineItem lineItem) {
        Invoice invoice = invoiceService.addLineItem(invoiceId, lineItem);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/{invoiceId}/line-items/update")
    public ResponseEntity<Invoice> updateLineItem(
            @PathVariable Long invoiceId,
            @RequestBody Map<String, Object> body) {
        Long lineItemId = ((Number) body.get("lineItemId")).longValue();
        LineItem lineItem = new LineItem();
        lineItem.setDescription((String) body.get("description"));
        lineItem.setQuantity((Integer) body.get("quantity"));
        lineItem.setUnitPrice(((Number) body.get("unitPrice")).doubleValue());
        Invoice invoice = invoiceService.updateLineItem(invoiceId, lineItemId, lineItem);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/{invoiceId}/line-items/delete")
    public ResponseEntity<Invoice> removeLineItem(
            @PathVariable Long invoiceId,
            @RequestBody Map<String, Long> body) {
        Long lineItemId = body.get("lineItemId");
        Invoice invoice = invoiceService.removeLineItem(invoiceId, lineItemId);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/advanced-search")
    public ResponseEntity<List<Invoice>> advancedSearch(@RequestBody Map<String, String> body) {
        String customerName = body.get("customerName");
        String status = body.get("status");
        String dateFrom = body.get("dateFrom");
        String dateTo = body.get("dateTo");
        List<Invoice> invoices = invoiceService.advancedSearch(customerName, status, dateFrom, dateTo);
        return ResponseEntity.ok(invoices);
    }

    @PostMapping("/{invoiceId}/pay")
    public ResponseEntity<Invoice> payInvoice(
            @PathVariable Long invoiceId,
            @RequestBody Map<String, Double> body) {
        double amount = body.get("amount");
        Invoice invoice = invoiceService.payInvoice(invoiceId, amount);
        return ResponseEntity.ok(invoice);
    }

    @PostMapping("/{invoiceId}/refund")
    public ResponseEntity<Invoice> refundPayment(
            @PathVariable Long invoiceId,
            @RequestBody Map<String, Double> body) {
        double amount = body.get("amount");
        Invoice invoice = invoiceService.refundPayment(invoiceId, amount);
        return ResponseEntity.ok(invoice);
    }
}
