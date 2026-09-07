$ErrorActionPreference="Stop"
mvn clean package -DskipTests
docker compose -f infra/local/docker-compose.yml up --build
