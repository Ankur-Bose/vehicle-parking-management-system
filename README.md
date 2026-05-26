# Vehicle Parking Management System

A full-stack, production-grade parking lot management application built with a **microservices architecture**. The system handles slot management, reservations, vehicle tracking, and automated billing — end to end.

---

## Architecture Overview

```
                        ┌─────────────────┐
                        │   Angular SPA   │
                        │  (Frontend)     │
                        └────────┬────────┘
                                 │ JWT in every request
                                 ▼
                        ┌─────────────────┐
                        │   API Gateway   │  ← JWT validation & role extraction
                        └────────┬────────┘
                                 │ Forwards role as header
              ┌──────────────────┼──────────────────────┐
              ▼                  ▼                       ▼
     ┌────────────────┐  ┌──────────────┐   ┌──────────────────┐
     │  User Service  │  │ Reservation  │   │  Slot Management │
     │   (MySQL DB)   │  │   Service    │   │    Service       │
     └────────────────┘  │  (MySQL DB)  │   │   (MySQL DB)     │
                         └──────┬───────┘   └──────────────────┘
                                │
              ┌─────────────────┼──────────────────┐
              ▼                                     ▼
   ┌──────────────────────┐           ┌─────────────────────┐
   │  Vehicle Logging     │           │   Invoice Service   │
   │     Service          │           │    (MySQL DB)       │
   │    (MySQL DB)        │           └─────────────────────┘
   └──────────────────────┘
              
              All services register with → [ Eureka Service Discovery ]
```

---

## Microservices Breakdown

| Service | Responsibility | Port |
|---|---|---|
| **API Gateway** | Single entry point; JWT validation; role-based routing | 8080 |
| **Eureka Server** | Service discovery & registry | 8761 |
| **User Service** | Authentication, registration, JWT issuance | 8081 |
| **Slot Management** | Slot CRUD, state machine, availability tracking | 8082 |
| **Reservation Service** | Booking lifecycle, start/end time management | 8083 |
| **Vehicle Logging** | Entry/exit event recording | 8084 |
| **Invoice Service** | Automated billing on vehicle exit | 8085 |

---

## Tech Stack

### Backend
- **Java 17** + **Spring Boot 3**
- **Spring Cloud Gateway** — API Gateway
- **Spring Cloud Netflix Eureka** — Service Discovery
- **Spring Security + JJWT** — Authentication & Authorization
- **OpenFeign** — Inter-service HTTP communication
- **Spring Data JPA + Hibernate** — ORM
- **MySQL** — Per-service isolated databases

### Frontend
- **Angular 17**
- **HTTP Interceptor** — Automatic JWT attachment
- **Route Guards** — Unauthenticated navigation prevention
- **Angular Services + RxJS** — State and async management

---

## Security Model

JWT authentication is handled **exclusively at the API Gateway**. The gateway:

1. Validates the incoming JWT token.
2. Extracts the user's **role** (`ADMIN`, `STAFF`, `CUSTOMER`).
3. Forwards the role as a **request header** to downstream services.

Individual microservices trust the header passed by the gateway — they never deal with JWT logic themselves. This keeps auth concerns cleanly centralized.

**Roles & Access:**

| Role | Capabilities |
|---|---|
| `ADMIN` | Full access — manage slots, view all reservations, generate reports |
| `STAFF` | Log vehicle entry/exit, view active reservations |
| `CUSTOMER` | Book slots, view own reservations and invoices |

---

## Slot State Machine

Instead of a simple boolean for availability, each parking slot is governed by a **three-state machine** to prevent double-booking:

```
   ┌─────────────────────────────────────────────┐
   │                                             │
   ▼                                             │
[ -1: AVAILABLE ] ──── booking starts ────► [ 0: BOOKING IN PROGRESS ]
                                                 │
                                           payment confirmed
                                                 │
                                                 ▼
                                         [ 1: OCCUPIED ]
                                                 │
                                          vehicle exits
                                                 │
                                                 ▼
                                        [ -1: AVAILABLE ]
```

**Why this matters:** The moment a booking request arrives, the slot **atomically transitions to `0`**. Any concurrent request sees a non-available slot and is rejected immediately — preventing race conditions and double-booking **without distributed locks**.

---

## Invoice Service — How It Works

The Invoice Service is the most complex service in the system. When a vehicle exits, it:

1. **Fires three parallel Feign calls** simultaneously:
   - → `Reservation Service` for the **booking start time**
   - → `Vehicle Logging Service` for the **exit timestamp**
   - → `Slot Management Service` for the **slot type** (e.g., compact, standard, large)

2. **Calculates the charge** based on duration × slot type rate.

3. **Persists the invoice** to its own database.

```
Vehicle Exit Event
        │
        ├──► [Reservation Service]   → start time
        ├──► [Vehicle Logging]       → exit time        ──► Invoice Calculation ──► Save
        └──► [Slot Management]       → slot type & rate
```

---

## Frontend Features

- **JWT interceptor** — automatically injects `Authorization: Bearer <token>` on every HTTP request
- **Route guards** — blocks access to protected pages if the user is unauthenticated or lacks the required role
- **Role-based UI** — navigation and components adapt based on `ADMIN`, `STAFF`, or `CUSTOMER` role
- **Real-time slot grid** — visual representation of lot availability

---

## Database Design

Each microservice owns its own isolated MySQL database — no shared schemas. Inter-service data needs are fulfilled through API calls (Feign), not direct DB joins.

| Service | Database |
|---|---|
| User Service | `user_db` |
| Slot Management | `slot_db` |
| Reservation Service | `reservation_db` |
| Vehicle Logging | `vehicle_log_db` |
| Invoice Service | `invoice_db` |

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+ & Angular CLI
- MySQL 8+
- Maven 3.8+

### 1. Clone the repository
```bash
git clone https://github.com/your-username/vehicle-parking-management.git
cd vehicle-parking-management
```

### 2. Set up databases
Create the five MySQL databases listed in the table above, then update each service's `application.yml` with your credentials.

### 3. Start services (in order)
```bash
# 1. Eureka Server
cd eureka-server && mvn spring-boot:run

# 2. Microservices (can be started in any order after Eureka)
cd user-service && mvn spring-boot:run
cd slot-service && mvn spring-boot:run
cd reservation-service && mvn spring-boot:run
cd vehicle-logging-service && mvn spring-boot:run
cd invoice-service && mvn spring-boot:run

# 3. API Gateway (last)
cd api-gateway && mvn spring-boot:run
```

### 4. Start the Angular frontend
```bash
cd frontend
npm install
ng serve
```

The app will be available at `http://localhost:4200`. All API calls go through the gateway at `http://localhost:8080`.

---

## Project Structure

```
vehicle-parking-management/
├── eureka-server/
├── api-gateway/
├── user-service/
├── slot-service/
├── reservation-service/
├── vehicle-logging-service/
├── invoice-service/
└── frontend/                  # Angular app
```

---

## Key Design Decisions

**1. Centralized JWT at the Gateway**
JWT validation at a single point eliminates code duplication across services and makes rotating secrets or swapping auth strategies a one-service change.

**2. Atomic Slot State Machine**
Using `-1 / 0 / 1` states instead of a boolean prevents race conditions in concurrent booking scenarios without requiring distributed locking infrastructure.

**3. Database-per-Service**
Strict data isolation ensures services can be scaled, deployed, and failed independently without cascading schema dependencies.

**4. Feign for Inter-Service Calls**
OpenFeign provides declarative, clean HTTP clients that integrate naturally with Eureka for service discovery — no hardcoded URLs anywhere.

---

## Author - Ankur Bose

Built as a full-stack portfolio project demonstrating microservices architecture, distributed system design patterns, and end-to-end security.
