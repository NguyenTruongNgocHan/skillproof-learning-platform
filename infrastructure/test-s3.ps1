$ErrorActionPreference = "Stop"

$repo = Split-Path -Parent $PSScriptRoot
$payload = Join-Path $PSScriptRoot "s3-it-payload.txt"
$download = Join-Path $PSScriptRoot "s3-it-payload-downloaded.txt"
Set-Content -LiteralPath $payload -Value "skillproof-s3-integration"

try {
    & (Join-Path $PSScriptRoot "init-bucket.ps1")
    $mount = "type=bind,source=$PSScriptRoot,target=/workspace"
    $common = @("--rm", "--network", "infrastructure_default", "--mount", $mount,
        "-e", "AWS_ACCESS_KEY_ID=skillproof", "-e", "AWS_SECRET_ACCESS_KEY=skillproof123",
        "amazon/aws-cli:latest")
    & docker run @common s3 cp /workspace/s3-it-payload.txt s3://skillproof-media/it/s3-it-payload.txt --endpoint-url http://minio:9000
    & docker run @common s3 cp s3://skillproof-media/it/s3-it-payload.txt /workspace/s3-it-payload-downloaded.txt --endpoint-url http://minio:9000
    if ((Get-FileHash $payload).Hash -ne (Get-FileHash $download).Hash) {
        throw "S3 download bytes do not match upload bytes."
    }
    & docker run @common s3 rm s3://skillproof-media/it/s3-it-payload.txt --endpoint-url http://minio:9000
    if ($LASTEXITCODE -ne 0) {
        throw "S3 delete failed."
    }
    Write-Output "S3 integration upload/download/delete: PASS"
}
finally {
    Remove-Item -LiteralPath $payload, $download -Force -ErrorAction SilentlyContinue
}
