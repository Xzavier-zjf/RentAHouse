# RentAHouse

RentAHouse is a multi-module rental platform built with Spring Boot and a Vue frontend.
It includes user/account management, house listing management, orders, comments, messaging, and a gateway.

## Tech Stack

- Java 17
- Spring Boot 3.2.x
- Spring Cloud Gateway
- Spring Security + JWT
- MyBatis-Plus
- MySQL
- Redis
- MongoDB (file storage via GridFS)
- RabbitMQ (order timeout flow)
- Vue 3 + Vite (frontend)

## Project Structure

- `rental-gateway`: API gateway and global auth filter
- `rental-common`: shared utilities, exceptions, auth helpers
- `rental-user`: users, login, profile, admin user management
- `rental-house`: house listing and favorite features
- `rental-order`: order lifecycle and payment simulation
- `rental-comment`: house comments
- `rental-message`: chat/system messages
- `frontend`: Vue-based UI
- `database`: SQL scripts and DB assets

## Prerequisites

- JDK 17
- Maven Wrapper (`mvnw.cmd` provided)
- MySQL
- Redis
- MongoDB
- RabbitMQ
- Node.js 18+ (for frontend)

## Quick Start (Dev)

1. Start infrastructure: MySQL, Redis, MongoDB, RabbitMQ.
2. Set profile:
   - PowerShell: `$env:SPRING_PROFILES_ACTIVE='dev'`
3. Run backend services (recommended order):
   1. `rental-user`
   2. `rental-house`
   3. `rental-comment`
   4. `rental-message`
   5. `rental-order`
   6. `rental-gateway`
4. Start frontend:
   - `cd frontend`
   - `npm install`
   - `npm run dev`

## Build and Test

- Run all tests:
  - `.\mvnw.cmd test`
- Build all modules:
  - `.\mvnw.cmd clean package -DskipTests`

## Profiles and Config

- Base config in each module: `src/main/resources/application.yml`
- Dev overrides: `application-dev.yml`
- Prod overrides: `application-prod.yml`

For production-required env vars and go-live checks, see:

- `DEPLOYMENT_CHECKLIST.md`

## Security Notes

- JWT secret must be provided by environment variables in production.
- CORS origins should be explicitly configured in gateway prod settings.
- Do not expose internal services directly to the public network.

## License

For personal/educational use unless otherwise specified by repository owner.

