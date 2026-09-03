# 🧾 Invoicely - High-Performance Invoice & Financial Management Platform (Backend)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6-blue.svg)](https://spring.io/projects/spring-security)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event--Driven-red.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-Distributed%20Locking%20%26%20Caching-red.svg)](https://redis.io/)
[![Bucket4j](https://img.shields.io/badge/Bucket4j-Rate%20Limiting-yellow.svg)](https://github.com/bucket4j/bucket4j)
[![Razorpay](https://img.shields.io/badge/Razorpay-Payment%20Gateway-blue.svg)](https://razorpay.com/)

**Invoicely Backend** is an enterprise-grade RESTful backend microservice built with **Spring Boot 3 (Java 21)**. It provides end-to-end invoice management, automated payment reconciliation via **Razorpay Webhooks**, event-driven notifications using **Apache Kafka**, distributed locking and caching via **Redis**, and API rate-limiting via **Bucket4j**.

---

## 🌟 Key Features & Architecture Highlights

### 1. 🔐 Authentication & Multi-Tenancy
- **JWT Stateless Security:** Role-based access control and token-based authentication via Spring Security 6.
- **Google OAuth Integration:** Secure ID token verification for Google Sign-In.
- **Tenant Data Isolation:** Business-scoped queries ensuring full isolation across registered merchants.

### 2. 💳 Payment Integration & Automated Reconciliation
- **Razorpay Payment Links:** Automatic generation of unique Razorpay payment URLs for invoices.
- **HMAC-SHA256 Signature Verification:** Webhook security filter guarding against forgery or intrusion attempts.
- **Partial & Full Payment Ledger:** Automated payment history tracking (`PaymentHistory`), calculating cumulative paid balances to update status (`PAID` vs `PARTIALLY_PAID`).

### 3. ⚡ Event-Driven Microservices (Apache Kafka)
- **Asynchronous Event Streaming:** Decoupled producer (`InvoiceProducer`) and consumer (`NotificationConsumer`) handling events:
  - `InvoiceCreatedEvent`: Triggers automated email notifications with attached PDF invoices.
  - `PaymentReminderEvent`: Dispatches payment reminders to customers.

### 4. 🛡️ API Rate Limiting (Bucket4j + Redis)
- **Per-IP Rate Limiting:** Perimeter defense using `RateLimitFilter` powered by **Bucket4j**.
- **DDoS & Brute-Force Shield:** Throttles excessive requests returning `HTTP 429 Too Many Requests`.

### 5. 🚀 Redis Caching & Distributed Locking
- **Distributed Lock (`DistributedLockService`):** Prevents concurrent execution of background cron jobs across replicated backend nodes.
- **Dashboard Caching (`@Cacheable`):** Low-latency dashboard metric responses with automatic cache eviction on invoice or payment updates.

### 6. 📄 PDF Generation & Reporting
- **Flying Saucer / OpenPDF:** HTML & Thymeleaf template rendering into high-fidelity downloadable PDF invoices.
- **Financial Reports:** CSV and summary reporting for date ranges and invoice statuses (`ReportService`).

### 7. ⏰ Background Automation
- **Overdue Invoice Scheduler:** Cron job (`InvoiceReminderScheduler`) identifying past-due invoices and publishing reminder events to Kafka.

---

## 🛠️ Technology Stack

| Component | Technology |
| :--- | :--- |
| **Core Framework** | Java 21, Spring Boot 3.4.1 |
| **Security** | Spring Security 6, JJWT (JSON Web Token), BCrypt |
| **Database** | PostgreSQL, Spring Data JPA / Hibernate |
| **Messaging & Events** | Apache Kafka |
| **Caching & Locks** | Redis, Spring Data Redis |
| **Rate Limiting** | Bucket4j Core |
| **Payment Gateway** | Razorpay Java SDK |
| **Templating & PDF** | Spring Mail, Thymeleaf, Flying Saucer OpenPDF |
| **Build Tool** | Apache Maven |

---

## 📡 API Endpoint Overview

### 🔐 Authentication (`/api/v1/auth`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/signup` | Register a new Business account | Public |
| `POST` | `/api/v1/auth/login` | Login and receive JWT access token | Public |
| `POST` | `/api/v1/auth/google` | Authenticate via Google OAuth ID Token | Public |

### 📄 Invoices (`/api/v1/invoices`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/invoices` | Create a new invoice | Authenticated |
| `GET` | `/api/v1/invoices` | List invoices for authenticated Business | Authenticated |
| `GET` | `/api/v1/invoices/{id}` | Retrieve specific invoice details | Authenticated |
| `GET` | `/api/v1/invoices/{id}/pdf` | Generate & download invoice PDF | Authenticated |
| `POST` | `/api/v1/invoices/{id}/payment-link` | Generate Razorpay payment URL | Authenticated |

### 🌐 Public & Webhooks (`/api/v1/public`, `/api/v1/webhooks`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/public/invoices/{id}` | View public invoice details | Public |
| `POST` | `/api/v1/webhooks/razorpay` | Process Razorpay payment webhooks | Public (HMAC Verified) |

### 📊 Reports & Business (`/api/v1/reports`, `/api/v1/business`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/reports/summary` | Get financial dashboard summary | Authenticated |
| `GET` | `/api/v1/reports/export` | Download financial CSV report | Authenticated |
| `GET` | `/api/v1/business/profile` | Get current business profile | Authenticated |

---

## ⚙️ Getting Started

### Prerequisites
- **JDK 21** or higher
- **Maven 3.8+**
- **PostgreSQL** database instance
- **Redis Server**
- **Apache Kafka Cluster**
- **Razorpay API Keys** (Key ID & Secret)

### Installation & Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/VaibhavSharmaggwp/invoicely-.git
   cd invoicely-/backend
   ```

2. **Configure Environment Variables / `application.properties`:**
   Update your database credentials and API keys in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/invoicely_db
   spring.datasource.username=postgres
   spring.datasource.password=your_password

   app.razorpay.key-id=your_key_id
   app.razorpay.key-secret=your_key_secret
   app.razorpay.webhook-secret=your_webhook_secret

   spring.kafka.bootstrap-servers=localhost:9092
   spring.data.redis.host=localhost
   spring.data.redis.port=6379
   ```

3. **Build and Run Application:**
   ```bash
   mvn clean package -DskipTests
   mvn spring-boot:run
   ```
   The backend server will start on `http://localhost:8080`.

---

## 🛡️ License

This project is licensed under the MIT License.
