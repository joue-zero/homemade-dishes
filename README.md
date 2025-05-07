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

## Note

This is a simplified version for learning purposes. Some advanced features like shipping companies and complex error handling are not implemented. 