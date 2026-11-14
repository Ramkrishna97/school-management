# School Management System

Java 17, Spring Boot 3.5.6, Spring Cloud 2025.0.0, Gradle 8.10.2, and React/Vite. This repository implements the first delivery slice: service discovery, gateway, JWT accounts, teacher HR, timetable and substitutions, and inventory/procurement. Each data service owns its own in-process H2 database. Docker is not used.

## Start and stop on Windows

From this folder run `start.cmd`. It builds the Spring Boot JARs with the Gradle wrapper, installs frontend packages with `npm ci` if needed, starts seven processes, waits for authenticated gateway routes to respond, and opens `http://localhost:5173/`. Run `stop.cmd` to stop only processes whose recorded command lines match this project. Logs and PID records are in `.run/`.

Prerequisites: JDK 17, Node.js/npm, and network access for the first Gradle/npm dependency download. The scripts use `C:\Program Files\Java\jdk-17` by default; set `JAVA_HOME` if yours is elsewhere. Set `SCHOOL_JWT_SECRET` to a unique secret of at least 32 bytes and `SCHOOL_BOOTSTRAP_PASSWORD` before starting if you want to change the local defaults. The demo password is `ChangeMe123!`.

The root wrapper is `gradlew.bat` on Windows and `./gradlew` on macOS/Linux. To run a backend service manually, use `./gradlew :<module>:bootRun` in its own terminal. Start the discovery server first, followed by auth, teacher HR, timetable, inventory, and the gateway. In `frontend/`, run `npm ci` and `npm run dev`.

| Module | Port | H2 JDBC URL | H2 console |
| --- | ---: | --- | --- |
| discovery-server | 8761 | none | none |
| api-gateway | 8080 | none | none |
| auth-user-service | 8084 | `jdbc:h2:mem:authdb` | `http://localhost:8084/h2-console` |
| teacher-hr-service | 8081 | `jdbc:h2:mem:teacherdb` | `http://localhost:8081/h2-console` |
| timetable-substitution-service | 8082 | `jdbc:h2:mem:timetabledb` | `http://localhost:8082/h2-console` |
| inventory-asset-service | 8083 | `jdbc:h2:mem:inventorydb` | `http://localhost:8083/h2-console` |
| frontend | 5173 | none | none |

Each database exists only in its service process and resets when that service stops. H2 consoles are local development tools; connect with username `sa`, blank password, and the matching JDBC URL above. Cross-service data access uses Eureka-discovered Feign REST calls and a service JWT. The Feign clients have Resilience4j circuit breakers and fail-closed fallbacks. A failed leave-triggered allocation is recorded in the response and can be retried with the manual allocation endpoint or the scheduled scan.

## Seeded demo

Sign in at `http://localhost:5173/` as `principal`, `admin`, `teacher`, or `storemanager` with the bootstrap password. All eight requested account roles have a seeded login in the auth database; domain screens are currently implemented for principal/admin, teachers, and store managers. The `teacher` demo account belongs to teacher 2, who receives the sample substitution notice. The teacher database seeds six teachers and two leave requests. The timetable database seeds eight Monday periods for class 7-A. The approved leave for teacher 1 on `2030-01-07` exercises substitution allocation. The inventory database seeds eight assets, four of which need restock; startup monitoring creates their requisitions.

The gateway serves backend APIs under `http://localhost:8080/api`. Example requests:

```powershell
$login = Invoke-RestMethod -Method Post http://localhost:8080/api/auth/login -ContentType application/json -Body '{"username":"principal","password":"ChangeMe123!"}'
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
Invoke-RestMethod -Method Post http://localhost:8080/api/substitutions/allocate-absences/2030-01-07 -Headers $headers
Invoke-RestMethod 'http://localhost:8080/api/timetables/daily?date=2030-01-07&className=7&section=A' -Headers $headers
Invoke-RestMethod http://localhost:8080/api/inventory/requisitions -Headers $headers
```

Teacher leave is requested with `POST /api/teachers/{id}/leaves` using `startDate`, `endDate`, `type` (`CASUAL`, `SICK`, `EARNED`), and `reason`. A principal decides it with `PATCH /api/teachers/leaves/{leaveId}/decision` and `{"status":"APPROVED"}`. Approval makes the teacher unavailable and triggers allocation for each leave date. Teachers can see their assigned notices at `GET /api/substitutions/notices/mine`. Timetable creation uses `POST /api/timetables`; daily views include all eight periods and show `FREE`, `REGULAR`, `SUBSTITUTED`, or `UNFILLED`.

The timetable policy defaults to eight periods, at most five teaching periods per teacher per day, and at most three consecutive periods. Allocation considers subject qualification, approved leave, period availability, workload, and consecutive-period limits; it chooses the lowest workload, then lowest teacher ID. Database constraints protect against same-period duplicate assignments. Repeat scans reuse valid assignments and cancel old notices when an assigned substitute later becomes absent.

Inventory stock checks run at startup and every five minutes. Stock below the configured item minimum is flagged and produces one active requisition per item. Store managers can place vendor orders and receive them through the API or dashboard. All MVC service errors use the same `timestamp`, `status`, `errorCode`, `message`, and `path` shape. Gateway validates JWTs, enforces roles, permits the Vite origin, and applies per-IP in-memory rate limits.

## Verify

```powershell
.\gradlew.bat test bootJar
cd frontend
npm ci
npm run build
```

The remaining requested student, academic/report card, fee/payroll, library/transport, and notification domains are future modules; their behavior and seed records are not represented here. This foundation is locally runnable, but production deployment needs persistent databases and migrations, secret management, a distributed lock for multi-instance substitution allocation, durable notification delivery, and a distributed rate limiter.
