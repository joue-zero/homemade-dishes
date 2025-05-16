# Simple Home-made Dishes Platform

A simplified version of an online platform for home-made dishes using Java, RabbitMQ, MySQL, and React.

## Project Structure

```
├── order-service/         # Handles order processing
├── dish-service/         # Manages dishes and inventory
├── user-service/         # Handles user management
└── frontend/            # React frontend application
```

## Prerequisites

- Java 17 or higher
- Maven
- Node.js and npm
- MySQL
- RabbitMQ

## Setup Instructions

1. Start MySQL server
2. Start RabbitMQ server
3. Build and run each service:
   ```bash
   cd order-service
   mvn spring-boot:run
   
   cd dish-service
   mvn spring-boot:run
   
   cd user-service
   mvn spring-boot:run
   ```
4. Start the frontend:
   ```bash
   cd frontend
   npm install
   npm start
   ```

## Basic Features

- User registration and login
- View and add dishes
- Place orders
- Basic order notifications via RabbitMQ

## Advanced Messaging Features

The platform uses RabbitMQ for the following messaging patterns:

### Direct Exchange
- Payment failure notifications to admins using direct exchange with `payment.failed` routing key
- Only payment failure events are routed to the admin notification queue

### Topic Exchange
- Centralized logging system using a topic exchange named `log.exchange`
- Each service publishes logs with routing keys in the format `ServiceName_Severity` (e.g., `Order_ERROR`, `Dish_INFO`)
- Admin notifications are subscribed only to error logs using the wildcard pattern `*_Error`

## Note

This is a simplified version for learning purposes. Some advanced features like shipping companies and complex error handling are not implemented. 