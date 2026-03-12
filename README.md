# Invoice App

A Spring Boot application for managing invoices, line items, and payments.

## Prerequisites

- Java 21+
- Docker (optional)

## Run Application

```bash
./gradlew bootRun
```

Application runs at http://localhost:8080

## Run Tests

```bash
./gradlew test
```

## Run with Docker

```bash
docker-compose up --build
```

## API Endpoints

- `POST /api/invoices` - Create invoice
- `POST /api/invoices/list` - Get all invoices
- `POST /api/invoices/get` - Get invoice by ID
- `POST /api/invoices/{id}/pay` - Pay invoice
- `POST /api/invoices/{id}/refund` - Refund payment

## H2 Console

http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:invoicedb`
- Username: `sa`
- Password: (empty)
