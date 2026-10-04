# DEV deployment configuration

The `deploy-dev.yml` workflow uses the GitHub Actions environment named `dev`.
Set the retailer portal's Firebase web configuration as environment variables:

- `VITE_FIREBASE_API_KEY`
- `VITE_FIREBASE_AUTH_DOMAIN`
- `VITE_FIREBASE_PROJECT_ID`
- `VITE_FIREBASE_APP_ID`

`VITE_FIREBASE_PROJECT_ID` is also passed to `retailer-service` as
`KAWA_FIREBASE_PROJECT_ID` in the encrypted `/kawa/dev/runtime-env` SSM
parameter. No separate backend project-ID variable is required.

Set `FIREBASE_SERVICE_ACCOUNT_B64` as a GitHub environment secret containing
the base64-encoded Firebase service-account JSON. The workflow decodes it in
a restricted temporary file on the ephemeral runner and stores it in the
`/kawa/dev/firebase-service-account` SSM `SecureString`. The EC2 deploy script
materializes it as a host file and mounts it read-only into the backend
services that use Firebase Admin ADC. It is not part of either frontend image.
Do not put this credential in a GitHub variable, a plaintext SSM parameter, or
the repository.
