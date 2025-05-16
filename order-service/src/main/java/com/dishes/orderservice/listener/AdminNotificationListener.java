package com.dishes.orderservice.listener;

import com.dishes.orderservice.config.NotificationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AdminNotificationListener {
    private static final Logger logger = LoggerFactory.getLogger(AdminNotificationListener.class);

    @RabbitListener(queues = NotificationConfig.ADMIN_PAYMENT_NOTIFICATION_QUEUE)
    public void receivePaymentFailureNotification(String message) {
        // In a real application, this could send an email, push notification, or update admin dashboard
        logger.info("ADMIN NOTIFICATION: Payment failure detected: {}", message);
        System.out.println("⚠️ ADMIN ALERT: Payment failure detected - " + message);
    }

    @RabbitListener(queues = NotificationConfig.ADMIN_LOG_NOTIFICATION_QUEUE)
    public void receiveErrorLogNotification(String message) {
        // In a real application, this could send an email, push notification, or update admin dashboard
        logger.info("ADMIN NOTIFICATION: Error log received: {}", message);
        System.out.println("🔴 ADMIN ALERT: Error log received - " + message);
    }
} 