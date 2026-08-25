# TechMate

A full-stack attendance and analytics platform built for teachers to manage subjects, track daily student attendance, and view attendance trends — with Google OAuth 2.0 login and bulk CSV/Excel import.

Built with Java, Spring Boot, Spring Data JPA/Hibernate, MySQL, Thymeleaf and Spring Security.

## Why I built this

Teachers manually tracking attendance in registers or spreadsheets have no easy way to see patterns — which students are chronically absent, which subjects have low attendance, etc. TechMate gives each teacher their own dashboard: create subjects, take daily attendance, and see analytics per subject, without touching a spreadsheet.

## Features

- **Google OAuth 2.0 login** — teachers sign in with their Google account; no separate password/registration flow to maintain
- **Subject & student management** — create subjects, add students manually or via bulk CSV/Excel import, export student lists
- **Daily attendance tracking** — mark each student Present / Absent / Authorized Leave / Unauthorized Leave for any date, with per-subject attendance history
- **Bulk attendance import** — upload a CSV or Excel file to import attendance records instead of marking manually
- **Analytics dashboard** — per-subject attendance analytics for identifying trends
- **Ownership-scoped access** — every subject/attendance action checks that the logged-in teacher owns the subject before allowing reads or writes

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Auth | Spring Security + OAuth 2.0 Client (Google) |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL |
| Templating | Thymeleaf |
| File import | Apache POI (Excel), OpenCSV (CSV) |
| Build | Maven |

## Architecture

Standard layered Spring Boot MVC structure:

```
controller/   → HomeController, SubjectController, StudentController,
                AttendanceController, AnalyticsController
service/      → business logic per domain (Teacher, Subject, Student,
                Attendance, Analytics)
repository/   → Spring Data JPA repositories
entity/       → Teacher, Subject, Student, Attendance (JPA entities)
resources/    → Thymeleaf templates + static CSS/JS per page
```

**Data model:** a `Teacher` owns many `Subject`s; each `Subject` has many `Student`s; `Attendance` is a join entity linking a `Student` + `Subject` + `date` to a status, with a unique constraint preventing duplicate attendance records for the same student/subject/day.

**Access control:** authentication is handled entirely via Google OAuth 2.0 (no local password storage). Every subject-scoped endpoint verifies the authenticated user's email matches the subject's owning teacher before returning or mutating data.

## Getting started

### Prerequisites

- Java 17+
- Maven (or use the included `./mvnw` wrapper)
- MySQL running locally
- A Google OAuth 2.0 Client ID/Secret ([console.cloud.google.com](https://console.cloud.google.com))

### Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/priiiiitam/techMate.git
   cd techMate
   ```

2. Create a MySQL database:
   ```sql
   CREATE DATABASE techmate;
   ```

3. Create `src/main/resources/application.properties` (not committed — contains secrets) with:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/techmate
   spring.datasource.username=your_mysql_username
   spring.datasource.password=your_mysql_password
   spring.jpa.hibernate.ddl-auto=update

   spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
   spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
   ```

4. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```

5. Visit `http://localhost:8080` and sign in with Google.

## Roadmap

- [ ] Deploy a live demo
- [ ] Automated tests for controller/service layers
- [ ] Attendance percentage alerts for students below a threshold

## Author

**Pritam Jadhav** — [Portfolio](https://portfolio.pritamjadhav5462.workers.dev/) · [LinkedIn](https://www.linkedin.com/in/pritam-jadhav-09251b289) · [GitHub](https://github.com/priiiiitam)
