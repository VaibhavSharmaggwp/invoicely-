# INVOICELY — Android-First Invoice & Payment Management SaaS

**INVOICELY** is a modern, event-driven SaaS backend for automated invoice generation, client management, payment tracking, and asynchronous notifications.

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

### 3. Asynchronous Event-Driven Architecture (Kafka)
- **Event Producer & Consumer:** Uses `spring-boot-starter-kafka` to publish `InvoiceCreatedEvent` asynchronously upon invoice creation.
- **Instant Response Times:** API returns `201 Created` instantly while background notification workers simulate email/WhatsApp delivery without blocking HTTP response threads.
- **Docker Compose (KRaft Mode):** Containerized Kafka server running without Zookeeper dependency.

### 4. Automated Reminders & Scheduling
- **Spring Scheduler (`@EnableScheduling`):** Periodically scans for overdue invoices (`InvoiceReminderScheduler`).
- Automatically triggers background Kafka notification events for due/overdue customer payments.

---

## 🛠️ Setup & Running Locally

### Prerequisites
- **Java 21** / OpenJDK 21
- **Docker & Docker Desktop** (for Kafka)
- **PostgreSQL 15+** (Running on port `5433` by default)
- **Maven 3.8+** (or included `./mvnw` wrapper)

---

### Step 1: Start Infrastructure (Kafka Container)

Navigate to the `backend` directory and launch Kafka using Docker Compose:

```bash
cd backend
docker-compose up -d
```

---

### Step 2: Environment Configuration

Set up environment variables in your system or `application-local.yml`:

```bash
export DB_URL=jdbc:postgresql://localhost:5433/invoicely_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_postgres_password
export GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

---

### Step 3: Run Backend Application

```bash
cd backend
./mvnw spring-boot:run
```

---

## 🧪 Testing with Postman

1. **Obtain Dev JWT Token:**
   ```http
   GET http://localhost:8080/api/v1/auth/dev-token?email=test@invoicely.com
   ```
2. **Create Invoice (Asynchronous Kafka Trigger):**
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
3. **Fetch Business Invoices:**
   ```http
   GET http://localhost:8080/api/v1/invoices
   Authorization: Bearer <YOUR_JWT_TOKEN>
   ```
