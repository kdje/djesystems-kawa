# partner-service

Port: `8084`

Package layers: `api`, `application`, `domain`, `infrastructure`, `config`.

No shared JPA entities or direct cross-service database access.


## Authentication

This service does **not** use Firebase consumer authentication.

`/partner/**` is a B2B machine-to-machine boundary and validates OAuth2/JWT access tokens
issued for partner clients (Client Credentials flow).

Firebase remains dedicated to end-user authentication for the responsive KAWA Web application.
