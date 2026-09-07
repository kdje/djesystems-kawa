# ADR 0007 — MySQL for KAWA MVP

## Decision
Use MySQL for the MVP.

## Rationale
MySQL and MySQL are both technically suitable for KAWA.
The MVP data model is relational and does not require MySQL-specific capabilities.

Using MySQL provides an immediate operational advantage because the team is already comfortable with it and can use phpMyAdmin for inspection and administration.

## Rules
- use a supported MySQL 8.x release
- one logical database per microservice
- no direct cross-service SQL access
- use Flyway for schema evolution
- never use automatic destructive schema synchronization in production

## Administration
phpMyAdmin is provided for local development only.
It must not be exposed publicly in production.
