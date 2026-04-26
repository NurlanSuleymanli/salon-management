<div align="center">


# 💈 Salon Manager

###  Salon Management System

</div>

---

## 📖 Overview

**Salon Manager** is a production-ready, full-stack web application for managing salon operations — from customer bookings to barber scheduling and admin control. It features a **secure REST API** backend built with Spring Boot and a lightweight **multi-panel frontend** (Customer, Barber, Admin) built with vanilla HTML/CSS/JavaScript.

The system supports **role-based access control**, **JWT authentication with refresh tokens**, **rate limiting**, and **real-time availability slot calculation** for barbers.

---

## ✨ Features

### 🔐 Authentication & Security
- JWT-based authentication with **access + refresh tokens**
- Token **blacklisting** on logout (prevents token reuse)
- **Rate limiting** on login/register endpoints using Bucket4j (5 req/min per IP)
- Role-based access control: `ADMIN`, `BARBER`, `CUSTOMER`
- Password encryption with BCrypt
- CORS configuration for frontend integration

### 👤 Customer Panel
- Register & login securely
- Browse salons and available services
- View barbers by salon and filter by service
- Check **real-time available time slots** per barber for any date
- Create, view, and cancel reservations
- View full booking history

### 💈 Barber Panel
- View and manage personal profile
- Update offered services
- View daily/weekly **schedule** (all upcoming reservations)
- Update reservation statuses: `CONFIRMED`, `COMPLETED`, `NO_SHOW`, `CANCELLED`
- Manage personal working hours

### 🛠️ Admin Panel
- Full **CRUD** for salons, services, and barbers
- Manage all users (activate / deactivate accounts)
- View **all reservations** with pagination
- Assign barbers to salons
- Toggle barber active/inactive status
- Manage barber working hours per day

---

## 🏗️ Architecture

```
SalonManager/
├── src/
│   └── main/
│       ├── java/com/nurlansuleymanli/salonmanager/
│       │   ├── config/           # CORS & application configuration
│       │   ├── controller/       # REST API endpoints (7 controllers)
│       │   ├── exception/        # Global exception handling
│       │   ├── mapper/           # MapStruct DTO ↔ Entity mappers
│       │   ├── model/
│       │   │   ├── dto/          # Request & Response DTOs
│       │   │   ├── entity/       # JPA entities
│       │   │   └── enums/        # Role, ReservationStatus
│       │   ├── repository/       # Spring Data JPA repositories
│       │   ├── security/         # JWT filter, security config, logging
│       │   └── service/          # Business logic layer
│       └── resources/
│           ├── application.yaml  # App configuration
├── SalonWeb/                     # Frontend (Vanilla HTML/CSS/JS)
│   ├── index.html                # Landing page
│   ├── customer.html             # Customer dashboard
│   ├── barber.html               # Barber dashboard
│   ├── admin.html                # Admin dashboard
│   ├── css/                      # Stylesheets
│   └── js/                       # Panel-specific JS files
├── Dockerfile                    # Multi-stage Docker build
├── docker-compose.yml            # App + PostgreSQL orchestration
└── build.gradle                  # Gradle dependencies
```

---

## 🗄️ Data Model

| Entity | Key Fields |
|---|---|
| `User` | id, fullName, email, password, role, isActive |
| `Salon` | id, name, address, contactPhone (+994 format), contactEmail |
| `Barber` | id, user, salon, services, isActive |
| `BarberWorkingHour` | id, barber, dayOfWeek, startTime, endTime |
| `Service` | id, name, description, duration, price, salon |
| `Reservation` | id, customer, barber, service, date, startTime, endTime, status |
| `TokenBlacklist` | id, token (invalidated JWTs) |

**Reservation Statuses:** `PENDING` → `CONFIRMED` → `COMPLETED` / `CANCELLED` / `NO_SHOW`

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.3 |
| **Security** | Spring Security + JJWT 0.13.0 |
| **Database** | PostgreSQL 16 |
| **ORM** | Spring Data JPA / Hibernate |
| **Mapping** | MapStruct 1.5.5 |
| **Validation** | Jakarta Bean Validation |
| **Rate Limiting** | Bucket4j 8.10.1 |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) 2.8.5 |
| **Code Gen** | Lombok 1.18.30 |
| **Build Tool** | Gradle (Groovy DSL) |
| **Containerization** | Docker + Docker Compose |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript |

---

## ⚙️ Getting Started

### Prerequisites

Make sure you have the following installed:

- [Java 21+](https://adoptium.net/)
- [Docker & Docker Compose](https://www.docker.com/get-started)
- [Git](https://git-scm.com/)

---

## 📡 API Endpoints

### Auth (`/auth`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Register a new user |
| `POST` | `/auth/login` | Public | Login and receive JWT tokens |
| `POST` | `/auth/refresh` | Public | Refresh access token |
| `POST` | `/auth/logout` | Authenticated | Logout and blacklist token |

### Barbers (`/api/barbers`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/barbers/add` | ADMIN | Add a new barber |
| `PUT` | `/api/barbers/{id}/update` | ADMIN | Update barber info |
| `DELETE` | `/api/barbers/{id}/delete` | ADMIN | Remove a barber |
| `GET` | `/api/barbers/list` | ADMIN | List all barbers (paginated) |
| `GET` | `/api/barbers/{id}` | Authenticated | Get barber by ID |
| `GET` | `/api/barbers/salon/{salonId}` | Public | Get barbers by salon |
| `PUT` | `/api/barbers/{id}/status` | ADMIN | Toggle barber active status |
| `GET` | `/api/barbers/{id}/services` | Public | Get barber's offered services |
| `GET` | `/api/barbers/{id}/available-slots` | Public | Get available time slots for a date |
| `GET` | `/api/barbers/filter` | Public | Filter barbers by service |
| `PUT` | `/api/barbers/my-services` | BARBER | Update own offered services |
| `GET` | `/api/barbers/me` | BARBER | Get own barber profile |

### Reservations (`/api/reservations`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/reservations/create` | CUSTOMER | Create a reservation |
| `GET` | `/api/reservations/my-history` | CUSTOMER | View own booking history |
| `PUT` | `/api/reservations/{id}/cancel` | CUSTOMER | Cancel a reservation |
| `GET` | `/api/reservations/barber-schedule` | BARBER | View own schedule |
| `PUT` | `/api/reservations/{id}/status` | BARBER | Update reservation status |
| `GET` | `/api/reservations/all` | ADMIN | All reservations (paginated) |

### Services (`/api/services`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/services/list` | Public | List all services (paginated) |
| `GET` | `/api/services/salon/{salonId}/with-barbers` | Public | Services with barber info by salon |
| `GET` | `/api/services/barber/my-services` | BARBER | Get own salon's services |
| `POST` | `/api/services/add` | ADMIN | Create a new service |
| `PUT` | `/api/services/{id}/update` | ADMIN | Update a service |
| `DELETE` | `/api/services/{id}/delete` | ADMIN | Delete a service |

### Salons, Users & Working Hours

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/salons/add` | ADMIN | Create a salon |
| `GET` | `/api/salons/list` | Public | List all salons |
| `PUT` | `/api/salons/{id}/update` | ADMIN | Update salon info |
| `DELETE` | `/api/salons/{id}/delete` | ADMIN | Delete a salon |
| `GET` | `/api/users/list` | ADMIN | List all users |
| `PUT` | `/api/users/{id}/status` | ADMIN | Toggle user active status |
| `POST` | `/api/barbers/{id}/working-hours/add` | ADMIN | Set barber working hours |
| `GET` | `/api/barbers/{id}/working-hours` | Authenticated | Get barber working hours |

> 📄 **Full interactive documentation available at:** `http://localhost:8080/swagger-ui/index.html`

---

## 🔑 Authentication Flow

```
Client                          Server
  │                               │
  ├──── POST /auth/login ────────▶│
  │                               │  Validates credentials
  │◀─── { accessToken,           │  Generates JWT pair
  │        refreshToken } ────────│
  │                               │
  ├──── API Request + Bearer ────▶│
  │                               │  JwtAuthenticationFilter validates
  │◀─── 200 OK ───────────────────│
  │                               │
  ├──── POST /auth/refresh ──────▶│  (when access token expires)
  │◀─── { new accessToken } ──────│
  │                               │
  ├──── POST /auth/logout ───────▶│
  │                               │  Token added to blacklist
  │◀─── 200 OK ───────────────────│
```

---

## 🐳 Docker Configuration

The `docker-compose.yml` orchestrates two services:

```yaml
services:
  db:      # PostgreSQL 16 Alpine — port 5432
  app:     # Spring Boot App — port 8080
```

The application uses a **multi-stage Dockerfile**:
- **Stage 1** (`eclipse-temurin:21-jdk-alpine`): Builds the fat JAR via Gradle
- **Stage 2** (`eclipse-temurin:21-jre-alpine`): Runs only the JAR (smaller image)

---

## 📁 Environment Variables

When running with Docker Compose, these environment variables are configured automatically:

| Variable | Default Value | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/salon_booking` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |

> ⚠️ For production, replace these with secure values and use Docker secrets or a `.env` file.

---

## 🗃️ Database

- **Database:** `salon_booking`
- **Schema:** `public`
- **Engine:** PostgreSQL 16 (Alpine)
- **Persistence:** Docker named volume `postgres_data` (data survives container restarts)
---

## 👨‍💻 Author

<div align="center">

**Nurlan Suleymanli**

[![GitHub](https://img.shields.io/badge/GitHub-NurlanSuleymanli-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/NurlanSuleymanli)

</div>



