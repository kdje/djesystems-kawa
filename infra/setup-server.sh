#!/usr/bin/env bash
set -Eeuo pipefail

FRONT_DOMAIN="${1:-dev.kawa-retail.com}"
API_DOMAIN="${2:-api-dev.kawa-retail.com}"

if [[ "$(id -u)" -eq 0 ]]; then
  SUDO=""
else
  SUDO="sudo"
fi

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

log "Updating Amazon Linux 2023..."
$SUDO dnf update -y

log "Installing required packages..."
$SUDO dnf install -y docker nginx certbot python3-certbot-nginx curl

log "Enabling Docker and Nginx..."
$SUDO systemctl enable --now docker
$SUDO systemctl enable --now nginx

if id ec2-user >/dev/null 2>&1; then
  $SUDO usermod -aG docker ec2-user
fi
if id ssm-user >/dev/null 2>&1; then
  $SUDO usermod -aG docker ssm-user
fi

log "Checking Docker Compose plugin..."
if ! docker compose version >/dev/null 2>&1 && ! $SUDO docker compose version >/dev/null 2>&1; then
  ARCH="$(uname -m)"
  case "$ARCH" in
    x86_64) COMPOSE_ARCH="x86_64" ;;
    aarch64|arm64) COMPOSE_ARCH="aarch64" ;;
    *) echo "Unsupported architecture: $ARCH" >&2; exit 1 ;;
  esac

  COMPOSE_VERSION="$(
    curl -fsSL https://api.github.com/repos/docker/compose/releases/latest \
      | sed -n 's/.*"tag_name":[[:space:]]*"\([^"]*\)".*/\1/p' \
      | head -n 1
  )"

  [[ -n "$COMPOSE_VERSION" ]] || { echo "Unable to determine Docker Compose version" >&2; exit 1; }

  log "Installing Docker Compose ${COMPOSE_VERSION}..."
  $SUDO mkdir -p /usr/local/lib/docker/cli-plugins
  $SUDO curl -fL \
    "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-${COMPOSE_ARCH}" \
    -o /usr/local/lib/docker/cli-plugins/docker-compose
  $SUDO chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
fi

log "Preparing KAWA directories..."
$SUDO mkdir -p /opt/kawa/secrets /opt/kawa/logs
$SUDO chown -R ec2-user:ec2-user /opt/kawa

log "Configuring Nginx reverse proxy..."
$SUDO tee /etc/nginx/conf.d/kawa-dev.conf >/dev/null <<NGINX
server {
    listen 80;
    listen [::]:80;
    server_name ${FRONT_DOMAIN};

    location / {
        proxy_pass http://127.0.0.1:5173;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}

server {
    listen 80;
    listen [::]:80;
    server_name ${API_DOMAIN};

    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
NGINX

$SUDO nginx -t
$SUDO systemctl reload nginx

log "Creating TLS helper..."
$SUDO tee /usr/local/bin/kawa-enable-tls >/dev/null <<'TLS'
#!/usr/bin/env bash
set -Eeuo pipefail
EMAIL="${1:?Usage: kawa-enable-tls <email> [front-domain] [api-domain]}"
FRONT_DOMAIN="${2:-dev.kawa-retail.com}"
API_DOMAIN="${3:-api-dev.kawa-retail.com}"

sudo certbot --nginx \
  --non-interactive \
  --agree-tos \
  --redirect \
  --email "$EMAIL" \
  -d "$FRONT_DOMAIN" \
  -d "$API_DOMAIN"
TLS
$SUDO chmod +x /usr/local/bin/kawa-enable-tls

log "Versions:"
docker --version || $SUDO docker --version
docker compose version || $SUDO docker compose version
aws --version
nginx -v

log "KAWA EC2 bootstrap completed."
log "Open a new SSH/SSM session before using Docker without sudo."
log "After DNS points to this EC2, enable HTTPS with:"
log "  sudo kawa-enable-tls <email> ${FRONT_DOMAIN} ${API_DOMAIN}"
