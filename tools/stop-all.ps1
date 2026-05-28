$ErrorActionPreference = 'Continue'

$scriptDir = $PSScriptRoot

Write-Host 'Stopping frontend...'
& (Join-Path $scriptDir 'stop-frontend.ps1')

Write-Host ''
Write-Host 'Stopping backend services...'
& (Join-Path $scriptDir 'stop-services.ps1')

Write-Host ''
Write-Host 'Current status:'
& (Join-Path $scriptDir 'status-services.ps1')

Write-Host ''
Write-Host 'All services stop command finished.'
