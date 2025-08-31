# Services/nfr-tests/run_performance.ps1
param(
    [Parameter(Mandatory=$true)][string]$P2Url,            # e.g. http://localhost:8087/api/books?limit=20
    [Parameter(Mandatory=$true)][string]$P1Url,            # e.g. http://localhost:8091/api/books?limit=20
    [int]$RatePerMin = 200,                                 # Y
    [int]$DurationMinutes = 5,                              # 5–10 recommended
    [string]$TimeUnit = '1m',                               # '1m' or '1s'
    [string]$P2Auth = '',                                   # optional "Bearer <token>"
    [string]$P1Auth = '',                                   # optional "Bearer <token>"
    [int]$VUs = 150, [int]$MaxVUs = 1000
)

$ErrorActionPreference = "Stop"
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$art = Join-Path $here "artifacts"; New-Item -ItemType Directory -Force -Path $art | Out-Null
$k6 = "k6"  # k6.exe must be on PATH

function RunK6([string]$which,[string]$url,[string]$auth){
    $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $json = Join-Path $art "$which`_perf_$stamp.json"
    $args = @(
        "run", (Join-Path $here "02_perf_k6.js"),
        "--summary-export", $json,
        "-e","TARGET=$url",
        "-e","RATE=$RatePerMin",
        "-e","TIME_UNIT=$TimeUnit",
        "-e","DURATION=$($DurationMinutes)m",
        "-e","VUS=$VUs",
        "-e","MAX_VUS=$MaxVUs"
    )
    if ($auth) { $args += @("-e","AUTH=$auth") }
    # run k6, but don't let its console output become function output
    $null = (& $k6 @args 2>&1 | Out-Host)
    $code = $LASTEXITCODE

    # treat code 99 (thresholds) as warning, continue
    if ($code -ne 0 -and $code -ne 99) {
        throw "k6 $which run failed with code $code"
    } elseif ($code -eq 99) {
        Write-Warning "k6 $which thresholds failed (code 99). Continuing so we can compare P1 vs P2."
    }
    return $json

}

function Read-Metrics([string]$jsonPath){
    $j = Get-Content -LiteralPath $jsonPath -Raw | ConvertFrom-Json
    $p95 = [double]$j.metrics.http_req_duration.values.'p(95)'
    $avg = [double]$j.metrics.http_req_duration.values.avg
    $count = [double]$j.metrics.http_reqs.values.count
    $failedRate = [double]$j.metrics.http_req_failed.values.rate
    $rps = [math]::Round($count / ($DurationMinutes*60), 2)
    [pscustomobject]@{ p95=$p95; avg=$avg; count=$count; rps=$rps; failRate=$failedRate; json=$jsonPath }
}


Write-Host "== Running P2 @ $P2Url =="
$p2Json = RunK6 "P2" $P2Url $P2Auth
$p2 = Read-Metrics $p2Json

Write-Host "== Running P1 @ $P1Url =="
$p1Json = RunK6 "P1" $P1Url $P1Auth
$p1 = Read-Metrics $p1Json

$impr = if ($p1.p95 -gt 0) { [math]::Round((($p1.p95 - $p2.p95)/$p1.p95)*100,2) } else { 0 }

"`n== SUMMARY (High demand: $RatePerMin per $TimeUnit for $DurationMinutes m) =="
" P2: p95=${($p2.p95)} ms, avg=${($p2.avg)} ms, rps=${($p2.rps)}, failRate=${($p2.failRate*100)}%, json=$($p2.json)"
" P1: p95=${($p1.p95)} ms, avg=${($p1.avg)} ms, rps=${($p1.rps)}, failRate=${($p1.failRate*100)}%, json=$($p1.json)"
" Improvement (p95): $impr percentage points (target ≥ 25)"
if ($impr -ge 25 -and $p2.failRate -lt 0.01) {
    "RESULT: PASS (≥25% faster p95 and P2 fail rate <1%)" | Write-Host -ForegroundColor Green
} else {
    "RESULT: FAIL (does not meet 25% or fail rate too high)" | Write-Host -ForegroundColor Red
}
