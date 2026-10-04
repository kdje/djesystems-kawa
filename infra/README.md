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

## Frontend host routing for the retailer portal

The portal image is published on port `5174`, while `dev.kawa-retail.com/`
continues to proxy to the customer `kawa-platform` on port `5173`. The frontend
Nginx vhost must route `/retailer-portal/` and `/api/retailer-portal/` to the
host gateway at `127.0.0.1:8080`; the gateway then serves the portal SPA and
routes its API calls. The proxy must preserve both URIs, and the portal deploy
build intentionally uses an empty `VITE_RETAILER_API_BASE_URL` for same-origin
requests.

`infra/setup-server.sh` installs
`/etc/nginx/snippets/kawa-retailer-portal-locations.conf` and includes it in
the generated HTTP frontend vhost. Do **not** rerun that full provisioning
script on an already configured host: it rewrites
`/etc/nginx/conf.d/kawa-dev.conf` and may damage Certbot-managed TLS settings.
To update an existing host safely:

1. Inspect `sudo nginx -T` and identify the HTTP and HTTPS `server` blocks
   whose `server_name` is `dev.kawa-retail.com`.
2. Install the tracked location snippet without replacing the vhost:
   `sudo install -D -m 0644 infra/nginx/kawa-retailer-portal-locations.conf /etc/nginx/snippets/kawa-retailer-portal-locations.conf`
3. Add `include /etc/nginx/snippets/kawa-retailer-portal-locations.conf;`
   inside each of those frontend server blocks, before the general `location /`
   proxy. Do not add it to the `api-dev.kawa-retail.com` vhost.
4. Run `sudo nginx -t` and only if it succeeds run
   `sudo systemctl reload nginx`.

This keeps the root platform route unchanged and does not edit certificates or
the API gateway. It must be applied to the host separately; a frontend image
deployment does not update this external Nginx configuration.
