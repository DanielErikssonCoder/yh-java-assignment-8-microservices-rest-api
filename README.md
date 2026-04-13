# Microservice REST API

A cinema management microservice for the Wigell Portal, handling customers, movies, screenings, bookings, and tickets with full Keycloak JWT authentication and role-based access control, built with Java 24 and Spring Boot 3.5.

[![Java](https://img.shields.io/badge/Java-24-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6-6DB33F.svg)](https://spring.io/projects/spring-security)
[![Keycloak](https://img.shields.io/badge/Auth-Keycloak_26.3-4A90D9.svg)](https://www.keycloak.org/)
[![MySQL](https://img.shields.io/badge/Database-MySQL_8.0-4479A1.svg)](https://www.mysql.com/)

## Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
- [Project Structure](#project-structure)
- [Architecture & Design](#architecture--design)
- [How to Run](#how-to-run)
- [Why a Raspberry Pi?](#why-a-raspberry-pi)
- [Group Infrastructure & Deployment](#group-infrastructure--deployment)
- [Usage](#usage)
- [Technical Implementation](#technical-implementation)

## About the Project

Built as part of a group project in 'Backend Development' for the YH education in Java System Development, this microservice is one of four services in the Wigell Portal. It handles everything related to cinema operations: managing customers, rooms, movies, screenings, bookings (room rentals), and ticket purchases.

The service integrates with a shared **Keycloak 26.3** instance for authentication and authorization, a shared **MySQL 8.0** database, a **Spring Cloud Gateway** for routing, and a **Spring Boot Admin** dashboard for monitoring. All of this shared infrastructure runs on a **Raspberry Pi 5** that I set up as a central server for the group, with all four developers connected over **Tailscale VPN**. More on that further down.

The API supports two roles, **ADMIN** and **USER**, each with their own set of endpoints and access restrictions. It demonstrates proper use of **Spring Data JPA**, **Keycloak JWT resource server**, **DTO-based request handling**, **global exception handling** with correct HTTP status codes, and automatic **SEK to USD currency conversion** via a shared library.

## Features

### Core Functionality

- **Full customer lifecycle**: Create, read, update, and delete customers, with automatic Keycloak account provisioning and cleanup.
- **Movie management**: CRUD for movies with age limit, genre, and length. Deletion blocked if screenings exist.
- **Room management**: CRUD for cinema rooms with capacity and technical equipment. Deletion blocked if bookings or screenings exist.
- **Screenings**: Link movies to rooms with date, time, and price per ticket. Deletion blocked if tickets have been sold.
- **Room bookings**: Customers can reserve an entire room for a private event. Capacity validation and ownership enforcement included.
- **Ticket purchasing**: Customers can buy tickets for screenings. Price returned in both SEK and USD.
- **Address management**: Add and remove addresses on customers, with cross-customer ownership validation.

### Security

- **Keycloak JWT authentication** throughout. All endpoints require a valid Bearer token.
- Roles extracted from `realm_access.roles` in the JWT via a custom `KeycloakRealmRoleConverter`.
- **ADMIN** role required for all management operations (customers, rooms, movies, screenings).
- **USER** role required for customer-facing operations (bookings, tickets, browsing movies and screenings).
- Ownership enforcement on bookings: users cannot modify another customer's booking (403 Forbidden).
- Each customer automatically gets a dedicated Keycloak account (`cinema-c{n}`) on creation, deleted on removal.

### Logging

- Info-level logging to file on all create, update, and delete operations.
- Log file: `logs/wigell-cinema.log`

### Currency Conversion

- All prices returned in both **SEK** and **USD**.
- Conversion handled by the shared group library `com.aspman:gruppe-shared-lib:1.0-SNAPSHOT`.

### Monitoring

- Registered with **Spring Boot Admin** dashboard at startup.
- Actuator endpoints exposed: `health`, `info`, and all management endpoints.

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com.danielerikssoncoder.cinema_project/
│   │       ├── CinemaProjectApplication.java
│   │       │
│   │       ├── config/
│   │       │   ├── DataSeeder.java                  # Seeds 5 customers, 3 rooms, 5 movies, screenings, bookings
│   │       │   └── SecurityConfig.java              # JWT resource server, role-based access, KeycloakRealmRoleConverter
│   │       │
│   │       ├── controller/
│   │       │   ├── CustomerController.java          # /api/v1/customers - ADMIN CRUD + address management
│   │       │   ├── MovieController.java             # /api/v1/movies - ADMIN write, USER read
│   │       │   ├── RoomController.java              # /api/v1/rooms - ADMIN only
│   │       │   ├── ScreeningController.java         # /api/v1/screenings - ADMIN write, USER read
│   │       │   ├── BookingController.java           # /api/v1/bookings - USER, ownership enforced
│   │       │   └── TicketController.java            # /api/v1/tickets - USER, customerId from JWT
│   │       │
│   │       ├── dto/
│   │       │   ├── request/
│   │       │   │   ├── AddressRequest.java
│   │       │   │   ├── BookingRequest.java
│   │       │   │   ├── BookingUpdateRequest.java
│   │       │   │   ├── CustomerRequest.java
│   │       │   │   ├── MovieRequest.java
│   │       │   │   ├── RoomRequest.java
│   │       │   │   ├── ScreeningRequest.java
│   │       │   │   └── TicketRequest.java           # No customerId field - set from JWT in controller
│   │       │   └── response/
│   │       │       ├── AddressResponse.java
│   │       │       ├── BookingResponse.java         # Includes totalPriceSek + totalPriceUsd
│   │       │       ├── CustomerResponse.java
│   │       │       ├── MovieResponse.java
│   │       │       ├── RoomResponse.java
│   │       │       ├── ScreeningResponse.java
│   │       │       └── TicketResponse.java          # Includes totalPriceSek + totalPriceUsd
│   │       │
│   │       ├── entity/
│   │       │   ├── Customer.java                    # keycloakId + keycloakUsername columns
│   │       │   ├── Address.java
│   │       │   ├── Movie.java
│   │       │   ├── Room.java
│   │       │   ├── Screening.java
│   │       │   ├── Booking.java
│   │       │   └── Ticket.java
│   │       │
│   │       ├── exception/
│   │       │   └── GlobalExceptionHandler.java      # Maps exceptions to correct HTTP status codes
│   │       │
│   │       ├── repository/
│   │       │   ├── CustomerRepository.java          # findByKeycloakId, findMaxKeycloakNumber, EntityGraph methods
│   │       │   ├── AddressRepository.java
│   │       │   ├── MovieRepository.java
│   │       │   ├── RoomRepository.java
│   │       │   ├── ScreeningRepository.java
│   │       │   ├── BookingRepository.java
│   │       │   └── TicketRepository.java
│   │       │
│   │       └── service/
│   │           ├── AuthService.java                 # getCurrentCustomer(), verifyOwnership()
│   │           ├── KeycloakService.java             # Keycloak Admin REST API - createUser(), deleteUser()
│   │           ├── CurrencyService.java             # SEK to USD via shared lib
│   │           ├── CustomerService.java
│   │           ├── MovieService.java
│   │           ├── RoomService.java
│   │           ├── ScreeningService.java
│   │           ├── BookingService.java
│   │           └── TicketService.java
│   │
│   └── resources/
│       └── application.yaml
│
└── test/
    └── java/
        └── com.danielerikssoncoder.cinema_project/
            └── CinemaProjectApplicationTests.java
```

## Architecture & Design

### Multi-Layered Architecture

**1. Controller Layer**
- Handles HTTP routing and request/response mapping.
- Delegates all business logic to the service layer.
- Uses DTOs for inbound data. Raw entities are never accepted directly from clients.
- `customerId` is always resolved from the JWT, never trusted from the request body.

**2. Service Layer**
- Validates business rules (duplicate usernames, capacity limits, conflict checks before delete).
- Calls `KeycloakService` to provision/deprovision Keycloak accounts during customer lifecycle.
- Applies currency conversion to all price fields before returning responses.

**3. Repository Layer**
- Extends `JpaRepository` for standard CRUD.
- `CustomerRepository` uses `@EntityGraph` methods for eager-loading relations, split into separate methods (`findWithBookingsById`, `findWithTicketsById`) to avoid Hibernate's `MultipleBagFetchException`.
- `findMaxKeycloakNumber()` returns the highest `cinema-c{n}` index, ensuring sequential account naming even after deletions.

**4. Persistence Layer**
- MySQL 8.0 in Docker, shared across all group microservices.
- Schema managed by Hibernate `ddl-auto: update`.

### Design Decisions

- **Keycloak account per customer**: Every customer in the database has a dedicated Keycloak account (`cinema-c{n}`). `KeycloakService.createUser()` provisions the account before the customer is saved: username uniqueness is validated first to avoid orphaned Keycloak users on rollback.
- **JWT-based identity**: `AuthService.getCurrentCustomer()` resolves the logged-in customer by matching the JWT `sub` claim against `Customer.keycloakId`. No session state is maintained.
- **Ownership enforcement**: `AuthService.verifyOwnership()` compares the resource owner's `keycloakId` against the token's `sub`. Mismatch throws `AccessDeniedException` (caught as 403).
- **Conflict-safe deletion**: Movies, rooms, and screenings check for dependent records before deletion and return 409 Conflict if any exist. Customers with active bookings or tickets are likewise protected.
- **Shared currency library**: Exchange rates and conversion logic live in the group's shared lib, keeping the service decoupled from the implementation.

## How to Run

There are two ways to run this project depending on how much of the stack you want to set up.

### Option 1: Running the Cinema Service Only

This assumes you already have Keycloak and MySQL running somewhere and just want to run the cinema service against them.

**Prerequisites**

- **Java 24**
- **Maven 3.9+** (or use the included `mvnw` wrapper)
- **Keycloak 26.3** instance with realm `grupp-e` and client `grupp-e-client` configured
- **MySQL 8.0** with database `wigell_cinema_db`
- **gruppe-shared-lib** installed locally:

```powershell
git clone https://github.com/ClassicWorks/GruppESharedLib
cd GruppESharedLib
.\mvnw.cmd install
```

**Configuration**

Update `src/main/resources/application.yaml` with your environment's values:

```yaml
server:
  port: 8583
  address: 0.0.0.0
 
spring:
  datasource:
    url: jdbc:mysql://<MYSQL_HOST>:<MYSQL_PORT>/wigell_cinema_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: root
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://<KEYCLOAK_HOST>:<KEYCLOAK_PORT>/realms/grupp-e
          jwk-set-uri: http://<KEYCLOAK_HOST>:<KEYCLOAK_PORT>/realms/grupp-e/protocol/openid-connect/certs
  boot:
    admin:
      client:
        url: http://<DASHBOARD_HOST>:9090
        instance:
          service-base-url: http://<THIS_SERVICE_IP>:8583
```

**Build & Run**

```powershell
git clone https://github.com/DanielErikssonCoder/wigell-cinema.git
cd wigell-cinema
.\mvnw.cmd spring-boot:run
```

The application starts on `http://localhost:8583`. On startup, the `DataSeeder` populates the database with 5 customers, 3 rooms, 5 movies, screenings, and bookings if the tables are empty.

**Getting a Token**

```powershell
$token = (Invoke-RestMethod -Method Post `
  -Uri "http://<KEYCLOAK_HOST>:8081/realms/grupp-e/protocol/openid-connect/token" `
  -Body @{
    grant_type = "password"
    client_id  = "grupp-e-client"
    username   = "cinema-c1"
    password   = "password"
  }).access_token
```

Use the token as a Bearer token in all subsequent requests.

### Option 2: Running the Full Stack (Gateway, Keycloak, Dashboard, MySQL)

If you want to run the complete infrastructure, including the gateway, Keycloak, the Spring Boot Admin dashboard, and the shared MySQL instance, the full Docker Compose setup is available in a separate repository:

**[yh-java-assignment-8-microservices-rest-api-pi-stack](https://github.com/DanielErikssonCoder/yh-java-assignment-8-microservices-rest-api-pi-stack)**

That repository contains the gateway and dashboard source code along with the Docker Compose configuration and database init scripts. It includes instructions for standing up the entire shared infrastructure. Once it is running, come back to Option 1 above and point your `application.yaml` at the stack's host.

## Why a Raspberry Pi?

Early in the project it became clear that coordinating four developers across different machines was going to be a pain. Everyone needed the same Keycloak realm, the same database, and eventually a shared gateway. The obvious solution of "just run it locally" meant every person would spend hours configuring the same things independently, with no guarantee the setups would actually match.

I already had a Raspberry Pi 5 running at home as a home server, so I figured I could put it to better use. The idea was simple: set up Keycloak, MySQL, the gateway, and the dashboard once, on a single machine, and let everyone connect to it. That way the group could focus on writing their own microservices instead of fighting with auth configuration.

To make this work across four different home networks without any of us having to deal with port forwarding or exposing anything to the public internet, I set up **Tailscale VPN**. Each group member joined my Tailscale network, which gave everyone a stable private IP regardless of where they were or what network they were on. The Pi became the hub, and everyone's computer became a node. I also wrote a custom ACL policy so that group members could only reach the shared services on the Pi, not each other's machines directly, which kept things clean and secure.

The entire shared stack runs in **Docker Compose** on the Pi. MySQL initializes all four databases on first boot via an init script, Keycloak starts against that MySQL instance, and the gateway and dashboard build from source via Dockerfiles. Configuration for the gateway (routing rules, Tailscale IPs) lives as a bind-mounted YAML file so it can be updated without rebuilding the image.

The setup paid off during development. Anyone in the group could boot up their microservice, point it at the Pi, and have a fully working auth and routing layer immediately. For the final presentation, all four services were running live simultaneously, routed through the same gateway, visible in the same dashboard, authenticated by the same Keycloak instance.

## Group Infrastructure & Deployment

During the project, all four microservices ran simultaneously as part of a shared distributed system. Rather than running everything locally, the group set up a centralized server environment on a **Raspberry Pi 5 (16 GB RAM)** hosting all shared infrastructure in Docker Compose.

### Shared Infrastructure (Raspberry Pi 5)

| Component | Technology | Purpose |
|-----------|-----------|---------|
| API Gateway | Spring Cloud Gateway 2025.0.1 | Single entry point, routes requests to each microservice |
| Auth Server | Keycloak 26.3.3 | JWT authentication and role-based authorization |
| Database | MySQL 8.0 | Shared database server, one schema per microservice |
| Dashboard | Spring Boot Admin 3.5.8 | Real-time monitoring of all registered microservices |

### Networking

All four group members ran their microservices on their own machines. The services were connected to each other and to the Pi over **Tailscale VPN**, creating a private mesh network without any port forwarding or public exposure. The gateway on the Pi routes incoming requests to each member's machine by Tailscale IP.

```
Client (Postman)
      │
      ▼
Spring Cloud Gateway  (Raspberry Pi :8080)
      │
      ├──▶ Wigell Cinema   (Daniel's PC     :8583)
      ├──▶ Wigell Travels  (William's PC    :8581)
      ├──▶ Wigell Sushi    (Oskar's PC      :8582)
      └──▶ Wigell MC       (Chansamone's PC :8585)
```

All gateway routes share the same Keycloak instance for authentication. The gateway forwards the JWT to each microservice, which validates it independently as an OAuth2 resource server.

### Shared Library

Currency conversion is handled by a shared Maven library maintained by the group:
[GruppESharedLib](https://github.com/ClassicWorks/GruppESharedLib) provides SEK to target currency conversion used by all four services.

## Usage

### USER Endpoints

All requests require a Bearer token for a user with the `USER` role.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/movies` | List all movies |
| GET | `/api/v1/movies/{id}` | Get a specific movie |
| GET | `/api/v1/screenings` | List all screenings |
| GET | `/api/v1/screenings?movieId={id}&date={YYYY-MM-DD}` | Filter screenings by movie and/or date |
| POST | `/api/v1/bookings` | Reserve a room (customerId set from JWT) |
| PATCH | `/api/v1/bookings/{id}` | Update own booking (date, technical equipment) |
| GET | `/api/v1/bookings?customerId={id}` | List own bookings |
| POST | `/api/v1/tickets` | Buy tickets for a screening (customerId set from JWT) |
| GET | `/api/v1/tickets?customerId={id}` | List own tickets |

### ADMIN Endpoints

All requests require a Bearer token for a user with the `ADMIN` role.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/customers` | List all customers |
| GET | `/api/v1/customers/{id}` | Get a specific customer |
| POST | `/api/v1/customers` | Create customer + Keycloak account |
| PUT | `/api/v1/customers/{id}` | Update customer |
| DELETE | `/api/v1/customers/{id}` | Delete customer + Keycloak account |
| POST | `/api/v1/customers/{id}/addresses` | Add address to customer |
| DELETE | `/api/v1/customers/{id}/addresses/{addressId}` | Remove address |
| POST | `/api/v1/movies` | Create movie |
| DELETE | `/api/v1/movies/{id}` | Delete movie (409 if screenings exist) |
| GET | `/api/v1/rooms` | List all rooms |
| GET | `/api/v1/rooms/{id}` | Get a specific room |
| POST | `/api/v1/rooms` | Create room |
| PUT | `/api/v1/rooms/{id}` | Update room |
| DELETE | `/api/v1/rooms/{id}` | Delete room (409 if bookings or screenings exist) |
| POST | `/api/v1/screenings` | Create screening |
| DELETE | `/api/v1/screenings/{id}` | Delete screening (409 if tickets sold) |

### Example Request Bodies

**POST /api/v1/customers**
```json
{
  "username": "karl.lindqvist",
  "firstName": "Karl",
  "lastName": "Lindqvist",
  "email": "karl@example.com",
  "password": "password"
}
```

**POST /api/v1/bookings**
```json
{
  "roomId": 1,
  "date": "2025-09-01",
  "numberOfGuests": 20,
  "performance": "Birthday party",
  "technicalEquipment": "Projector"
}
```

**POST /api/v1/tickets**
```json
{
  "screeningId": 3,
  "numberOfTickets": 2
}
```

**PATCH /api/v1/bookings/{id}**
```json
{
  "date": "2025-10-15",
  "technicalEquipment": "Projector, Microphone"
}
```

## Technical Implementation

### Spring Security & Keycloak JWT

- `SecurityConfig` configures Spring Security as an OAuth2 resource server validating JWTs against Keycloak's JWK endpoint.
- A custom `KeycloakRealmRoleConverter` (inner class, not lambda-based) extracts roles from `realm_access.roles` in the JWT payload and maps them to Spring `GrantedAuthority` objects with the `ROLE_` prefix.
- Actuator endpoints are `permitAll()` so the Spring Boot Admin dashboard can reach them without authentication.

### Keycloak Account Management

`KeycloakService` communicates with Keycloak's Admin REST API using an admin service account:
- **`createUser()`**: Creates a Keycloak user, sets a temporary password, assigns the `USER` realm role, and returns the user's `sub` UUID.
- **`deleteUser()`**: Deletes the Keycloak user by `keycloakId`. A `null` check prevents crashes if the account was manually removed from Keycloak: the case is logged as a warning instead.

Keycloak usernames follow the pattern `cinema-c{n}`. The next number is determined by `CustomerRepository.findMaxKeycloakNumber()`, which returns the highest existing index. This is safe against gaps caused by previous deletions.

### Exception Handling

`GlobalExceptionHandler` maps exceptions to HTTP responses:

| Exception / Scenario | Status |
|----------------------|--------|
| Resource not found | 404 Not Found |
| Duplicate username | 409 Conflict |
| Delete blocked by dependencies | 409 Conflict |
| Capacity exceeded | 400 Bad Request |
| Validation failure (`@Valid`) | 400 Bad Request |
| Ownership mismatch | 403 Forbidden |

### Currency Conversion

`CurrencyService` wraps the `gruppe-shared-lib` converter. All booking and ticket responses include both `totalPriceSek` and `totalPriceUsd`.

---

**Author:** Daniel Eriksson  
**Course:** Java System Development (YH)  
**Assignment:** Microservices (Group Project)   
**Date:** April 2026  