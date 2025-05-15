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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private static final String USER_SERVICE_URL = "http://localhost:8081/api/users";
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private RestTemplate restTemplate;
    
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
        
        // Check user balance in user service
        try {
            Long customerId = order.getCustomerId();
            BigDecimal orderAmount = order.getTotalAmount();
            
            // Get current balance
            String balanceUrl = USER_SERVICE_URL + "/" + customerId + "/balance";
            BigDecimal currentBalance = restTemplate.getForObject(balanceUrl, BigDecimal.class);
            
            // Check if balance is sufficient
            if (currentBalance == null || currentBalance.compareTo(orderAmount) < 0) {
                logger.error("Insufficient balance: {} for order amount: {}", 
                    currentBalance, orderAmount);
                
                return PaymentResponse.failed(
                    request.getOrderId(),
                    "Insufficient balance to process payment"
                );
            }
            
            // Update balance by deducting order amount
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", orderAmount);
            
            // Call user service to update balance
            restTemplate.postForObject(balanceUrl, payload, BigDecimal.class);
            logger.info("Updated user balance for customer ID: {}, deducted amount: {}", customerId, orderAmount);
            
            // Update order payment status to paid
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            orderService.createOrder(order);
            
            logger.info("Payment succeeded for order: {}", request.getOrderId());
            return PaymentResponse.success(request.getOrderId());
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
} 