# Phase 2
## Implemented Requirements
### Microservices
- Author service
- Authentication (auth) service
- Book service
- Gateway service
- Lending service
- Reader service
- Reporting service

### Non-functional Requirements

### Functional Requirements
- As a librarian, I want to create a Book, Author and Genre in the same process.
- As a reader, I want to suggest the acquisition of a new book
- As a reader, upon returning a book, I went to recommend it (positively or negatively).

## Kanban board
| Not addressed               | Work-in-progress | Addressed                 | Tested | Completed |
|-----------------------------|------------------|---------------------------|--------|-----------|
|                             |                  | Implement Eureka server   |        |           |
| Implement Author service    |                  |                           |        |           |
|                             |                  | Implement Auth service    |        |           |
| Implement Book service      |                  |                           |        |           |
|                             |                  | Implement Gateway service |        |           |
| Implement Lending service   |                  |                           |        |           |
| Implement Reader service    |                  |                           |        |           |
| Implement Reporting service |                  |                           |        |           |

## Attribute-Driven Design (ADD)

# Attribute-Driven Design (ADD) – Phase 2: Microservice Reengineering

## 1. Context

In the previous phase, the LMS (Library Management System) was implemented using a monolithic or modular monolithic architecture. Although functional, this architecture proved limiting in terms of:

- Performance under load
- Availability
- Scalability
- Elasticity

### Goal
Reengineer the LMS application using a **microservices-based distributed architecture**, aligned with SOA principles and API-led connectivity.

---

## 2. Functional Requirements

| ID  | Requirement                                                                      |
|-----|----------------------------------------------------------------------------------|
| FR1 | As a librarian, I want to create a Book, Author, and Genre in the same process   |
| FR2 | As a reader, I want to suggest the acquisition of a new Book                     |
| FR3 | As a reader, I want to recommend a Book (positive or negative) when returning it |
| FR4 | (Phase 1) Generate Lending and Author IDs in various formats                     |
| FR5 | (Phase 1) Recommend Lendings based on reader’s age or genre                      |
| FR6 | (Phase 2) View top 5 most lent books                                             |

---

## 3. Architectural Decisions

### Architecture Style
- **Microservices** – Each bounded context is implemented as a separate Spring Boot service.

### Service Discovery & Routing
- **Eureka Server** for dynamic service discovery
- **Spring Cloud Gateway** as API Gateway

### Database Strategy
- Each service manages its own database (PostgreSQL, H2, etc.)

### Authentication (IAM)
- Dedicated **auth-service** using OAuth2 login via **Google** and **Facebook**

### ID Generation
- Lending ID: 24-character UUID
- Author ID: SHA-256 business hash
- Book ID: Independent from DB (auto-generated)

---

## 4. Quality Attributes & Design Drivers

| Attribute         | Realization Strategy                                           |
|-------------------|----------------------------------------------------------------|
| **Availability**  | Services are isolated; failure of one doesn't crash the system |
| **Performance**   | Top 5 reporting and lending recommendations are isolated       |
| **Scalability**   | Horizontal scaling per service                                 |
| **Modifiability** | Services can evolve independently                              |
| **Security**      | IAM via OAuth2, JWT-secured API communication                  |
| **Releasability** | CI/CD enables fast, isolated deployments                       |
| **Portability**   | Dockerized services managed via Docker Compose                 |

---

## 5. Component Structure

- `book-service`: manages books and full creation flow
- `author-service`: author creation, ID generation
- `lending-service`: book lending, recommendations
- `reader-service`: book suggestions, reader metadata
- `reporting-service`: generates top-5 lending statistics
- `auth-service`: OAuth2-based authentication
- `gateway-service`: routing and API access control
- `eureka-server`: central service registry

---

## 6. Example: Service Interaction for FR1

**“Create Book, Author, and Genre in one flow”**

