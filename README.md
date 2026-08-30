# INVOICELY — Android-First Invoice & Payment Management SaaS

**INVOICELY** is a modern, high-performance, event-driven SaaS backend for automated invoice generation, client management, payment tracking, Razorpay payment links, automated webhooks, asynchronous notifications, and real-time dashboard analytics.

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
  - `GET /api/v1/public/invoices/{id}` — Public link endpoint for customers to view invoice details & pay via Razorpay link without JWT authentication.

### 3. Razorpay Payment Gateway & Automated Webhooks
- **Dynamic Payment Links (`RazorpayService`):**
  - Auto-converts Rupee totals into Paise for precision math.
  - Passes custom notes containing `invoiceId` for seamless tracking.
  - Automatically sets link expiry matching invoice due dates.
  - Generated dynamically on public invoice views (`paymentUrl`).
- **Real-Time Webhook Processing (`WebhookController`):**
  - Endpoint: `POST /api/v1/webhooks/razorpay` (Public / Unauthenticated for Razorpay callbacks).
  - Cryptographic HMAC-SHA256 signature verification via Razorpay SDK (`Utils.verifySignature`).
  - Supports `payment.captured`, `order.paid`, and `payment_link.paid` events.
  - Automatically updates invoice status to `PAID`.
  - Evicts stale Redis dashboard summary cache so real-time metrics update instantly.

### 4. Asynchronous Event-Driven Architecture (Apache Kafka)
- **Event Producer & Consumer:** Uses `spring-boot-starter-kafka` to publish `InvoiceCreatedEvent` asynchronously upon invoice creation.
- **Instant Response Times:** API returns `201 Created` instantly while background notification workers simulate email/WhatsApp delivery without blocking HTTP response threads.
- **Docker Compose (KRaft Mode):** Containerized Kafka server running without Zookeeper dependency.

### 5. High-Performance Caching & Distributed Locking (Redis)
- **Spring Data Redis & Cache Management:** Configured `RedisCacheManager` with a 10-minute TTL and modern non-deprecated JSON serialization (`RedisSerializer.json()`).
- **Dashboard Summary Caching:** `@Cacheable(value = "dashboard_summary", key = "#userEmail")` speeds up dashboard metrics reads to sub-millisecond speeds.
- **Cache Eviction Strategy:** `@CacheEvict` automatically invalidates stale dashboard cache entries upon new invoice creation or Razorpay webhook settlement.
- **Distributed Concurrency Locks:** `DistributedLockService` using Redis atomic `SETNX` operations with TTL to prevent race conditions across distributed backend instances.

### 6. Automated Reminders & Scheduling
- **Spring Scheduler (`@EnableScheduling`):** Periodically scans for overdue invoices (`InvoiceReminderScheduler`).
- Automatically triggers background Kafka notification events for due/overdue customer payments.

---

## 🛠️ Setup & Running Locally

### Prerequisites
- **Java 21** / OpenJDK 21
- **Docker & Docker Desktop** (for Kafka and Redis)
- **PostgreSQL 15+** (Running on port `5433` by default)
- **Maven 3.8+** (or included `./mvnw` wrapper)
- **ngrok** (for local webhook testing)

---

### Step 1: Start Infrastructure (Kafka & Redis Containers)

Navigate to the `backend` directory and launch Kafka and Redis using Docker Compose:

```bash
cd backend
docker-compose up -d
```

---

### Step 2: Environment Configuration

Create a `.env` file in the project root directory (or update `application-local.yml`). 
> **Note:** `.env` and `application-local.yml` are ignored by `.gitignore` to keep credentials completely safe.

Use `.env.example` as a template:

```env
# Database & Infrastructure
DB_URL=jdbc:postgresql://localhost:5433/invoicely_db
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
REDIS_HOST=localhost
REDIS_PORT=6379

# Google Auth
GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com

# Razorpay Integration (Test/Dev Mode)
RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
RAZORPAY_WEBHOOK_SECRET=your_razorpay_webhook_secret
```

---

### Step 3: Local Webhook Tunneling (ngrok)

To test Razorpay webhooks locally on port `8080`:

```powershell
ngrok http 8080
```
Copy your forwarding HTTPS URL (e.g. `https://<hash>.ngrok-free.app`) and configure Webhook in Razorpay Dashboard:
- **Webhook URL:** `https://<hash>.ngrok-free.app/api/v1/webhooks/razorpay`
- **Secret:** Same value as `RAZORPAY_WEBHOOK_SECRET` in your `.env`.

---

### Step 4: Run Backend Application

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

5. **View Public Invoice with Razorpay Payment Link (No JWT Required):**
   ```http
   GET http://localhost:8080/api/v1/public/invoices/<INVOICE_UUID>
   ```
   *Response includes `"paymentUrl": "https://rzp.io/i/..."` for the customer to pay.*

---

## 📂 Repository Structure

```
INVOICELY/
├── .env.example                 # Environment configuration template
├── .gitignore                   # Git rules ignoring secrets (.env, local yml)
├── backend/
│   ├── docker-compose.yml       # Docker configuration for Kafka & Redis
│   ├── pom.xml                  # Maven dependencies (Spring Boot, Razorpay, Kafka, Redis, Security, JPA)
│   └── src/
│       └── main/
│           ├── java/com/invoicely/backend/
│           │   ├── config/      # Redis, Security & Razorpay Configurations
│           │   ├── controller/  # REST Endpoints (Auth, Invoice, PublicInvoice, WebhookController)
│           │   ├── dto/         # Request & Response Data Transfer Objects
│           │   ├── entity/      # JPA Entities
│           │   ├── repository/  # Data Access Interfaces
│           │   └── Service/     # InvoiceService, PaymentService, RazorpayService & DistributedLockService
│           └── resources/       # Application properties & profiles
└── README.md                    # Project documentation
```
