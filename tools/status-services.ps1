$ErrorActionPreference = 'Continue'

$runDir = Join-Path $PSScriptRoot 'run'
$services = @(
    @{ Name = 'rental-user';    Port = 9001 },
    @{ Name = 'rental-house';   Port = 9002 },
    @{ Name = 'rental-message'; Port = 9005 },
    @{ Name = 'rental-comment'; Port = 9004 },
    @{ Name = 'rental-order';   Port = 9003 },
    @{ Name = 'rental-gateway'; Port = 8888 },
    @{ Name = 'frontend';       Port = if ($env:RENTAL_FRONTEND_PORT) { [int]$env:RENTAL_FRONTEND_PORT } else { 5173 } }
)

$rows = foreach ($svc in $services) {
    $name = $svc.Name
    $port = [int]$svc.Port
    $pidFile = Join-Path $runDir "$name.pid"
    $savedPid = if (Test-Path -LiteralPath $pidFile) {
        (Get-Content -LiteralPath $pidFile -ErrorAction SilentlyContinue | Select-Object -First 1)
    } else {
        ''
    }

    $listener = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -First 1

    [pscustomobject]@{
        Service = $name
        Port = $port
        Listening = [bool]$listener
        OwningProcess = if ($listener) { $listener.OwningProcess } else { '' }
        SavedPid = $savedPid
    }
}

$rows | Format-Table -AutoSize

Write-Host ''
Write-Host 'Infrastructure:'
foreach ($dep in @(
    @{ Name = 'MySQL'; Port = 3306 },
    @{ Name = 'Redis'; Port = 6379 },
    @{ Name = 'RabbitMQ'; Port = 5672 },
    @{ Name = 'RabbitMQ Management'; Port = 15672 }
)) {
    $listener = Get-NetTCPConnection -LocalPort ([int]$dep.Port) -State Listen -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($listener) {
        Write-Host "$($dep.Name) OK on port $($dep.Port)"
    } else {
        Write-Host "$($dep.Name) is not listening on port $($dep.Port)"
    }
}
