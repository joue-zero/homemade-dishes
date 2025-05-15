package com.dishes.orderservice.controller;

import com.dishes.orderservice.dto.PaymentRequest;
import com.dishes.orderservice.dto.PaymentResponse;
import com.dishes.orderservice.model.Order;
import com.dishes.orderservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Process a payment for an order
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        logger.info("Payment request received for order ID: {}", request.getOrderId());
        
        if (request.getOrderId() == null) {
            logger.error("Order ID is required for payment processing");
            return ResponseEntity.badRequest().body(
                PaymentResponse.failed(null, "Order ID is required for payment processing")
            );
        }
        
        try {
            PaymentResponse response = paymentService.processPayment(request);
            logger.info("Payment processed for order ID: {}, success: {}", request.getOrderId(), response.isSuccess());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                PaymentResponse.failed(request.getOrderId(), "Error processing payment: " + e.getMessage())
            );
        }
    }
    
    /**
     * Get payment status for an order
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long orderId) {
        logger.info("Payment status request for order ID: {}", orderId);
        
        try {
            Order.PaymentStatus status = paymentService.getPaymentStatus(orderId);
            logger.info("Payment status for order ID {}: {}", orderId, status);
            return ResponseEntity.ok(status.toString());
        } catch (Exception e) {
            logger.error("Error getting payment status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error getting payment status: " + e.getMessage());
        }
    }
} 