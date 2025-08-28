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

### Migration Strategy – Strangler Fig

We used the Strangler Fig pattern:

1. Start with the monolith in production.
2. Introduce a gateway to route calls.
3. Gradually extract functionality (e.g. author, book, lending) into new services.
4. Redirect traffic for each feature from monolith → microservice.
5. Once fully migrated, the monolith becomes obsolete.

This evolutionary approach reduced risk, avoided a “big bang” rewrite, and allowed parallel operation of old and new components

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

---------------------------------------------------------------

## Access

1. Run Docker Engine
2. Run docker-compose.yml or if built, run the services
3. Surf to:
   - [Login/signup](http://localhost:8081/login)
   - [Eureka server](http://localhost:8761/)
4. Or use POSTMAN:
- **Auth** service: port 8081
  - Login: http://localhost:8081/login (POST)
  - signup: http://localhost:8081/signup (POST)


- **Author** service: port 8082
  - Get all authors: http://localhost:8082/api/authors (GET)
  - Create author: http://localhost:8082/api/authors (POST)


- **Book** service: port 8083
  - Create book: http://localhost:8083/api/books (POST)
  - Get all books: http://localhost:8083/api/books/all (GET)
  - Search by title: http://localhost:8083/api/books/search (GET)
  - Get top books: http://localhost:8083/api/books/top (GET)
  - Get top genres: http://localhost:8083/api/books/top-genres (GET)
  - Delete book by id: http://localhost:8083/api/books/{id} (DELETE)
  - Check if exists by id: http://localhost:8083/api/books/{id}/exist (GET) 

    
- **Lending** service: port 8084
    - Lend a book: http://localhost:8084/api/lendings/borrow (POST)
    - Return book by id: http://localhost:8084/api/lendings/{id}/return (POST)
    - Return book by title: http://localhost:8084/api/lendings/return/by-title/{readerEmail}/{bookTitle}/{recommended}/{comment} (POST)
    - Return book with positive recommendation id: http://localhost:8084/api/lendings/return/{id}/{recommended}/{comment} (POST)
    - Return book with negative recommendation id: http://localhost:8084/api/lendings/return/{id}/{recommended}/{comment} (POST)
    - Get all lendings: http://localhost:8084/api/lendings/all (GET)
    - Get all active lendings: http://localhost:8084/api/lendings/active (GET)
    - Get all active lendings from reader: http://localhost:8084/api/lendings/all/{readerEmail} (GET)
    - Get overdue books: http://localhost:8084/api/lendings/overdue (GET)
    - Get average lending duration: http://localhost:8084/api/lendings/average-duration (GET)
    - Get Recommendation summary: http://localhost:8084/api/lendings/books/1/recommendations/summary (GET)
  

- **Reader** service: port 8085
    - Book acquisition suggestion
        - Get all suggestions: http://localhost:8085/api/suggestions/all (GET)
        - Get all approved: http://localhost:8085/api/suggestions/approved (GET)
        - Top 5 not yet approved suggestions: http://localhost:8085/api/suggestions/top5Suggestions (GET)
        - Search by status: http://localhost:8085/api/suggestions/statusList/{status} (GET)
    - Normal reader calls:
      - Create Reader: http://localhost:8085/api/readers (POST)
      - Get by email: http://localhost:8085/api/readers/email?email=... (GET)
      - Get by phone: http://localhost:8085/api/readers/phone?phone=... (GET)
      - Get by email: http://localhost:8085/api/readers/email (GET)
      - Get preferred genres from reader: http://localhost:8087/api/readers/{email}/genres (GET)


- **Reporting** service: port 8086
  - Top genres: http://localhost:8086/api/reports/top-genres (GET)
  - Top readers by genre: http://localhost:8086/api/reports/top-readers/{genre} (GET)
  - Top 5 authors: http://localhost:8086/api/reports/top5 (GET)

