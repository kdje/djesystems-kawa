#!/usr/bin/env bash
set -Eeuo pipefail

KAWA_HOME="${KAWA_HOME:-/opt/kawa}"
ENV_NAME="${KAWA_ENV:-dev}"
PARAM_PREFIX="/kawa/${ENV_NAME}"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"; }

mkdir -p "${KAWA_HOME}/secrets"
cd "${KAWA_HOME}"
umask 077

log "Loading runtime configuration from SSM Parameter Store..."
aws ssm get-parameter --name "${PARAM_PREFIX}/runtime-env" --with-decryption --query 'Parameter.Value' --output text > .env.tmp
aws ssm get-parameter --name "${PARAM_PREFIX}/firebase-service-account" --with-decryption --query 'Parameter.Value' --output text > secrets/firebase-service-account.json.tmp
mv .env.tmp .env
mv secrets/firebase-service-account.json.tmp secrets/firebase-service-account.json
chmod 600 .env secrets/firebase-service-account.json

AWS_REGION="$(grep '^AWS_REGION=' .env | cut -d= -f2-)"
ECR_REGISTRY="$(grep '^ECR_REGISTRY=' .env | cut -d= -f2-)"
IMAGE_TAG="$(grep '^IMAGE_TAG=' .env | cut -d= -f2-)"
[[ -n "$AWS_REGION" && -n "$ECR_REGISTRY" && -n "$IMAGE_TAG" ]] || { echo "Missing AWS_REGION/ECR_REGISTRY/IMAGE_TAG" >&2; exit 1; }

log "Authenticating Docker to ECR..."
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$ECR_REGISTRY"

log "Pulling KAWA images (${IMAGE_TAG})..."
docker compose --env-file .env -f compose.dev.yml pull

log "Starting KAWA..."
docker compose --env-file .env -f compose.dev.yml up -d --remove-orphans

wait_http() {
  local name="$1" url="$2" max_attempts="${3:-60}"
  for attempt in $(seq 1 "$max_attempts"); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      log "${name} is healthy: ${url}"
      return 0
    fi
    sleep 5
  done
  echo "${name} did not become healthy: ${url}" >&2
  return 1
}

log "Waiting for application health checks..."
wait_http "customer-service" "http://127.0.0.1:8081/actuator/health" 90
wait_http "wallet-service" "http://127.0.0.1:8082/actuator/health" 90
wait_http "retailer-service" "http://127.0.0.1:8083/actuator/health" 90
wait_http "kawa-platform" "http://127.0.0.1:5173/" 30

docker compose --env-file .env -f compose.dev.yml ps
docker image prune -f
log "KAWA DEV deployment completed successfully."
