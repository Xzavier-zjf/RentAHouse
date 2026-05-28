$ErrorActionPreference = 'Stop'

$scriptDir = $PSScriptRoot

Write-Host 'Starting backend services...'
& (Join-Path $scriptDir 'start-services.ps1')
if ($LASTEXITCODE -ne 0) {
    throw 'Backend startup failed.'
}

Write-Host ''
Write-Host 'Starting frontend...'
& (Join-Path $scriptDir 'start-frontend.ps1')
if ($LASTEXITCODE -ne 0) {
    throw 'Frontend startup failed.'
}

Write-Host ''
Write-Host 'Current status:'
& (Join-Path $scriptDir 'status-services.ps1')

Write-Host ''
Write-Host 'All services startup command finished.'
Write-Host 'Frontend: http://127.0.0.1:5173'
Write-Host 'Gateway:  http://127.0.0.1:8888'
