param(
    [Parameter(Mandatory=$true)][string]$P2Url,           # e.g. http://localhost:8087/actuator/health
    [Parameter(Mandatory=$true)][string]$P1Url,           # e.g. http://localhost:8091/health or /actuator/health
    [int]$DurationMinutes = 10,
    [int]$IntervalSec = 5,
    [string]$P2BgUrl = "",                                # optional, e.g. http://localhost:8087/api/books?limit=10
    [string]$P1BgUrl = "",                                # optional, e.g. http://localhost:8091/api/books?limit=10
    [switch]$NoBackground                                # set to skip background load
)

$ErrorActionPreference = "Stop"
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$probe = Join-Path $here "01_availability.ps1"
$artDir = Join-Path $here "artifacts"
New-Item -ItemType Directory -Force -Path $artDir | Out-Null

function StartLoad([string]$url, [int]$mins) {
    if ([string]::IsNullOrWhiteSpace($url)) { return $null }
    Write-Host "  > starting background load -> $url ($mins min)"
    return Start-Job -ScriptBlock {
        param($u,$m)
        $end=(Get-Date).AddMinutes($m)
        while((Get-Date) -lt $end){
            try { Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 -Uri $u | Out-Null } catch {}
            Start-Sleep -Milliseconds 900
        }
    } -ArgumentList $url,$mins
}

function RunProbe([string]$name,[string]$url,[int]$mins,[int]$interval){
    Write-Host "== $name probe =="
    $before = Get-ChildItem "$artDir\availability_*.csv" -ErrorAction SilentlyContinue

    # Run the child probe and suppress all its output
    $null = & PowerShell -NoProfile -ExecutionPolicy Bypass -File $probe `
            -Url $url -PeriodSec ($mins*60) -IntervalSec $interval *> $null

    $after = Get-ChildItem "$artDir\availability_*.csv"
    $new = $after | Where-Object { $before -notcontains $_ } | Sort-Object LastWriteTime | Select-Object -Last 1
    if (-not $new) { throw "Could not find new artifact for $name" }
    $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $dest = Join-Path $artDir "$name`_availability_$stamp.csv"
    Copy-Item $new.FullName $dest -Force
    return $dest
}

function ComputeAvailability([object]$csvPath){
    # If an array sneaks in, pick the last string
    if ($csvPath -is [array]) { $csvPath = $csvPath[-1] }
    $csvPath = [string]$csvPath
    $lines = Get-Content -LiteralPath $csvPath
    if ($lines.Count -lt 2) { return 0.0 }
    $rows = $lines | Select-Object -Skip 1
    $total = $rows.Count
    $ok = 0
    foreach($r in $rows){
        $parts = $r.Split(',')
        if ($parts.Length -ge 3) {
            $code = $parts[2].Trim()
            if ($code -match '^(2|3)\d\d$') { $ok++ }
        }
    }
    if ($total -eq 0) { return 0.0 }
    return [Math]::Round(100.0*$ok/$total,2)
}


# ---- P2 run ----
if (-not $NoBackground) { $p2Job = StartLoad -url $P2BgUrl -mins $DurationMinutes }
$p2Csv = RunProbe -name "P2" -url $P2Url -mins $DurationMinutes -interval $IntervalSec
if ($p2Job) { Stop-Job $p2Job -ErrorAction SilentlyContinue; Remove-Job $p2Job -ErrorAction SilentlyContinue }

# ---- P1 run ----
if (-not $NoBackground) { $p1Job = StartLoad -url $P1BgUrl -mins $DurationMinutes }
$p1Csv = RunProbe -name "P1" -url $P1Url -mins $DurationMinutes -interval $IntervalSec
if ($p1Job) { Stop-Job $p1Job -ErrorAction SilentlyContinue; Remove-Job $p1Job -ErrorAction SilentlyContinue }

# ---- Summary ----
$p2 = ComputeAvailability $p2Csv
$p1 = ComputeAvailability $p1Csv
$delta = [Math]::Round($p2 - $p1, 2)

Write-Host ""
Write-Host "== SUMMARY =="
Write-Host ("P2 availability: {0}%  -> {1}" -f $p2, $p2Csv)
Write-Host ("P1 availability: {0}%  -> {1}" -f $p1, $p1Csv)
Write-Host ("Improvement:     {0} percentage points" -f $delta)
