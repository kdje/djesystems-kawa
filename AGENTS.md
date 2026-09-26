# KAWA — OpenCode project instructions

## Project purpose

KAWA is a retail loyalty / customer-retailer linking platform.

The repository contains multiple Spring Boot services and Android/Web applications.

## Repository map

```text
KAWA/
├── services/
│   ├── customer-service/
│   ├── wallet-service/
│   ├── retailer-service/
│   └── partner-service/
├── apps/
│   ├── kawa-platform/
│   └── kawa-retailer-simulator/
├── .opencode/
│   ├── agents/
│   └── commands/
└── AGENTS.md
```

## Main technology stack

- Java 21
- Spring Boot 3.4.x
- Maven
- Flyway
- Azure Service Bus
- Firebase Authentication / Notifications
- React + Vite
- Capacitor Android
- Native Android / Gradle for `kawa-retailer-simulator`

## KAWA architecture rules

### Backend

- Keep REST/API code separate from application/domain logic.
- Do not bypass existing security filters or JWT validation.
- Do not weaken Spring Security to make a test pass.
- Preserve stateless authentication.
- Database schema changes must use Flyway migrations.
- Do not modify an already-applied Flyway migration. Add a new migration instead.
- Event publication must remain compatible with Azure Service Bus.
- Preserve the outbox pattern where already used.
- Never silently swallow messaging errors.
- Avoid introducing synchronous coupling between services when an event already exists for the use case.

### customer-service

Known responsibilities include:
- customer identity
- retailer relationships
- pending / active relationships
- retailer-link event handling
- Firebase related customer notification behaviour

### wallet-service

Known responsibilities include:
- wallet / KawaId resolution
- retailer workflow
- Azure Service Bus event publication
- outbox events

### retailer-service

Known responsibilities include:
- retailer information
- retailer credentials / configuration where applicable
- protected APIs

### kawa-platform

- React / Vite frontend.
- Firebase authentication.
- Android builds use the Android-specific Vite mode when required.
- Do not hardcode API URLs or Firebase secrets.
- Preserve environment-variable based configuration.

Useful commands:

```powershell
cd apps/kawa-platform
npm install
npm run build
npm run build:android
```

Use `npm run build:android` when the output is intended for Capacitor Android.

### kawa-retailer-simulator

Native Android simulator used to emulate a retailer / checkout flow.

Useful command:

```powershell
cd apps/kawa-retailer-simulator
.\gradlew.bat assembleDebug
```

Never commit:
- `local.properties`
- secrets
- tenant IDs if they are confidential
- client secrets
- private keys
- Firebase service-account credentials

## Git rules

The default protected branch is `main`.

Agents MUST NOT:
- commit directly on `main`
- push directly to `main`
- use `git push --force`
- use `git push -f`
- use `git reset --hard`
- use `git clean -fd`
- merge a pull request automatically
- delete remote branches unless explicitly requested by the human

Allowed working branch prefixes:

```text
feature/
fix/
refactor/
chore/
docs/
test/
```

Before a commit:
1. inspect `git status`
2. inspect the diff
3. ensure no secret is being added
4. run relevant build/tests
5. use a meaningful Conventional Commit message

Examples:

```text
feat(wallet): add automatic retailer consent
fix(customer): refresh retailer relationships after link event
refactor(retailer): isolate credential resolution
test(wallet): cover direct retailer link workflow
```

## Pull request rules

Every code change should normally go through a pull request.

A PR must contain:
- objective
- technical changes
- impacted modules/services
- tests executed
- risks / points of attention
- migration/configuration changes if applicable

The agent may create a PR but MUST NOT merge it.

## Verification rules

Run the smallest relevant verification first, then broaden when necessary.

Examples:

```powershell
mvn -pl services/customer-service test
mvn -pl services/wallet-service test
mvn -pl services/retailer-service test
```

If Maven module selection does not match the actual parent POM, detect the real module path and use the repository's actual Maven structure rather than inventing commands.

For web:

```powershell
cd apps/kawa-platform
npm run build
```

For Android Capacitor packaging:

```powershell
cd apps/kawa-platform
npm run build:android
```

For the retailer simulator:

```powershell
cd apps/kawa-retailer-simulator
.\gradlew.bat assembleDebug
```

## Agent workflow

Preferred workflow:

```text
kawa-code
   ↓
kawa-reviewer
   ↓
kawa-code (only if reviewer finds blocking issues)
   ↓
kawa-git
   ↓
kawa-pr
   ↓
HUMAN REVIEW
   ↓
MERGE
```

A human owns the final merge decision.
