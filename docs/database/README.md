# Database choice and administration

## MVP choice: MySQL

KAWA uses MySQL for the MVP.

MySQL would also be a strong technical choice, but KAWA's current data model does not require MySQL-specific features.

Operational familiarity is valuable during the MVP phase, so MySQL is preferred.

## Local visual administration

Docker Compose includes phpMyAdmin:

```text
http://localhost:8089
```

Use it only in local/development environments.

Other good visual clients:
- DBeaver Community (works with MySQL and MySQL)
- JetBrains DataGrip
- MySQL Workbench
- Adminer

## If MySQL is chosen later

A graphical interface exists:
- pgAdmin 4
- DBeaver
- DataGrip

The application architecture does not depend strongly on the database vendor because schema changes are managed with Flyway and persistence through JPA.
