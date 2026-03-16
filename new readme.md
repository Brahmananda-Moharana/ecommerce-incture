## Table of Contents

* [Project Title](#project-title)
* [Project Overview](#project-overview)
    * [Key Capabilities](#key-capabilities)
* [Tech Stack](#tech-stack)
    * [Backend](#backend)
    * [Database](#database)
    * [Authentication](#authentication)
    * [Utilities](#utilities)
    * [Validation](#validation)
    * [Testing](#testing)
    * [Build Tool](#build-tool)
* [Project Architecture](#project-architecture)
    * [Package Structure](#package-structure)
    * [Controller Layer](#controller-layer)
    * [Service Layer](#service-layer)
    * [Repository Layer](#repository-layer)
    * [Entity Layer](#entity-layer)
    * [DTO Layer](#dto-layer)
    * [Security Layer](#security-layer)
    * [Request Flow Architecture](#request-flow-architecture)
* [Running the Project Locally](#running-the-project-locally)
    * [Prerequisites](#prerequisites)
    * [1. Clone the Repository](#1-clone-the-repository)
    * [2. Configure Database](#2-configure-database)
    * [3. Build the Project](#3-build-the-project)
    * [4. Run the Application](#4-run-the-application)
    * [5. Verify Application](#5-verify-application)
    * [6. Test APIs](#6-test-apis)
* [Base URL](#base-url)
* [Authentication](#authentication-1)
* [API Access Control](#api-access-control)
    * [Public Endpoints](#public-endpoints-no-authentication-required)
    * [Admin Only Endpoints](#admin-only-endpoints)
    * [User Only Endpoints](#user-only-endpoints)
    * [Authenticated Endpoints](#authenticated-endpoints-user-or-admin)
* [User APIs](#user-apis)
    * [1. Register User](#1-register-user)
    * [2. Login User](#2-login-user)
    * [3. Get User By ID](#3-get-user-by-id)
    * [4. Update User](#4-update-user)
    * [5. Delete User (Admin Only)](#5-delete-user-admin-only)
* [Product APIs](#product-apis)
    * [1. Create Product (Admin)](#1-create-product-admin)
    * [2. Get All Products](#2-get-all-products)
    * [3. Get Product By ID](#3-get-product-by-id)
    * [4. Update Product (Admin)](#4-update-product-admin)
    * [5. Delete Product (Admin)](#5-delete-product-admin)
* [Cart APIs](#cart-apis)
    * [1. Add Product to Cart](#1-add-product-to-cart)
    * [2. Update Cart Item Quantity](#2-update-cart-item-quantity)
    * [3. Remove Item from Cart](#3-remove-item-from-cart)
    * [4. Get User Cart](#4-get-user-cart)
* [Order APIs](#order-apis)
    * [1. Checkout Order](#1-checkout-order)
    * [2. Get All Orders](#2-get-all-orders)
    * [3. Get Order By ID](#3-get-order-by-id)
    * [4. Update Order Status (Admin Only)](#4-update-order-status-admin-only)
* [Roles](#roles)
* [Project Structure (Important Packages)](#project-structure-important-packages)
* [Default Application Port](#default-application-port)
* [Build Command Summary](#build-command-summary)
* [Database Schema](#database-schema)
* [Database Diagram](#database-diagram)
# E-Commerce Backend API – Spring Boot

# Project Title

**E-Commerce Backend API – Spring Boot**

A RESTful backend application that powers an e-commerce platform. The system handles product management, user authentication, shopping cart operations, and order processing with secure, scalable APIs.

---

# Project Overview

This project is a **Spring Boot–based backend service for an e-commerce platform** that provides REST APIs for managing products, shopping carts, and orders.

The application follows a **clean N-tier architecture** where responsibilities are clearly separated into controller, service, repository, and entity layers. It uses **JWT-based authentication with Spring Security** to ensure secure access to protected resources.

### Key Capabilities

* User registration and login with secure password hashing
* JWT-based authentication and role-based authorization
* Product management with stock tracking and soft deletion
* Shopping cart management with automatic price calculation
* Order placement with simulated payment processing
* Comprehensive exception handling and logging
* Unit testing of service-layer business logic

The system is designed to mimic a **real-world e-commerce backend architecture**, focusing on scalability, maintainability, and proper separation of concerns.

---

# Tech Stack

## Backend

* **Java 17**
* **Spring Boot 3**
* **Spring Data JPA**
* **Spring Security**

## Database

* **MySQL**

## Authentication

* **JWT (JSON Web Tokens)**
* **BCrypt password hashing**

## Utilities

* **Lombok**
* **SLF4J / Logback Logging**

## Validation

* **Jakarta Bean Validation**

## Testing

* **JUnit 5**
* **Mockito**
* **Mockito-inline**

## Build Tool

* **Maven**

---

# Project Architecture

The project follows a **layered N-tier architecture**, which separates responsibilities into independent modules to improve maintainability, scalability, and testability.

```
Controller Layer
        ↓
Service Layer
        ↓
Repository Layer
        ↓
Database
```

---

## Package Structure

```
com.incture.eCommerce
│
├── controller
├── service
├── repository
├── entity
├── dto
├── exception
├── config
├── util
└── filter
```

### Controller Layer

Handles **incoming HTTP requests** and returns API responses.

Responsibilities:

* Mapping REST endpoints
* Validating request DTOs using `@Valid`
* Calling appropriate service methods
* Returning formatted responses

Example Controllers:

* `UserController`
* `ProductController`
* `CartController`
* `OrderController`

---

### Service Layer

Contains the **core business logic** of the application.

Responsibilities:

* Implement business rules
* Manage transactions using `@Transactional`
* Perform validations and authorization checks
* Communicate with repositories

Example Services:

* `UserService`
* `ProductService`
* `CartService`
* `OrderService`

---

### Repository Layer

Responsible for **database interaction** using Spring Data JPA.

Responsibilities:

* CRUD operations
* Custom query execution
* Entity persistence

Example Repositories:

* `UserRepository`
* `ProductRepository`
* `CartRepository`
* `OrderRepository`

---

### Entity Layer

Defines **database table mappings** using JPA annotations.

Example Entities:

* `User`
* `Product`
* `Cart`
* `CartItem`
* `Order`
* `OrderItem`

---

### DTO Layer

DTOs (Data Transfer Objects) are used to **separate API models from database entities**.

Advantages:

* Prevent exposing internal entity structures
* Enable request validation
* Provide clean API responses

Example DTOs:
* `UserLoginRequest`
* `UserRegistrationRequest`
* `UserResponse`
* `UserUpdateRequest`
* `ProductRequest`
* `CartItemDto`
* `CartResponse`
* `OrderItemDto`
* `OrderResponse`

---

### Security Layer

Spring Security protects API endpoints using **JWT authentication**.

Key components:

* `SecurityConfig`
* `JwtRequestFilter`
* `JwtUtil`
* `SecurityUtil`

Authentication Flow:

1. User logs in using credentials.
2. Server generates a JWT token.
3. Client sends the token in request headers.
4. Security filter validates the token.
5. Authenticated user is stored in `SecurityContextHolder`.

---

## Request Flow Architecture

```
[ Client (Postman / Frontend React/Angular App) ]
                              |
                              | 1. HTTP Request (POST /api/orders/checkout)
                              |    Headers: Authorization: Bearer <JWT>
                              v

+-------------------------------------------------------------------------+
|                          SECURITY LAYER                                 |
|  [ JwtRequestFilter ]                                                   |
|  - Intercepts incoming requests                                         |
|  - Extracts and validates JWT using JwtUtil                             |
|  - Sets authenticated user in SecurityContextHolder                     |
+-------------------------------------------------------------------------+
                              |
                              | 2. Forwarded Request
                              v
+-------------------------------------------------------------------------+
|                        CONTROLLER LAYER                                 |
|  [ OrderController, CartController, ProductController ]                 |
|  - Handles REST endpoint routing                                        |
|  - Validates request payload using DTOs and @Valid                      |
|  - Sends request to Service layer                                       |
+-------------------------------------------------------------------------+
                              |
                              | 3. Method Invocation
                              v
+-------------------------------------------------------------------------+
|                          SERVICE LAYER                                  |
|  [ OrderService, CartService, UserService ]                             |
|  - Executes business logic                                              |
|  - Simulates payment processing                                        |
|  - Updates product stock                                               |
|  - Handles cart management                                             |
|  - Throws custom exceptions if rules fail                              |
+-------------------------------------------------------------------------+
                              |
                              | 4. Data Access
                              v
+-------------------------------------------------------------------------+
|                       REPOSITORY LAYER                                  |
|  [ OrderRepository, ProductRepository, CartRepository ]                 |
|  - Spring Data JPA interfaces                                           |
|  - Converts Java method calls to SQL queries                            |
+-------------------------------------------------------------------------+
                              |
                              | 5. SQL Operations
                              v
+-------------------------------------------------------------------------+
|                         DATABASE LAYER                                  |
|                         MySQL Database                                  |
|  Tables:                                                                |
|  - users                                                                |
|  - products                                                             |
|  - carts                                                                |
|  - cart_items                                                           |
|  - orders                                                               |
|  - order_items                                                          |
+-------------------------------------------------------------------------+
```

# E-Commerce Backend API

A RESTful backend API built with **Spring Boot** for an e-commerce platform.
The system provides APIs for **user management, product catalog, and shopping cart operations** with **JWT-based authentication and role-based authorization**.

---

# Base URL

```id="6nu75l"
http://localhost:8080/api
```

---

# Authentication

The API uses **JWT (JSON Web Token)** authentication.

For secured endpoints include the header:

```id="548m70"
Authorization: Bearer <JWT_TOKEN>
```

---

# API Access Control

## Public Endpoints (No Authentication Required)

| Method | Endpoint              | Description                 |
| ------ | --------------------- | --------------------------- |
| POST   | `/api/users/register` | Register new user           |
| POST   | `/api/users/login`    | Login and receive JWT token |
| GET    | `/api/products`       | Get product list            |
| GET    | `/api/products/{id}`  | Get product details         |

---

## Admin Only Endpoints

| Method | Endpoint             | Description    |
| ------ | -------------------- | -------------- |
| DELETE | `/api/users/{id}`    | Delete user    |
| POST   | `/api/products`      | Create product |
| PUT    | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |

Required header:

```id="tq87sv"
Authorization: Bearer <JWT_TOKEN>
```

---

## User Only Endpoints

| Method | Endpoint          | Description         |
| ------ | ----------------- | ------------------- |
| PUT    | `/api/users/{id}` | Update user profile |

Required header:

```id="cfdhku"
Authorization: Bearer <JWT_TOKEN>
```

---

## Authenticated Endpoints (User or Admin)

| Method | Endpoint                       | Description             |
| ------ | ------------------------------ | ----------------------- |
| GET    | `/api/users/{id}`              | Get user details        |
| POST   | `/api/cart/add/{productId}`    | Add item to cart        |
| PUT    | `/api/cart/update/{productId}` | Update cart item        |
| DELETE | `/api/cart/remove/{productId}` | Remove cart item        |
| GET    | `/api/cart`                    | Get current user's cart |

Required header:

```id="6f2o81"
Authorization: Bearer <JWT_TOKEN>
```

---

# User APIs

## 1. Register User

**Endpoint**

```id="mrm3c1"
POST /api/users/register
```

**Request Body**

```json id="zowy5w"
{
  "name": "brahma",
  "email": "brahma@gmail.com",
  "password": "123",
  "role": "CUSTOMER"
}
```

**Response**

```json id="mr0fay"
{
  "id": 1,
  "name": "brahma",
  "email": "brahma@gmail.com",
  "password": "123",
  "role": "CUSTOMER"
}
```

---

## 2. Login User

**Endpoint**

```id="fp2vzt"
POST /api/users/login
```

**Request Body**

```json id="rj8swg"
{
  "email": "brahma@gmail.com",
  "password": "123"
}
```

**Response**

```id="53xfzq"
JWT Token
```

Example

```id="a7vmd7"
eyJhbGciOiJIUzI1NiJ9...
```

---

## 3. Get User By ID

**Endpoint**

```id="h97621"
GET /api/users/{id}
```

**Header**

```id="3o67fh"
Authorization: Bearer <JWT_TOKEN>
```

**Response**

```json id="pz7bkz"
{
  "id": 1,
  "name": "brahma",
  "email": "brahma@gmail.com",
  "role": "CUSTOMER"
}
```

---

## 4. Update User

**Endpoint**

```id="6qcerg"
PUT /api/users/{id}
```

**Header**

```id="lt8yr5"
Authorization: Bearer <JWT_TOKEN>
```

**Request Body**

```json id="5lobgi"
{
  "name": "Brahmananda"
}
```

**Response**

```json id="9636iy"
{
  "id": 1,
  "name": "Brahmananda",
  "email": "brahma@gmail.com",
  "role": "CUSTOMER"
}
```

---

## 5. Delete User (Admin Only)

**Endpoint**

```id="c3x4ao"
DELETE /api/users/{id}
```

**Header**

```id="ogrc97"
Authorization: Bearer <JWT_TOKEN>
```

**Response**

```id="9kwvsb"
204 No Content
```

---

# Product APIs

## 1. Create Product (Admin)

**Endpoint**

```id="i53ltp"
POST /api/products
```

**Header**

```id="0vsu2i"
Authorization: Bearer <JWT_TOKEN>
```

**Request Body**

```json id="kr2dqh"
{
  "name": "Keychron Q6 Pro",
  "description": "Full-size QMK/VIA wireless custom mechanical keyboard with CNC aluminum body",
  "price": 15499,
  "stock": 25,
  "category": "Accessories",
  "imageURL": "https://example.com/images/keychronq6pro.jpg",
  "rating": 4.8,
  "active": true
}
```

**Response**

```json id="m4yjjf"
{
  "id": 6,
  "name": "Keychron Q6 Pro",
  "description": "Full-size QMK/VIA wireless custom mechanical keyboard with CNC aluminum body",
  "price": 15499.0,
  "stock": 25,
  "category": "Accessories",
  "imageURL": "https://example.com/images/keychronq6pro.jpg",
  "rating": 4.8,
  "active": true
}
```

---

## 2. Get All Products

**Endpoint**

```id="dcgk28"
GET /api/products
```

### Query Parameters

| Parameter | Type    | Description              |
| --------- | ------- | ------------------------ |
| page      | Integer | Page number              |
| size      | Integer | Number of items per page |
| sortBy    | String  | Field used for sorting   |

Example

```id="ypp82o"
GET /api/products?page=0&size=5&sortBy=price
```

### Response

```json id="g7jcz2"
{
  "content": [
    {
      "id": 6,
      "name": "Keychron Q6 Pro",
      "description": "Full-size QMK/VIA wireless custom mechanical keyboard with CNC aluminum body",
      "price": 15499.0,
      "stock": 25,
      "category": "Accessories",
      "imageURL": "https://example.com/images/keychronq6pro.jpg",
      "rating": 4.8,
      "active": true
    },
    {
      "id": 4,
      "name": "Sony WH-1000XM5",
      "description": "Wireless noise cancelling headphones",
      "price": 29999.0,
      "stock": 40,
      "category": "Accessories",
      "imageURL": "https://example.com/images/sonyheadphones.jpg",
      "rating": 4.7,
      "active": true
    }
  ],
  "empty": false,
  "first": true,
  "last": true,
  "number": 0,
  "numberOfElements": 4,
  "size": 5,
  "totalElements": 4,
  "totalPages": 1
}
```

---

## 3. Get Product By ID

**Endpoint**

```id="bwraai"
GET /api/products/{id}
```

**Response**

```json id="ti7fwf"
{
  "id": 3,
  "name": "Dell Inspiron 15",
  "description": "15-inch laptop with Intel i7 processor",
  "price": 65000.0,
  "stock": 15,
  "category": "Laptop",
  "imageURL": "https://example.com/images/dellinspiron15.jpg",
  "rating": 4.3,
  "active": false
}
```

---

## 4. Update Product (Admin)

**Endpoint**

```id="5yqy0h"
PUT /api/products/{id}
```

**Header**

```id="82a18l"
Authorization: Bearer <JWT_TOKEN>
```

**Request Body**

```json id="7r69y7"
{
  "name": "Keychron Q6 Pro",
  "description": "Full-size QMK/VIA wireless custom mechanical keyboard with CNC aluminum body",
  "price": 15499.0,
  "stock": 30,
  "category": "Accessories",
  "imageURL": "https://example.com/images/keychronq6pro.jpg",
  "rating": 4.8,
  "active": true
}
```

---

## 5. Delete Product (Admin)

**Endpoint**

```id="mrja7x"
DELETE /api/products/{id}
```

**Header**

```id="hg2jvb"
Authorization: Bearer <JWT_TOKEN>
```

**Response**

```id="jhhnu2"
204 No Content
```

---

# Cart APIs

All cart endpoints require authentication.

---

## 1. Add Product to Cart

**Endpoint**

```id="f61htx"
POST /api/cart/add/{productId}
```

### Header

```id="2uh7aw"
Authorization: Bearer <JWT_TOKEN>
```

### Query Parameters

| Parameter | Type    | Description            |
| --------- | ------- | ---------------------- |
| quantity  | Integer | Number of items to add |

Example

```id="92x70d"
POST /api/cart/add/1?quantity=2
```

**Response**

```json id="1sxlbg"
{
  "cartId": 2,
  "cartTotal": 159998.0,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 2,
      "unitPrice": 79999.0,
      "totalPrice": 159998.0
    }
  ]
}
```

---

## 2. Update Cart Item Quantity

**Endpoint**

```id="c3j5mu"
PUT /api/cart/update/{productId}
```

### Header

```id="mge4fb"
Authorization: Bearer <JWT_TOKEN>
```

### Query Parameters

| Parameter | Type    | Description              |
| --------- | ------- | ------------------------ |
| quantity  | Integer | Updated product quantity |

Example

```id="txhdyt"
PUT /api/cart/update/1?quantity=7
```

**Response**

```json id="ftlh4h"
{
  "cartId": 2,
  "cartTotal": 1084986.0,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 7,
      "unitPrice": 79999.0,
      "totalPrice": 559993.0
    }
  ]
}
```

---

## 3. Remove Item from Cart

**Endpoint**

```id="tswsye"
DELETE /api/cart/remove/{productId}
```

### Header

```id="1ts5u0"
Authorization: Bearer <JWT_TOKEN>
```

Example

```id="s30h7i"
DELETE /api/cart/remove/2
```

**Response**

```json id="43m0sd"
{
  "cartId": 2,
  "cartTotal": 559993.0,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 7,
      "unitPrice": 79999.0,
      "totalPrice": 559993.0
    }
  ]
}
```

---

## 4. Get User Cart

**Endpoint**

```id="g8gt0p"
GET /api/cart
```

### Header

```id="b6q24p"
Authorization: Bearer <JWT_TOKEN>
```

**Response**

```json id="30yfbi"
{
  "cartId": 2,
  "cartTotal": 559993.0,
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 7,
      "unitPrice": 79999.0,
      "totalPrice": 559993.0
    }
  ]
}
```
# Order APIs

All order endpoints require **authentication** using a valid **JWT token**.

### Header

```text
Authorization: Bearer <JWT_TOKEN>
```

---

# 1. Checkout Order

Creates an order from the authenticated user's cart.

### Endpoint

```http
POST /api/orders/checkout
```

### Header

```text
Authorization: Bearer <JWT_TOKEN>
```

### Response

```json
{
  "orderId": 2,
  "userId": 3,
  "totalAmount": 559993.0,
  "orderDate": "2026-03-16T00:20:51.6203053",
  "paymentStatus": "SUCCESS",
  "orderStatus": "PLACED",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 7,
      "unitPrice": 79999.0,
      "totalPrice": 559993.0
    }
  ]
}
```

---

# 2. Get All Orders

Returns all orders for the authenticated user.

### Endpoint

```http
GET /api/orders
```

### Header

```text
Authorization: Bearer <JWT_TOKEN>
```

### Response

```json
[
  {
    "orderId": 2,
    "userId": 3,
    "totalAmount": 559993.0,
    "orderDate": "2026-03-16T00:20:51.620305",
    "paymentStatus": "SUCCESS",
    "orderStatus": "PLACED",
    "items": [
      {
        "productId": 1,
        "productName": "iPhone 15",
        "quantity": 7,
        "unitPrice": 79999.0,
        "totalPrice": 559993.0
      }
    ]
  }
]
```

---

# 3. Get Order By ID

Returns details of a specific order.

### Endpoint

```http
GET /api/orders/{id}
```

### Header

```text
Authorization: Bearer <JWT_TOKEN>
```

### Path Parameters

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| id        | Long | Order ID    |

Example

```http
GET /api/orders/2
```

### Response

```json
{
  "orderId": 2,
  "userId": 3,
  "totalAmount": 559993.0,
  "orderDate": "2026-03-16T00:20:51.620305",
  "paymentStatus": "SUCCESS",
  "orderStatus": "PLACED",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 7,
      "unitPrice": 79999.0,
      "totalPrice": 559993.0
    }
  ]
}
```

---

# 4. Update Order Status (Admin Only)

Updates the status of an existing order.

### Endpoint

```http
PUT /api/orders/{id}/status
```

### Header

```text
Authorization: Bearer <JWT_TOKEN>
```

### Query Parameters

| Parameter | Type   | Description      |
| --------- | ------ | ---------------- |
| status    | String | New order status |

Example

```http
PUT /api/orders/2/status?status=SHIPPED
```

### Response

```json
{
  "orderId": 2,
  "userId": 3,
  "totalAmount": 559993.0,
  "orderDate": "2026-03-16T00:20:51.620305",
  "paymentStatus": "SUCCESS",
  "orderStatus": "SHIPPED",
  "items": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "quantity": 7,
      "unitPrice": 79999.0,
      "totalPrice": 559993.0
    }
  ]
}
```

---

# Roles

| Role     | Permissions                     |
| -------- | ------------------------------- |
| CUSTOMER | Browse products and manage cart |
| ADMIN    | Manage products and users       |

# Running the Project Locally

This guide explains how to run the **ecommerce-incture** backend project on your local machine.

---

# Prerequisites

Make sure the following tools are installed on your system:

* **Java 17+**
* **Maven 3.8+**
* **Git**
* **Postman or any API testing tool**
* **MySQL / PostgreSQL** (depending on your database configuration)

You can verify installations using:

```bash
java -version
mvn -version
git --version
```

---

# 1. Clone the Repository

Clone the project from GitHub.

```bash
git clone https://github.com/Brahmananda-Moharana/ecommerce-incture.git
```

Navigate into the project directory:

```bash
cd ecommerce-incture
```

---

# 2. Configure Database

Open the following file:

```
src/main/resources/application.properties
```

Update the database configuration according to your local setup.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Create the database if it does not exist.

Example (MySQL):

```sql
CREATE DATABASE ecommerce_db;
```

---

# 3. Build the Project

Run the following command to build the project:

```bash
mvn clean install
```

This will:

* Download dependencies
* Compile the project
* Run tests
* Package the application

---

# 4. Run the Application

Start the Spring Boot application:

```bash
mvn spring-boot:run
```

Or run the generated jar:

```bash
java -jar target/ecommerce-incture.jar
```

---

# 5. Verify Application

The application will start on:

```
http://localhost:8080
```

Base API URL:

```
http://localhost:8080/api
```

---

# 6. Test APIs

You can test the APIs using **Postman** or **cURL**.

Example request:

```
POST http://localhost:8080/api/users/register
```

---

# Project Structure (Important Packages)

```
controller  → REST API endpoints
service     → Business logic
repository  → Database access (JPA)
entity      → Database entities
dto         → Request/Response models
exception   → Custom exceptions
filter      → JWT authentication filter
config      → Spring Security configuration
util        → Utility classes
```

---

# Default Application Port

If not configured, Spring Boot runs on:

```
http://localhost:8080
```

You can change the port in:

```properties
server.port=8081
```

---

# Build Command Summary

```bash
git clone https://github.com/<your-username>/ecommerce-incture.git
cd ecommerce-incture
mvn clean install
mvn spring-boot:run
```

---

Your API server should now be running locally 🚀

# Database Schema

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price DOUBLE NOT NULL,
    stock INT NOT NULL,
    category VARCHAR(255),
    image_url VARCHAR(255),
    rating DOUBLE,
    active BOOLEAN NOT NULL
);

CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    total_price DOUBLE,
    user_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    total_amount DOUBLE NOT NULL,
    order_date TIMESTAMP NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

---

# Database Diagram

```
┌─────────────────────┐             ┌─────────────────────┐
│        users        │             │      products       │
├─────────────────────┤             ├─────────────────────┤
│ **id**              │             │ **id**              │
│ name                │             │ name                │
│ email               │             │ description         │
│ password            │             │ price               │
│ role                │             │ stock               │
│ active              │             │ category            │
└──────────┬──────────┘             │ image_url           │
           │                        │ rating              │
           │ user_id               │ active              │
           ▼                        └──────────┬──────────┘
┌─────────────────────┐                      │
│        carts        │                      │ product_id
├─────────────────────┤                      ▼
│ **id**              │             ┌─────────────────────┐
│ total_price         │             │      cart_items     │
│ user_id (FK)        │────────────▶│ **id**              │
└──────────┬──────────┘  cart_id    │ cart_id (FK)        │
                                   │ product_id (FK)     │
                                   │ quantity            │
                                   └─────────────────────┘


┌─────────────────────┐
│        users        │
└──────────┬──────────┘
           │ user_id
           ▼
┌─────────────────────┐
│        orders       │
├─────────────────────┤
│ **id**              │
│ user_id (FK)        │
│ total_amount        │
│ order_date          │
│ payment_status      │
│ order_status        │
└──────────┬──────────┘
           │ order_id
           ▼
┌─────────────────────┐
│     order_items     │
├─────────────────────┤
│ **id**              │
│ order_id (FK)       │
│ product_id (FK)     │──────────────▶ products.id
│ quantity            │
│ price               │
└─────────────────────┘
```

```
only give the table of content for above readme file
