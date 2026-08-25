# INVOICELY — Android-First Invoice & Payment Management SaaS

INVOICELY is an Android-first SaaS platform for invoice generation, client management, and automated payment tracking.

## 🚀 Current Progress (Phase 1)
- **Backend Architecture:** Spring Boot REST API
- **Data Persistence:** Spring Data JPA with PostgreSQL
- **Core Domain Entities:**
  - `Business` - Business profiles & GST settings
  - `Customer` - Client records linked to businesses
  - `Invoice` - Invoice metadata, amounts, & lifecycle states
  - `InvoiceItem` - Line items per invoice
  - `InvoiceStatus` - Status tracking (`DRAFT`, `ISSUED`, `PARTIALLY_PAID`, `PAID`, `OVERDUE`, `VOID`)

## 🛠️ Setup & Configuration

### Prerequisites
- JDK 21
- PostgreSQL 15+
- Maven 3.8+

### Environment Variables
Configure your database credentials before running the application:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/invoicely_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

### Run Locally
```bash
cd backend
mvn spring-boot:run
```
