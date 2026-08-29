# INVOICELY — Android-First Invoice & Payment Management SaaS

**INVOICELY** is a modern, high-performance, event-driven SaaS backend for automated invoice generation, client management, payment tracking, asynchronous notifications, and real-time dashboard analytics.

---

## 🚀 Key Features & Architecture

### 1. Core Domain Entities & Persistence
- **Framework:** Spring Boot 3 / Java 21 with Spring Data JPA & PostgreSQL
- **Entities & Lifecycle States:**
  - `Business` — Business profiles & GST settings
  - `Customer` — Multi-tenant client records scoped to specific businesses
  - `Invoice` — Invoice metadata, dynamic total calculation, and status lifecycle (`DRAFT`, `ISSUED`, `PARTIALLY_PAID`, `PAID`, `OVERDUE`, `VOID`)
  - `InvoiceItem` — Line items per invoice

### 2. Authentication & Security
- **Spring Security & Stateless JWT:** Custom `JwtService` and `JwtAuthenticationFilter`
- **Google OAuth 2.0 Integration:** Verification of Google ID Tokens via `AuthService`
- **Auth & Business Endpoints:**
  - `POST /api/v1/auth/google` — Google OAuth Sign-In / Sign-Up
  - `GET /api/v1/auth/dev-token` — Dev token generator endpoint for rapid API testing
  - `POST /api/v1/businesses` — Register business profile
  - `GET /api/v1/businesses/me` — Fetch authenticated business profile

### 3. Asynchronous Event-Driven Architecture (Apache Kafka)
- **Event Producer & Consumer:** Uses `spring-boot-starter-kafka` to publish `InvoiceCreatedEvent` asynchronously upon invoice creation.
- **Instant Response Times:** API returns `201 Created` instantly while background notification workers simulate email/WhatsApp delivery without blocking HTTP response threads.
- **Docker Compose (KRaft Mode):** Containerized Kafka server running without Zookeeper dependency.

### 4. High-Performance Caching & Distributed Locking (Redis)
- **Spring Data Redis & Cache Management:** Configured `RedisCacheManager` with a 10-minute TTL and modern non-deprecated JSON serialization (`RedisSerializer.json()`).
- **Dashboard Summary Caching:** `@Cacheable(value = "dashboard_summary", key = "#userEmail")` speeds up dashboard metrics reads to sub-millisecond speeds.
- **Cache Eviction Strategy:** `@CacheEvict` automatically invalidates stale dashboard cache entries upon new invoice creation.
- **Distributed Concurrency Locks:** `DistributedLockService` using Redis atomic `SETNX` operations with TTL to prevent race conditions across distributed backend instances.

### 5. Automated Reminders & Scheduling
- **Spring Scheduler (`@EnableScheduling`):** Periodically scans for overdue invoices (`InvoiceReminderScheduler`).
- Automatically triggers background Kafka notification events for due/overdue customer payments.

---

## 🛠️ Setup & Running Locally

### Prerequisites
- **Java 21** / OpenJDK 21
- **Docker & Docker Desktop** (for Kafka and Redis)
- **PostgreSQL 15+** (Running on port `5433` by default)
- **Maven 3.8+** (or included `./mvnw` wrapper)

---

### Step 1: Start Infrastructure (Kafka & Redis Containers)

Navigate to the `backend` directory and launch Kafka and Redis using Docker Compose:

```bash
cd backend
docker-compose up -d
```

---

### Step 2: Environment Configuration

Set up environment variables in your system or `application-local.yml` (Note: `application-local.yml` is git-ignored for security):

```bash
export DB_URL=jdbc:postgresql://localhost:5433/invoicely_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_postgres_password
export GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

---

### Step 3: Run Backend Application

```bash
cd backend
./mvnw spring-boot:run
```

---

## 🧪 Testing Endpoints with Postman

1. **Obtain Dev JWT Token:**
   ```http
   GET http://localhost:8080/api/v1/auth/dev-token?email=test@invoicely.com
   ```

2. **Fetch Cached Dashboard Summary:**
   ```http
   GET http://localhost:8080/api/v1/invoices/dashboard-summary
   Authorization: Bearer <YOUR_JWT_TOKEN>
   ```

3. **Create Invoice (Triggers Asynchronous Kafka Event & Invalidates Cache):**
   ```http
   POST http://localhost:8080/api/v1/invoices
   Authorization: Bearer <YOUR_JWT_TOKEN>
   Content-Type: application/json

   {
     "customerId": "<YOUR_CUSTOMER_UUID>",
     "issueDate": "2026-08-29",
     "dueDate": "2026-09-15",
     "items": [
       {
         "description": "Web Development Services",
         "quantity": 1,
         "unitPrice": 15000.00
       }
     ]
   }
   ```

4. **Fetch Business Invoices:**
   ```http
   GET http://localhost:8080/api/v1/invoices
   Authorization: Bearer <YOUR_JWT_TOKEN>
   ```

---

## 📂 Repository Structure

```
INVOICELY/
├── backend/
│   ├── docker-compose.yml       # Docker configuration for Kafka & Redis
│   ├── pom.xml                  # Maven dependencies (Spring Boot, Kafka, Redis, Security, JPA)
│   └── src/
│       └── main/
│           ├── java/com/invoicely/backend/
│           │   ├── config/      # Redis & Security Configurations
│           │   ├── controller/  # REST Endpoints (Auth, Invoice, Business)
│           │   ├── dto/         # Request & Response Data Transfer Objects
│           │   ├── model/       # JPA Entities
│           │   ├── repository/  # Data Access Interfaces
│           │   └── Service/     # Business Logic & Distributed Lock Service
│           └── resources/       # Application properties & profiles
└── README.md                    # Project documentation
```
