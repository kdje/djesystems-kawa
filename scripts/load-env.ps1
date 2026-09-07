$envFile = Join-Path $PSScriptRoot "..\.env.local"

if (-not (Test-Path $envFile)) {
    Write-Host "Fichier .env.local introuvable : $envFile" -ForegroundColor Red
    exit 1
}

Get-Content $envFile | ForEach-Object {

    $line = $_.Trim()

    # Ignore les lignes vides et commentaires
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }

    $parts = $line -split "=", 2

    if ($parts.Length -eq 2) {

        $name = $parts[0].Trim()
        $value = $parts[1].Trim()

        [Environment]::SetEnvironmentVariable(
            $name,
            $value,
            "Process"
        )

        Write-Host "Loaded: $name"
    }
}


$trustStore = Join-Path $PSScriptRoot "..\certs\kawa-cacerts"

if (Test-Path $trustStore) {

    $env:JAVA_TOOL_OPTIONS = `
        "-Djavax.net.ssl.trustStore=$trustStore -Djavax.net.ssl.trustStorePassword=changeit"

    Write-Host "Java truststore KAWA chargé : $trustStore" -ForegroundColor Green
}
else {
    Write-Host "Truststore KAWA introuvable : $trustStore" -ForegroundColor Red
}

Write-Host "Variables KAWA chargées depuis .env.local" -ForegroundColor Green