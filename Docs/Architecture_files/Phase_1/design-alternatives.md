# Attribute-Driven Design (ADD) – Design Alternatives

This document consolidates the design justifications and alternatives across all concerns of the LMS Monolith (Phase 1).  
It follows the Attribute-Driven Design (ADD) process, focusing on **quality attributes** (performance, availability, modifiability, security, usability) and the functional drivers.

------------------------------------------------------

## 1. Authentication and IAM (OAuth Login Flow)

### Key Decisions
- Adopt **OAuth2** with external providers (GitHub/Facebook) to avoid managing local credentials.
- Use a **custom AuthenticationSuccessHandler** to return a **JWT in JSON** instead of default redirect.
- Provision/load user on first login; issue stateless tokens.

### Alternatives Considered
- Local username/password with sessions.
- Opaque tokens with server-side storage.
- Redirect-only success handler.

### Trade-offs
- JWTs scale better and decouple client/server, but require expiry/rotation management.
- Dependency on third-party IAM availability.

### Risks & Mitigations
- **Risk:** Provider outage → allow multiple providers, fallback auth.
- **Risk:** Token misuse → short TTL, signature validation, HTTPS.

### Verification
- End-to-end tests with OAuth2 providers.
- Unit tests for handler branches (missing attributes, existing/new user).

------------------------------------------------------

## 2. Persistence (MySQL)

### Key Decisions
- Relational model (MySQL/H2) ensures strong consistency for Lending–Book–Reader relations.
- Layered architecture: **Controller → Service → Repository → DB**.
- Transactional integrity for CRUD operations.

### Alternatives Considered
- MongoDB/document store (flexible schema).
- In-memory only (H2).
- Fat controllers with inline queries.

### Trade-offs
- Stronger guarantees vs. reduced flexibility.
- More boilerplate but cleaner separation of concerns.

### Risks & Mitigations
- **Risk:** Lock contention → indexing, smaller transactions.
- **Risk:** Migration issues → versioned migrations (Flyway/Liquibase).

### Verification
- Integration tests with MySQL/Testcontainers.
- Schema validation on boot.

------------------------------------------------------

## 3. ID Generation (Lendings and Authors)

### Key Decisions
- Support **three ID formats**:
    - 24-hexadecimal (UUID-like),
    - 20-char alphanumeric hash (business ID),
    - Integer incremental (independent of DB auto-ID).
- Dedicated `IdGenerator` components invoked at entity creation.

### Alternatives Considered
- Single auto-increment ID.
- Client-side ID generation.
- External microservice for ID generation.

### Trade-offs
- Flexibility and compatibility vs. higher complexity.
- Multiple formats increase clarity for integrations but add overhead.

### Risks & Mitigations
- **Risk:** Hash collisions → use UUID/SHA.
- **Risk:** Confusion between ID types → documentation + API clarity.

### Verification
- Unit tests for uniqueness and format validation.

------------------------------------------------------

## 4. Recommendation Engine (Lending Recommendations)

### Key Decisions
- Simple, rule-based engine:
    - `<10y`: children books,
    - `10–17y`: juvenile books,
    - `18+`: top genre of user.
- Compute Top-X within Top-Y genres based on lending history.

### Alternatives Considered
- Global “most popular” only.
- ML-based collaborative filtering.
- Precomputed recommendations.

### Trade-offs
- Transparent, testable rules vs. less adaptive.
- Real-time queries ensure freshness but add latency.

### Risks & Mitigations
- **Risk:** Sparse data for new readers → fallback to global popular.
- **Risk:** Wrong age thresholds → configurable limits.

### Verification
- Unit tests per age branch.
- Integration tests with genre statistics.

------------------------------------------------------

## 5. REST–Database Mapping (Database)

### Key Decisions
- RESTful endpoints map directly to relational schema:
    - `/readers/{id}/lendings`,
    - `/authors/{id}/books`,
    - `/books/{isbn}`.
- Standard HTTP verbs (GET, POST, PATCH, DELETE).

### Alternatives Considered
- GraphQL API.
- Single query endpoint with parameters.
- Document DB schema.

### Trade-offs
- REST+RDBMS = clear contracts and integrity vs. less query flexibility.
- Verbose URIs vs. explicit semantics.

### Risks & Mitigations
- **Risk:** Overfetching large result sets → pagination.
- **Risk:** API/schema drift → DTOs decouple API from persistence.

### Verification
- REST-assured tests for endpoints.
- Integration tests with MySQL/H2.

------------------------------------------------------

## 6. Cross-Cutting Quality Attributes

- **Performance:**
    - JWT avoids DB lookups for auth.
    - Indexed queries for lending/recommendations.
- **Availability:**
    - External IAM redundancy.
    - Transaction boundaries minimize partial failures.
- **Modifiability:**
    - Layered services, DTOs, mappers decouple presentation/persistence.
- **Security:**
    - OAuth2, JWT validation, DTO filtering.
- **Releasability:**
    - Migration scripts, isolated service tests.

------------------------------------------------------

## 7. Consolidated Alternatives Overview

| Concern         | Main Decision | Alternatives                    | Trade-offs                    |
|-----------------|---------------|---------------------------------|-------------------------------|
| IAM/Auth        | OAuth2 (JWT)  | Local login, opaque tokens      | Scalability vs. complexity    |
| Persistence     | MySQL (RDBMS) | MongoDB, in-memory              | Integrity vs. flexibility     |
| ID Generation   | Multi-format  | Single auto-ID, client-side gen | Compatibility vs. complexity  |
| Recommendations | Rule-based    | Global popular, ML              | Transparency vs. adaptability |
| REST Mapping    | REST+RDBMS    | GraphQL, parameter endpoint     | Clarity vs. query power       |

------------------------------------------------------

## 8. Conclusion

The ADD process drove architectural decisions by mapping **functional requirements** to **quality attribute drivers**.  
At each decision point, alternatives were explored, trade-offs evaluated, and risks mitigated.  
The result is a **monolith architecture** that is secure, consistent, and maintainable, while laying the foundation for future evolution into microservices.

