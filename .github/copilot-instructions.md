# KAWA — GitHub Copilot Instructions

## General behaviour

Before modifying code:

1. Read `AGENTS.md` at the root of the repository.
2. Read `ARCHITECTURE.md` when the task affects architecture, service boundaries, events, persistence or integration.
3. Inspect the existing implementation before proposing changes.
4. Identify the root cause before applying a fix.
5. Prefer the smallest coherent change that solves the problem.
6. Do not invent files, classes, endpoints, events or configuration that do not exist in the repository.

## Architecture

Respect the existing KAWA architecture defined in `AGENTS.md`.

In particular:

- preserve service boundaries
- preserve asynchronous integration where already used
- preserve Azure Service Bus event contracts
- preserve the outbox pattern where already implemented
- preserve Spring Security and JWT validation
- use Flyway for database schema changes
- do not rewrite already-applied Flyway migrations
- avoid introducing unnecessary synchronous coupling between services

## Multi-service bugs

When a bug spans multiple services, trace the complete flow before changing code.

For example:

```text
source service
-> persistence
-> event publication
-> Azure Service Bus
-> subscription
-> consumer
-> handler
-> DTO / event contract
-> mapper
-> projection
-> destination persistence
