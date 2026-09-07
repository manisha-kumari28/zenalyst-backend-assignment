# Zenalyst AI - Supplier Purchase Decision System

## Overview

This project implements a backend service that helps a store decide:

- What quantity to purchase
- Which supplier to purchase from
- Which suppliers should be rejected
- The applicable price
- Total purchase cost
- Why each decision was made

The system considers supplier:

- Price
- Delivery time
- Minimum order quantity
- Maximum supply capacity
- Volume pricing tiers

## Technology Stack

- Java
- Spring Boot
- Spring Data JPA
- H2 Database
- Maven

No external database or infrastructure is required.

## Architecture

The application follows a simple layered architecture:

Controller
↓
Service
↓
Repository
↓
H2 Database

### Controller

Exposes REST APIs for products, suppliers, offers, and purchase decisions.

### Service

Contains the purchase decision algorithm and business rules.

### Repository

Uses Spring Data JPA for database access.

### Database

H2 file-based database is used so the application can run without requiring
an external database while still preserving data across application restarts.

## How to Run

### Prerequisites

- Java 17+
- Maven or Maven Wrapper

### Start Application

Linux/macOS:

./mvnw spring-boot:run

Windows:

mvnw.cmd spring-boot:run

The application runs on:

http://localhost:8080

## H2 Console

H2 console:

http://localhost:8080/h2-console

JDBC URL:

jdbc:h2:file:./data/zenalystdb

Username:

sa

Password:

Leave blank.

## APIs

### Create Product

POST /api/products

Example:

{
"name": "Laptop"
}

### Get Products

GET /api/products

### Create Supplier

POST /api/suppliers

Example:

{
"name": "Supplier A",
"deliveryDays": 2,
"minimumOrderQuantity": 10,
"maximumSupplyQuantity": 200
}

### Get Suppliers

GET /api/suppliers

### Create Supplier Offer

POST /api/offers

Example:

{
"productId": 1,
"supplierId": 1,
"price": 80,
"minimumQuantity": 100,
"maximumQuantity": 200
}

### Get Offers

GET /api/offers

### Get Offers for Product

GET /api/offers/product/{productId}

### Make Purchase Decision

POST /api/purchase/decide

Example:

{
"productId": 1,
"requiredQuantity": 100,
"requiredByDate": "2026-09-15"
}

## Decision Algorithm

The system uses a greedy, explainable selection strategy.

1. Validate the purchase requirement.
2. Verify that the product exists.
3. Load supplier offers for the product.
4. Reject suppliers that cannot meet the delivery deadline.
5. For each available supplier, check:
    - Supplier minimum order quantity
    - Pricing tier minimum
    - Supplier maximum capacity
    - Pricing tier maximum
6. Select the feasible supplier with the lowest applicable price.
7. Purchase the maximum feasible quantity from that supplier.
8. Continue with the remaining requirement.
9. If the complete requirement cannot be fulfilled, return the remaining
   quantity as UNFULFILLED.

## Volume Pricing

Multiple SupplierOffer records can represent different pricing tiers.

Example:

| Quantity | Price |
|----------|-------|
| 1-49     | 100   |
| 50-99    | 90    |
| 100-200  | 80    |

For a requirement of 100 units, the 100-200 pricing tier can be selected.

## Explainability

Each decision contains:

- Supplier
- Quantity
- Price per unit
- Total cost
- Status
- Reason

Possible statuses:

- SELECTED
- REJECTED
- UNFULFILLED

This allows the purchasing team to understand why the system selected
or rejected a supplier.

## Assumptions

1. A SupplierOffer represents one pricing tier for a supplier and product.
2. Pricing tier minimum and maximum quantities apply to the purchase quantity
   from that supplier.
3. A supplier is selected at most once for a purchase requirement.
4. Delivery time is calculated as today's date plus the supplier's delivery
   days.
5. If the supplier cannot meet the required date, the supplier is rejected.
6. If the requirement cannot be completely fulfilled, the remaining quantity
   is returned as UNFULFILLED.
7. The decision algorithm is greedy and prioritizes the lowest feasible price.
8. The system does not reserve supplier inventory because real-time inventory
   information was not provided in the assignment.

## Limitations

The current implementation uses a greedy strategy rather than solving a
global optimization problem.

For a production system, the decision engine could be extended to consider:

- Global cost optimization
- Shipping costs
- Supplier reliability
- Inventory availability
- Multiple purchase orders
- Historical supplier performance
- Dynamic pricing
- Transactional order creation

## Design Choice

The implementation intentionally avoids unnecessary infrastructure such as
Kafka, Redis, Docker, or microservices because the assignment is focused on
supplier selection and purchase decision logic.