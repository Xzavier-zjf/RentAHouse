# RentAHouse Deployment Checklist

## 1. Required Environment Variables

### Shared security
- `SPRING_PROFILES_ACTIVE=prod`
- `RENTAL_JWT_SECRET` (strong random secret, at least 32 chars)
- `RENTAL_JWT_EXPIRE_SECONDS` (for example `7200`)

### Data services
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `MONGODB_URI` (for `rental-user` and `rental-house`)

### Messaging
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`

### Gateway routing
- `RENTAL_USER_URL`
- `RENTAL_HOUSE_URL`
- `RENTAL_ORDER_URL`
- `RENTAL_COMMENT_URL`
- `RENTAL_MESSAGE_URL`
- `RENTAL_CORS_ALLOWED_ORIGINS` (comma-separated exact origins)

## 2. Service Startup Order

1. MySQL / Redis / MongoDB / RabbitMQ
2. `rental-user`
3. `rental-house`
4. `rental-comment`
5. `rental-message`
6. `rental-order`
7. `rental-gateway`

## 3. Build and Verify

1. Run tests:
   - `.\mvnw.cmd test`
2. Build artifacts:
   - `.\mvnw.cmd clean package -DskipTests`
3. Start each service with `prod` profile and verify health/log output.

## 4. Security Validation Before Go-Live

- Confirm no default weak secrets are used.
- Confirm CORS only allows trusted frontend origins.
- Confirm admin APIs require admin role end to end.
- Confirm file upload rejects non-image and oversized files.
- Confirm logs do not print token values or passwords.

## 5. Smoke Test Scenarios

- User register/login and profile update
- Owner publish house and update status
- User browse, favorite, and create order
- User pay order and owner updates order status
- Admin user/house/comment/order/message management

