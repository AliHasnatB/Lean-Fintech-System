# 💳 LeanBackend — Secure Fintech & Digital Wallet System

<p align="center">
  A secure, organization-oriented digital payment and wallet management platform built with Java and Spring Boot.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/Spring_Boot-Backend-6DB33F?style=for-the-badge&logo=springboot" />
  <img src="https://img.shields.io/badge/Spring_Security-Authentication-6DB33F?style=for-the-badge&logo=springsecurity" />
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Architecture-Layered-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/API-REST-informational?style=flat-square" />
  <img src="https://img.shields.io/badge/Testing-JUnit%20%7C%20Mockito-brightgreen?style=flat-square" />
</p>

---

## 📌 Overview

**LeanBackend** is a backend-focused fintech application designed to provide a secure digital payment environment for authorized users within an organization.

The application enables users to register, authenticate, manage digital wallet accounts, transfer funds, and monitor financial transactions through RESTful APIs.

The system focuses on secure authentication, authorization, transaction management, and maintainable backend architecture using the Spring Boot ecosystem.

## ✨ Core Features

### 🔐 Authentication & Security
- User registration and login.
- JWT-based authentication.
- OTP-based account verification.
- Password encryption using Spring Security.
- Role-based access control (RBAC).
- Protected REST API endpoints.

### 💰 Digital Wallet Management
- Create and manage wallet accounts.
- Retrieve wallet balance.
- Deposit and manage funds.
- Transfer money between authorized users.
- Validate transfer amounts and available balances.

### 📊 Transaction Management
- Record financial transactions.
- Track sender and receiver information.
- Maintain transaction history.
- Retrieve transaction summaries.

### 👤 User Management
- Organization-specific user registration.
- User profile and account management.
- Role assignment and authorization.
- User account status management.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Core programming language |
| Spring Boot | Backend application framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| MySQL | Relational database |
| JWT | Stateless authentication |
| Maven | Dependency and build management |
| Lombok | Boilerplate code reduction |
| ModelMapper | Object mapping |
| Jakarta Validation | Request validation |
| JUnit 5 | Unit testing |
| Mockito | Mock-based testing |

---

## 🏗️ Project Architecture

The application follows a layered architecture to maintain separation of concerns, readability, and maintainability.

```text
src/main/java/com/fintech/leanbackend
│
├── config/
│   ├── SecurityConfig.java
│   ├── JwtAuthFilter.java
│   ├── AsyncConfig.java
│   └── RoleInitializer.java
│
├── controller/
│   ├── AccountController.java
│   ├── AuthController.java
│   ├── OtpController.java
│   └── TransactionController.java
│
├── dto/
│   └── Request and Response DTOs
│
├── entites/
│   ├── Account.java
│   ├── Users.java
│   ├── Role.java
│   └── Transaction.java
│
├── exceptions/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
│
├── repository/
│   ├── AccountRepository.java
│   ├── UserRepository.java
│   └── TransactionRepository.java
│
├── security/
│   ├── CustomUserDetailsService.java
│   └── SecurityUtils.java
│
├── service/
│   ├── AccountService.java
│   ├── AuthService.java
│   ├── OtpService.java
│   └── TransactionService.java
│
├── service/implementation/
│   └── Service Implementations
│
├── utils/
│   ├── JwtUtils.java
│   └── KeyGenerator.java
│
└── validation/
    └── Custom Request Validators
```

### Architecture Responsibilities

- **Controller Layer:** Handles incoming HTTP requests and API responses.
- **Service Layer:** Implements business logic and application workflows.
- **Repository Layer:** Handles database communication through Spring Data JPA.
- **Entity Layer:** Represents database entities and relationships.
- **DTO Layer:** Transfers structured data between API clients and the application.
- **Security Layer:** Manages authentication and authorization.
- **Exception Layer:** Provides centralized exception handling.

---

## 🔒 Security Implementation

Security is an important part of the application because it handles financial operations and user account information.

The project implements:

- JWT-based authentication.
- Password hashing and secure password handling.
- OTP verification workflow.
- Role-based authorization.
- Input validation.
- Authenticated fund transfer operations.
- Server-side balance validation.

> Sensitive credentials, JWT secrets, and database passwords should be supplied through environment variables or external configuration rather than committed to the repository.

---

## 🧪 Testing

The project includes unit testing using JUnit 5 and Mockito.

Testing focuses on:

- Authentication service behavior.
- User registration validation.
- Role assignment.
- Transaction processing.
- Money transfer validation.
- Insufficient balance scenarios.
- Exception handling.

---

## ⚙️ Installation & Setup

### Prerequisites

- Java 17 or later (use the version configured in `pom.xml`).
- MySQL.
- Git.
- IntelliJ IDEA or another Java IDE.

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/LeanBackend.git
```

### 2. Navigate to the Project

```bash
cd LeanBackend
```

### 3. Configure Database

Create a MySQL database and configure your application properties using your local configuration.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/leanbackend
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Configure the remaining required application properties and secrets using your environment.

### 4. Build the Application

Linux / macOS:

```bash
./mvnw clean install
```

Windows:

```bash
mvnw.cmd clean install
```

### 5. Run the Application

```bash
./mvnw spring-boot:run
```

The backend will start on the configured application port.

---

## 🔌 API Modules

| Module | Description |
|---|---|
| Authentication | Registration and login |
| OTP | Account verification |
| Account | Wallet account management |
| Transactions | Deposits, transfers and transaction history |
| User Management | User information and account status |

---

## 🚀 Future Improvements

- Integration with external payment gateways.
- Email and SMS notification services.
- Advanced transaction analytics.
- Audit logging and monitoring.
- API documentation using Swagger / OpenAPI.
- Docker containerization.
- Integration testing.
- Rate limiting and enhanced fraud detection.

---

## 👨‍💻 Developer

**Ali Hasnat**

Java Backend Developer | Spring Boot | WebFlux

Interested in building secure, scalable backend systems and practical software engineering solutions.

---

<p align="center">
  <b>Built with Java & Spring Boot ❤️</b>
</p>