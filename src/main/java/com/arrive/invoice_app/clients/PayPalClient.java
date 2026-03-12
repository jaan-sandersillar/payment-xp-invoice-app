package com.arrive.invoice_app.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class PayPalClient {

    @Value("${paypal.api.base-url}")
    private String paypalApiUrl;

    @Value("${paypal.api.client-id}")
    private String clientId;

    @Value("${paypal.api.client-secret}")
    private String clientSecret;

    @Value("${paypal.api.access-token}")
    private String accessToken;

    @Autowired
    private RestTemplate restTemplate;

    public PayPalPaymentResponse processPayment(String invoiceNumber, double amount, String currency) {
        String url = paypalApiUrl + "/capture";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("intent", "CAPTURE");
        requestBody.put("invoice_id", invoiceNumber);
        requestBody.put("amount", Map.of("currency_code", currency, "value", String.valueOf(amount)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        PayPalPaymentResponse paymentResponse = new PayPalPaymentResponse();
        paymentResponse.setSuccess(response.getStatusCode().is2xxSuccessful());
        paymentResponse.setTransactionId("PAYPAL-" + System.currentTimeMillis());
        paymentResponse.setStatus(response.getStatusCode().is2xxSuccessful() ? "COMPLETED" : "FAILED");

        return paymentResponse;
    }

    public PayPalPaymentResponse processRefund(String originalTransactionId, double amount, String currency) {
        String url = paypalApiUrl + "/refund";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("original_transaction_id", originalTransactionId);
        requestBody.put("amount", Map.of("currency_code", currency, "value", String.valueOf(amount)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        PayPalPaymentResponse refundResponse = new PayPalPaymentResponse();
        refundResponse.setSuccess(response.getStatusCode().is2xxSuccessful());
        refundResponse.setTransactionId("PAYPAL-REF-" + System.currentTimeMillis());
        refundResponse.setStatus(response.getStatusCode().is2xxSuccessful() ? "REFUNDED" : "FAILED");

        return refundResponse;
    }

    public static class PayPalPaymentResponse {
        private boolean success;
        private String transactionId;
        private String status;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
