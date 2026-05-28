$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $root 'frontend'
$runDir = Join-Path $PSScriptRoot 'run'
$logDir = Join-Path $PSScriptRoot 'logs'
$port = if ($env:RENTAL_FRONTEND_PORT) { [int]$env:RENTAL_FRONTEND_PORT } else { 5173 }

New-Item -ItemType Directory -Path $runDir -Force | Out-Null
New-Item -ItemType Directory -Path $logDir -Force | Out-Null

function Test-Port {
    param([int]$Port)
    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    return $null -ne $conn
}

if (-not (Test-Path -LiteralPath (Join-Path $frontendDir 'index.html'))) {
    throw "Frontend index.html not found under $frontendDir"
}

if (Test-Port -Port $port) {
    $pid = (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty OwningProcess)
    Set-Content -LiteralPath (Join-Path $runDir 'frontend.pid') -Value $pid -Encoding ASCII
    Write-Host "Frontend already listens on port $port (pid=$pid)"
    Write-Host "URL: http://127.0.0.1:$port"
    exit 0
}

$node = Get-Command node -ErrorAction SilentlyContinue
if (-not $node) {
    throw 'Node.js was not found. Install Node.js or open frontend/index.html directly in a browser.'
}

$logFile = Join-Path $logDir 'frontend.log'
$errFile = Join-Path $logDir 'frontend.err.log'
Remove-Item -LiteralPath $logFile -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $errFile -Force -ErrorAction SilentlyContinue

$serverScript = Join-Path $PSScriptRoot 'frontend-server.js'
if (-not (Test-Path -LiteralPath $serverScript)) {
    throw "Frontend server script not found: $serverScript"
}

$process = Start-Process -FilePath $node.Source `
    -ArgumentList @($serverScript, "$port") `
    -WorkingDirectory $root `
    -WindowStyle Hidden `
    -RedirectStandardOutput $logFile `
    -RedirectStandardError $errFile `
    -PassThru

Set-Content -LiteralPath (Join-Path $runDir 'frontend.pid') -Value $process.Id -Encoding ASCII
Start-Sleep -Seconds 2

if (-not (Test-Port -Port $port)) {
    throw "Frontend failed to listen on port $port. Check $errFile and $logFile"
}

Write-Host "Frontend started on http://127.0.0.1:$port"
Write-Host "Logs: $logFile"
