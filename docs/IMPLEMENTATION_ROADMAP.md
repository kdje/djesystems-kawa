# MVP implementation roadmap

## Sprint 0 — Foundation
Repository, CI, MySQL, OIDC choice, observability baseline, OpenAPI review.

## Sprint 1 — Customer identity
Authenticated customer bootstrap, stable public ID, `/customers/me`, permanent QR display.

## Sprint 2 — Retailer catalog + wallet
Retailers, add/list/unlink loyalty accounts, validation rules.

## Sprint 3 — Partner/POS API
OAuth2 Client Credentials, partner authorization, `/partner/v1/loyalty/resolve`, audit, first performance tests.

## Sprint 4 — Production hardening
Rate limiting, security review, secrets, dashboards, backups, deployment pipeline.

## Later
Outbox relay/event bus, partner read model/cache, notifications, analytics, physical KAWA card, optional native/PWA client.
