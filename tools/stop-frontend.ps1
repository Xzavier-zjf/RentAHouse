$ErrorActionPreference = 'Continue'

$runDir = Join-Path $PSScriptRoot 'run'
$pidFile = Join-Path $runDir 'frontend.pid'
$port = if ($env:RENTAL_FRONTEND_PORT) { [int]$env:RENTAL_FRONTEND_PORT } else { 5173 }
$candidatePids = @()

if (Test-Path -LiteralPath $pidFile) {
    $content = Get-Content -LiteralPath $pidFile -ErrorAction SilentlyContinue
    foreach ($line in $content) {
        if ($line -match '^\d+$') {
            $candidatePids += [int]$line
        }
    }
}

$listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
foreach ($listener in $listeners) {
    $candidatePids += [int]$listener.OwningProcess
}

$candidatePids = $candidatePids | Sort-Object -Unique
if (-not $candidatePids -or $candidatePids.Count -eq 0) {
    Write-Host "Frontend is not running on port $port"
    Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
    exit 0
}

foreach ($processId in $candidatePids) {
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "Stopping frontend pid=$processId port=$port"
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
}

Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
Write-Host 'Frontend stop command finished.'
