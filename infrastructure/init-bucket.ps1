$ErrorActionPreference = "Stop"

Set-Location (Join-Path $PSScriptRoot "..")
docker compose -f "infrastructure\docker-compose.yml" run --rm minio-init
if ($LASTEXITCODE -ne 0) {
    throw "Object storage bucket initialization failed."
}
