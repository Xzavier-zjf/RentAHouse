$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$runDir = Join-Path $PSScriptRoot 'run'
$logDir = Join-Path $PSScriptRoot 'logs'

New-Item -ItemType Directory -Path $runDir -Force | Out-Null
New-Item -ItemType Directory -Path $logDir -Force | Out-Null

$services = @(
    @{ Name = 'rental-user';    Port = 9001; Jar = 'rental-user\target\rental-user-0.0.1-SNAPSHOT.jar' },
    @{ Name = 'rental-house';   Port = 9002; Jar = 'rental-house\target\rental-house-0.0.1-SNAPSHOT.jar' },
    @{ Name = 'rental-message'; Port = 9005; Jar = 'rental-message\target\rental-message-0.0.1-SNAPSHOT.jar' },
    @{ Name = 'rental-comment'; Port = 9004; Jar = 'rental-comment\target\rental-comment-0.0.1-SNAPSHOT.jar' },
    @{ Name = 'rental-order';   Port = 9003; Jar = 'rental-order\target\rental-order-0.0.1-SNAPSHOT.jar' },
    @{ Name = 'rental-gateway'; Port = 8888; Jar = 'rental-gateway\target\rental-gateway-0.0.1-SNAPSHOT.jar' }
)

function Test-Port {
    param([int]$Port)
    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    return $null -ne $conn
}

function Wait-Port {
    param(
        [string]$Name,
        [int]$Port,
        [int]$TimeoutSeconds = 90
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-Port -Port $Port) {
            Write-Host "$Name is listening on port $Port"
            return $true
        }
        Start-Sleep -Seconds 2
    }
    Write-Warning "$Name did not listen on port $Port within $TimeoutSeconds seconds"
    return $false
}

function Test-Dependency {
    param([string]$Name, [int]$Port)
    if (Test-Port -Port $Port) {
        Write-Host "$Name OK on port $Port"
    } else {
        Write-Warning "$Name is not listening on port $Port. Start it before using the full system."
    }
}

Set-Location $root

Write-Host 'Checking infrastructure services...'
Test-Dependency -Name 'MySQL' -Port 3306
Test-Dependency -Name 'Redis' -Port 6379
Test-Dependency -Name 'RabbitMQ' -Port 5672

$runningPorts = @()
foreach ($svc in $services) {
    if (Test-Port -Port ([int]$svc.Port)) {
        $runningPorts += [int]$svc.Port
    }
}

if ($runningPorts.Count -eq 0) {
    Write-Host 'Packaging backend modules...'
    & .\mvnw.cmd -q -DskipTests package
    if ($LASTEXITCODE -ne 0) {
        throw 'Maven package failed.'
    }
} else {
    Write-Host "Skipping Maven package because one or more service ports are already in use: $($runningPorts -join ', ')"
    Write-Host 'Run tools/stop-services.ps1 first when you need to rebuild jars.'
}

foreach ($svc in $services) {
    $name = $svc.Name
    $port = [int]$svc.Port
    $jar = Join-Path $root $svc.Jar
    $pidFile = Join-Path $runDir "$name.pid"
    $logFile = Join-Path $logDir "$name.log"
    $errFile = Join-Path $logDir "$name.err.log"

    if (Test-Port -Port $port) {
        $pid = (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty OwningProcess)
        Write-Host "$name already listens on port $port (pid=$pid)"
        Set-Content -LiteralPath $pidFile -Value $pid -Encoding ASCII
        continue
    }

    if (-not (Test-Path -LiteralPath $jar)) {
        throw "Jar not found: $jar"
    }

    if (Test-Path -LiteralPath $logFile) {
        Remove-Item -LiteralPath $logFile -Force
    }
    if (Test-Path -LiteralPath $errFile) {
        Remove-Item -LiteralPath $errFile -Force
    }

    Write-Host "Starting $name on port $port"
    $process = Start-Process -FilePath 'java' `
        -ArgumentList @('-Dfile.encoding=UTF-8', '-jar', $jar) `
        -WorkingDirectory $root `
        -WindowStyle Hidden `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $errFile `
        -PassThru

    Set-Content -LiteralPath $pidFile -Value $process.Id -Encoding ASCII
    Wait-Port -Name $name -Port $port | Out-Null
}

Write-Host ''
Write-Host 'Backend services startup command finished.'
Write-Host "Logs: $logDir"
Write-Host 'Gateway: http://localhost:8888'
