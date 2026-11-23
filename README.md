# GymFlex

GymFlex is a Spring Boot REST API with a React and Tailwind web application for a fitness club.

## Requirements

- Java 17 or newer
- PostgreSQL 15 or newer
- Node.js 20 or newer and npm (only needed to develop/build the React frontend)

## Database and backend

Create a PostgreSQL database named `gymflex_db`, then configure the connection with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The default URL points to `localhost:5432/gymflex_db`, and the default username is `postgres`; set `DB_PASSWORD` for your local database. The application creates/updates its JPA tables, then applies the idempotent indexes, constraints, occupancy view, `cancel_class` procedure, append-only audit trigger, and deferred membership/payment consistency triggers from `src/main/resources/schema.sql`.

Set `JWT_SECRET` to a private random value of at least 32 bytes before starting the application. The API listens on port `8081`.

To create the first administrator, set `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` in the IntelliJ run configuration (or process environment) before the first startup. The account is created with a BCrypt password. Clear those variables after setup; later startups leave the existing administrator unchanged. Legacy plaintext passwords in the existing database are upgraded to BCrypt during startup.

Run the backend with `mvn spring-boot:run` or `./mvnw spring-boot:run`. OpenAPI is available at `http://localhost:8081/swagger-ui.html`.

To send cancellation email notifications, configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, and `MAIL_SMTP_STARTTLS` for a reachable SMTP server. The local defaults target a development SMTP server on `localhost:1025`; notifications are not delivered until an SMTP server is running there or these variables point to one.

The backend serves the built React frontend from the same origin. Build it with `npm install` and `npm run build` in `frontend`, then copy `frontend/dist/index.html` and `frontend/dist/assets/*` into `src/main/resources/static/`. For normal use, run the backend and open only `http://localhost:8081`.

Run backend tests with `mvn test`. The stated latency and throughput goals are targets and must be measured against the intended database and deployment hardware; unit/build tests do not prove those numbers.

## Web application

```sh
cd frontend
npm install
npm run dev
```

The Vite development server listens on `http://localhost:5173`. It calls the API on the same origin by default; set `VITE_API_URL` only if the API runs elsewhere.

## Main API routes

- Public: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/classes`, `GET /api/membership-types`
- Clients: `POST /api/reservations`, `GET /api/reservations/mine`, `DELETE /api/reservations/{id}`, `POST /api/memberships/purchase`, `GET /api/memberships/mine`, `GET /api/payments/mine`
- Trainers: `GET /api/classes/mine`, `POST /api/classes`, `GET /api/classes/{id}/participants`, `PUT /api/classes/{id}/reservations/{reservationId}/attendance`
- Administrators: `POST /api/users`, `GET /api/users/admin`, `DELETE /api/users/{id}` (soft deactivation), membership-type management, class management, `GET /api/classes/occupancy`, payment status simulation, audit log, and active-client report

Authenticated requests use `Authorization: Bearer <JWT>`. Successful authenticated responses include a refreshed token in `X-Auth-Token`; clients should save it to keep the 60-minute idle timeout sliding with activity.
