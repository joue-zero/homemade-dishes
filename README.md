# Home-made Dishes Platform

A microservices-based platform for selling and ordering home-made dishes using Spring Boot, Enterprise JavaBeans (EJB), RabbitMQ, and React.

## Project Structure

```
├── order-service/       # Handles order processing and payments
├── dish-service/        # Manages dishes and inventory (EJB implementation)
├── user-service/        # Handles user management and authentication
├── gateway-service/     # API gateway for routing
└── frontend/            # React frontend application
```

## Prerequisites

- Java 21
- Maven
- Node.js 18+ and npm
- MySQL 8.0+
- RabbitMQ 3.9+

## Setup and Running Instructions

### 1. Set Up RabbitMQ

Install RabbitMQ and enable management plugin:
```bash
docker run -d \
  --hostname my-rabbit \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

Verify RabbitMQ is running:
- RabbitMQ service should be running on port 5672
- Management UI available at http://localhost:15672 (username: guest, password: guest)

### 3. Quick Start (All Services)
The project includes a convenience script to build and run all services:
```bash
.\rebuild-and-run.ps1
```

### 4. Running Services Individually

If you prefer to start services individually:

**Backend Services:**
```bash
# User Service
cd user-service
mvn clean install
mvn spring-boot:run

# Dish Service (EJB implementation)
cd dish-service
mvn clean install
mvn spring-boot:run

# Order Service
cd order-service
mvn clean install
mvn spring-boot:run

# Gateway Service (Optional)
cd gateway-service
mvn clean install
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm start
```

### 5. Service Endpoints

- User Service: http://localhost:8081
- Dish Service: http://localhost:8082
- Order Service: http://localhost:8084
- Frontend: http://localhost:3000

## Using the Platform

### User Roles

1. **Admin**
   - Login with default credentials (admin@example.com / admin123)
   - Create seller company accounts
   - Monitor user accounts and orders
   - View error logs

2. **Seller**
   - Login with credentials provided by admin
   - Manage dishes (add, update, view)
   - Monitor orders and sales

3. **Customer**
   - Register a new account
   - Browse available dishes
   - Add dishes to cart
   - Place and track orders

## RabbitMQ Integration (Detailed)

The platform leverages RabbitMQ extensively for asynchronous communication between services:

### 1. Order Validation Workflow

When a customer places an order:

```
Customer → Order Service → RabbitMQ → Dish Service → RabbitMQ → Order Service → Customer
```

**RabbitMQ Exchanges and Queues:**
- **Exchange**: `order.validation.exchange` (Direct exchange)
- **Queues**:
  - `stock.check.queue`: Verifies if requested dishes are in stock
  - `payment.validation.queue`: Processes payment if stock is available
  - `order.rejection.queue`: Handles order rejection when validation fails
  - `order.completion.queue`: Updates inventory when order is completed

**Routing Keys:**
- `stock.check`
- `payment.validation`
- `order.rejection`
- `order.completion`

### 2. Payment Processing

```
Order Service → Payment Queue → Order Service → Notification Exchange
```

- Minimum charge validation (configurable in `application.properties`)
- Payment failures trigger admin notifications
- Successful payments update user balance and order status

### 3. Admin Notifications

```
Services → Notification Exchange → Admin Notification Queue
```

- **Exchange**: `notification.exchange` (Direct exchange)
- **Queue**: `admin.notification.queue`
- **Routing Key**: `payment.failed`

### 4. Logging System

```
Services → Log Exchange → Log Queues
```

- **Exchange**: `log.exchange` (Topic exchange)
- **Queues**:
  - `all.logs.queue`: Captures all logs
  - `error.logs.queue`: Only captures error logs
  - `admin.logs.queue`: Routes errors to admin dashboard

- **Routing Key Pattern**: `{ServiceName}_{Severity}`
  - Examples: `Order_ERROR`, `Dish_INFO`, `User_WARNING`

## EJB Implementation

The Dish Service uses Enterprise JavaBeans with:

1. **Stateless Session Bean** (`StatelessDishServiceBean`):
   - Handles standard dish operations
   - Supports transactions for inventory updates

2. **Stateful Session Bean** (`StatefulDishSessionBean`):
   - Tracks user's viewed dishes and favorites
   - Maintains state across user's browsing session
