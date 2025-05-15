package com.dishes.orderservice.service;

import com.dishes.orderservice.dto.PaymentRequest;
import com.dishes.orderservice.dto.PaymentResponse;
import com.dishes.orderservice.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private OrderService orderService;
    
    @Value("${order.minimum.charge:10.0}")
    private BigDecimal minimumOrderCharge;
    
    /**
     * Process a payment for an order
     * @param request Payment details
     * @return PaymentResponse with the result
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        logger.info("Processing payment for order ID: {}", request.getOrderId());
        
        Order order = orderService.getOrder(request.getOrderId());
        if (order == null) {
            logger.error("Order not found: {}", request.getOrderId());
            return PaymentResponse.failed(request.getOrderId(), "Order not found");
        }
        
        // Check if order is in correct state for payment
        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.ACCEPTED) {
            logger.error("Invalid order status for payment: {}", order.getStatus());
            return PaymentResponse.failed(
                request.getOrderId(), 
                "Invalid order status for payment: " + order.getStatus()
            );
        }
        
        // Check if payment already processed
        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            logger.info("Payment already processed for order: {}", request.getOrderId());
            return PaymentResponse.success(request.getOrderId());
        }
        
        // Verify minimum charge
        if (order.getTotalAmount().compareTo(minimumOrderCharge) < 0) {
            logger.error(
                "Order amount {} is below minimum charge {}", 
                order.getTotalAmount(), 
                minimumOrderCharge
            );
            
            // Update order status to rejected
            order.setStatus(Order.OrderStatus.REJECTED);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            orderService.createOrder(order);
            
            return PaymentResponse.failed(
                request.getOrderId(),
                "Order amount is below minimum charge of $" + minimumOrderCharge
            );
        }
        
        // Process payment (simulated)
        try {
            // In a real app, this would call a payment gateway
            boolean paymentSucceeded = processPaymentWithGateway(request);
            
            if (paymentSucceeded) {
                // Update order payment status
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                orderService.createOrder(order);
                
                logger.info("Payment succeeded for order: {}", request.getOrderId());
                return PaymentResponse.success(request.getOrderId());
            } else {
                // Update order payment status
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                orderService.createOrder(order);
                
                logger.error("Payment failed for order: {}", request.getOrderId());
                return PaymentResponse.failed(request.getOrderId(), "Payment processing failed");
            }
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
            return PaymentResponse.failed(request.getOrderId(), "Payment processing error: " + e.getMessage());
        }
    }
    
    /**
     * Get the payment status for an order
     * @param orderId The order ID
     * @return The payment status
     */
    public Order.PaymentStatus getPaymentStatus(Long orderId) {
        logger.info("Getting payment status for order: {}", orderId);
        Order order = orderService.getOrder(orderId);
        if (order == null) {
            logger.error("Order not found: {}", orderId);
            throw new RuntimeException("Order not found: " + orderId);
        }
        return order.getPaymentStatus();
    }
    
    /**
     * Process payment with external payment gateway (simulated)
     * @param request Payment details
     * @return true if payment succeeded, false otherwise
     */
    private boolean processPaymentWithGateway(PaymentRequest request) {
        // Simulate payment processing
        // In a real app, this would call an external payment service
        
        // For testing purposes, consider all payments valid except those with specific test card numbers
        if (request.getCardNumber() != null && 
            (request.getCardNumber().equals("4111111111111111") || 
             request.getCardNumber().startsWith("4242"))) {
            return true;
        } else if (request.getCardNumber() != null && 
                  request.getCardNumber().equals("4000000000000002")) {
            return false; // simulate declined payment
        }
        
        // Default to successful for testing
        return true;
    }
} 