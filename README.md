# INVOICELY — Android-First Invoice & Payment Management SaaS

INVOICELY is an Android-first SaaS platform for invoice generation, client management, and automated payment tracking.

## 🚀 Current Progress

### Phase 1: Core Domain Entities & Persistence
- **Backend Architecture:** Spring Boot REST API
- **Data Persistence:** Spring Data JPA with PostgreSQL
- **Core Entities:**
  - `Business` — Business profiles & GST settings
  - `Customer` — Client records linked to businesses
  - `Invoice` — Invoice metadata, amounts, & lifecycle states
  - `InvoiceItem` — Line items per invoice
  - `InvoiceStatus` — Status tracking (`DRAFT`, `ISSUED`, `PARTIALLY_PAID`, `PAID`, `OVERDUE`, `VOID`)

### Phase 2: Authentication & Security (Current Stage)
- **Spring Security & Stateless JWT:** Custom `JwtService` and `JwtAuthenticationFilter`
- **Google OAuth 2.0 Integration:** Google ID Token verification via `AuthService`
- **Auth Endpoints:**
  - `POST /api/v1/auth/google` — Google OAuth Sign-In / Sign-Up
  - `POST /api/v1/auth/dev-token` — Development bypass token endpoint
- **Business Management Endpoints:**
  - `POST /api/v1/businesses` — Register business profile
  - `GET /api/v1/businesses/me` — Get authenticated business profile

## 🛠️ Setup & Configuration

### Prerequisites
- Java 21 / OpenJDK 21
- PostgreSQL 15+ (Running on port `5433` by default)
- Maven 3.8+

### Environment Variables
Configure your database credentials and Google OAuth client ID before running:
```bash
export DB_URL=jdbc:postgresql://localhost:5433/invoicely_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_postgres_password
export GOOGLE_CLIENT_ID=your_google_web_client_id.apps.googleusercontent.com
```

### Run Locally
```bash
cd backend
mvn spring-boot:run
```
