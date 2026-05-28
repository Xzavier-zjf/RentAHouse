$ErrorActionPreference = 'Continue'

$runDir = Join-Path $PSScriptRoot 'run'
$services = @(
    @{ Name = 'rental-gateway'; Port = 8888 },
    @{ Name = 'rental-order';   Port = 9003 },
    @{ Name = 'rental-comment'; Port = 9004 },
    @{ Name = 'rental-message'; Port = 9005 },
    @{ Name = 'rental-house';   Port = 9002 },
    @{ Name = 'rental-user';    Port = 9001 }
)

function Stop-ServiceProcess {
    param([string]$Name, [int]$Port)

    $pidFile = Join-Path $runDir "$Name.pid"
    $candidatePids = @()

    if (Test-Path -LiteralPath $pidFile) {
        $content = Get-Content -LiteralPath $pidFile -ErrorAction SilentlyContinue
        foreach ($line in $content) {
            if ($line -match '^\d+$') {
                $candidatePids += [int]$line
            }
        }
    }

    $listeners = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    foreach ($listener in $listeners) {
        $candidatePids += [int]$listener.OwningProcess
    }

    $candidatePids = $candidatePids | Sort-Object -Unique
    if (-not $candidatePids -or $candidatePids.Count -eq 0) {
        Write-Host "$Name is not running on port $Port"
        Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
        return
    }

    foreach ($processId in $candidatePids) {
        $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
        if ($process) {
            Write-Host "Stopping $Name pid=$processId port=$Port"
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        }
    }

    Remove-Item -LiteralPath $pidFile -Force -ErrorAction SilentlyContinue
}

foreach ($svc in $services) {
    Stop-ServiceProcess -Name $svc.Name -Port ([int]$svc.Port)
}

Write-Host 'Backend services stop command finished.'
