# Services/nfr-tests/run_performance.ps1
param(
    [Parameter(Mandatory = $true)][string]$P2Url,
    [Parameter(Mandatory = $true)][string]$P1Url,
    [int]$RatePerMin      = 200,
    [int]$DurationMinutes = 5,
    [int]$TimeoutSec      = 5,
    [int]$Concurrency     = 1
)

$ErrorActionPreference = "Stop"
$ts  = Get-Date -Format "yyyyMMdd_HHmmss"
$art = Join-Path $PSScriptRoot "artifacts"
New-Item -ItemType Directory -Force -Path $art | Out-Null

function StartLoad {
    param([string]$Url, [string]$Prefix)

    $jobs = @()
    # split the global rate across workers as evenly as possible
    $base = [math]::Floor($RatePerMin / $Concurrency)
    $rem  = $RatePerMin % $Concurrency

    for ($i = 1; $i -le $Concurrency; $i++) {
        $r    = $base + ($(if ($i -le $rem) { 1 } else { 0 }))
        $csv  = Join-Path $art "$Prefix`_$i.csv"
        $json = Join-Path $art "$Prefix`_$i.json"

        $sb = {
            param($u,$rate,$dur,$tout,$csvOut,$jsonOut,$root)
            & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root "02_performance.ps1") `
        -Url $u -RatePerMin $rate -DurationMinutes $dur -TimeoutSec $tout -CsvOut $csvOut -JsonOut $jsonOut
            exit $LASTEXITCODE
        }

        $jobs += Start-Job -ScriptBlock $sb -ArgumentList $Url,$r,$DurationMinutes,$TimeoutSec,$csv,$json,$PSScriptRoot
    }

    # wait and drain job output/errors (don't stuff two statements in one () )
    Wait-Job $jobs | Out-Null
    $null = $jobs | Receive-Job -Keep 2>$null
    Remove-Job $jobs -Force | Out-Null

    # merge CSVs and compute stats
    $csvFiles = Get-ChildItem -Path $art -Filter "$Prefix`_*.csv"
    $lat = New-Object System.Collections.Generic.List[int]
    $ok = 0; $fail = 0

    foreach ($f in $csvFiles) {
        foreach ($line in Get-Content $f.FullName) {
            if (-not $line) { continue }
            $parts = $line.Split(',')
            if ($parts.Count -lt 3) { continue }

            # skip headers or malformed lines safely
            try {
                $status = [int]$parts[1]
                $ms     = [int]$parts[2]
            } catch {
                continue
            }

            if ($status -ge 200 -and $status -lt 400) { $ok++; $lat.Add($ms) } else { $fail++ }
        }
    }

    if ($lat.Count -gt 0) {
        $sorted = $lat.ToArray() | Sort-Object
        $idx    = [int][Math]::Ceiling($sorted.Length * 0.95) - 1
        if ($idx -lt 0) { $idx = 0 }
        $p95 = $sorted[$idx]
        $avg = [int]([double]($sorted | Measure-Object -Average).Average)
    } else { $p95 = 0; $avg = 0 }

    [pscustomobject]@{
        url       = $Url
        p95_ms    = $p95
        avg_ms    = $avg
        ok        = $ok
        fail      = $fail
        csvBundle = "$art\$Prefix`_*.csv"
    }
}

Write-Host "== P2 ($P2Url) =="
$p2 = StartLoad -Url $P2Url -Prefix "P2_$ts"

Write-Host "== P1 ($P1Url) =="
$p1 = StartLoad -Url $P1Url -Prefix "P1_$ts"

$impr = if ($p1.p95_ms -gt 0) {
    [math]::Round((($p1.p95_ms - $p2.p95_ms) / [double]$p1.p95_ms) * 100, 2)
} else { 0 }

Write-Host ""
Write-Host "== SUMMARY (rate=$RatePerMin req/min, concurrency=$Concurrency, duration=$DurationMinutes min) =="
Write-Host (" P2: p95={0} ms, avg={1} ms, fail={2}%  -> {3}" -f $p2.p95_ms,$p2.avg_ms, ([int](100*$p2.fail/[math]::Max(1,($p2.ok+$p2.fail)))),$p2.csvBundle)
Write-Host (" P1: p95={0} ms, avg={1} ms, fail={2}%  -> {3}" -f $p1.p95_ms,$p1.avg_ms, ([int](100*$p1.fail/[math]::Max(1,($p1.ok+$p1.fail)))),$p1.csvBundle)
Write-Host (" Improvement (p95): {0}%" -f $impr)

if ($impr -ge 25 -and $p2.fail -eq 0 -and $p1.fail -eq 0) {
    Write-Host "RESULT: PASS"
} else {
    Write-Host "RESULT: FAIL"
}
