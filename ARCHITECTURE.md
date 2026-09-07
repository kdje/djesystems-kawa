# KAWA — Detailed Architecture

## 1. Business model

KAWA gives a consumer one public loyalty identity that can be presented at different retailers.

```text
KAWA public ID
    ├── CARREFOUR -> 123456789
    ├── AUCHAN    -> 998877665
    └── LECLERC   -> 445566778
```

The retailer remains responsible for points, rewards, promotions and loyalty rules.

## 2. Stable public ID and QR

Each customer has:

```text
internal_id = private technical UUID
public_id   = public, random, stable KAWA identifier
```

Example QR payload:

```text
KAWA:1:d73f8cb29aed4cc4941885b31e8d1327
```

The same user presents the same QR at every retailer and transaction. The public ID is an **identifier, not an authentication secret**. It must not grant access to the account or personal data. Exceptional revocation/reissue is possible if compromised, but normal transactions never generate a new QR.

There is deliberately **no QR microservice**: the Web client renders the QR locally from the stable public ID.

## 3. Microservices

### customer-service — port 8081
Owns customer technical identity, private UUID, stable public KAWA ID, status and identity-provider subject.

Public APIs:
- `GET /api/v1/customers/me`
- `GET /api/v1/customers/me/kawa-id`

Internal API:
- `GET /internal/v1/customers/by-public-id/{publicId}`

### wallet-service — port 8082
Owns links between a KAWA customer and retailer loyalty identifiers.

Public APIs:
- `GET /api/v1/wallet/loyalty-accounts`
- `POST /api/v1/wallet/loyalty-accounts`
- `DELETE /api/v1/wallet/loyalty-accounts/{id}`

Internal API:
- `GET /internal/v1/loyalty-accounts/resolve`

`loyalty_identifier` is always a string.

### retailer-service — port 8083
Owns retailer master data, status and future integration metadata.

Public APIs:
- `GET /api/v1/retailers`
- `GET /api/v1/retailers/{code}`

### partner-service — port 8084
B2B/POS boundary. Authenticates machine clients, determines retailer context, resolves a public KAWA ID into the retailer loyalty identifier, applies authorization and records audit metadata.

Public B2B API:
- `POST /partner/v1/loyalty/resolve`

For a partner bound to one retailer, retailer context can be inferred from the OAuth client. For a multi-retailer POS vendor, `retailerCode` is supplied and checked against authorization.

## 4. MVP POS sequence

```text
User -> presents permanent QR -> POS
POS  -> OAuth2 + publicId -> partner-service
partner-service -> customer-service -> private customer UUID
partner-service -> wallet-service -> loyalty identifier for retailer
partner-service -> POS -> loyalty identifier
```

At higher scale, `partner-service` can maintain an event-fed read model:

```text
(public_id, retailer_code) -> loyalty_identifier
```

This removes the two synchronous internal calls without changing the public API or service boundaries.

## 5. Web application

Responsive React/TypeScript application only for MVP.

Primary areas:
- Login
- Home / My KAWA: stable QR + public KAWA ID
- Wallet: linked retailer loyalty accounts
- Retailers
- Profile

## 6. Data ownership

A service never reads another service's tables directly.

For MVP/local cost control, one MySQL server hosts four isolated databases:

```text
kawa_customer
kawa_wallet
kawa_retailer
kawa_partner
```

They can later move to separate managed instances without changing service contracts.

## 7. Security

Consumer APIs: OIDC end-user authentication; Spring OAuth2 Resource Server/JWT validation. Code remains provider-neutral (Cognito/Auth0/Keycloak/etc.).

Partner APIs: OAuth2 Client Credentials. The public KAWA ID is never an authentication credential.

Internal APIs under `/internal/**` must not be internet-exposed and should use private networking plus service identity/OAuth2; mTLS can be added later if required.

## 8. Events prepared from MVP

Contracts are defined for:
- `customer.public-id.created.v1`
- `loyalty.account.linked.v1`
- `loyalty.account.updated.v1`
- `loyalty.account.unlinked.v1`
- `retailer.activated.v1`
- `retailer.deactivated.v1`

Recommended publication mechanism: transactional Outbox in the owning service, relayed later to EventBridge/SQS, SNS/SQS, Kafka or another broker.

## 9. Scaling

```text
customer-service  x2
wallet-service    x3
retailer-service  x2
partner-service   x20
```

POS traffic can therefore scale independently from consumer Web traffic.

## 10. Cloud trajectory

Provider-neutral Java services packaged as Docker images. An AWS target can use CloudFront/S3, API Gateway/ALB, ECS Fargate, RDS MySQL, Cognito/OIDC, Secrets Manager and CloudWatch/OpenTelemetry. EventBridge/SQS or SNS/SQS can be added later. Kubernetes/EKS is optional, not required for the first cloud deployment.
