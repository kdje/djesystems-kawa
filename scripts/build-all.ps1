$ErrorActionPreference="Stop"
mvn clean verify
Push-Location apps/kawa-platform
npm install
npm run build
Pop-Location
