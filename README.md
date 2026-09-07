# KAWA — MVP Architecture v1

This repository is a cloud-ready MVP skeleton for KAWA.

## Fixed architecture decisions

- **Responsive Web MVP**: React/TypeScript; no React Native project in the MVP.
- **Stable QR**: one user = one stable public KAWA ID = one stable QR code for all retailers and transactions.
- **Private internal ID**: the internal UUID is never exposed to retailers/POS.
- **No loyalty-points engine in MVP**: retailers remain responsible for points, rewards and loyalty rules.
- **Microservices from day one**: `customer-service`, `wallet-service`, `retailer-service`, `partner-service`.
- **Database ownership per service**: no direct cross-service table access.
- **REST first, events ready**: synchronous critical path for MVP; Outbox/event contracts prepared for scaling.

Read `ARCHITECTURE.md`, `docs/IMPLEMENTATION_ROADMAP.md` and `TREE.txt` first.
