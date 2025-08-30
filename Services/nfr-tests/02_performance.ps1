# Services/nfr-tests/02_performance.ps1
param(
    [Parameter(Mandatory=$true)][string]$Url,
    [int]$RatePerMin = 200,
    [int]$DurationMinutes = 5,
    [int]$TimeoutSec = 2,
    [string]$Auth = "",
    [string]$Accept = "",
    [string]$CsvOut = "",      # <— NEW
    [string]$JsonOut = ""      # <— NEW
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$artDir = Join-Path $scriptDir "artifacts"
New-Item -ItemType Directory -Force -Path $artDir | Out-Null

$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$csv = if ($CsvOut) { $CsvOut } else { Join-Path $artDir "perf_$stamp.csv" }
$json = if ($JsonOut) { $JsonOut } else { Join-Path $artDir "perf_$stamp.json" }

# ensure parent dirs exist for explicit paths
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $csv)  | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $json) | Out-Null

# IMPORTANT: write columns in the order the aggregator expects: ts,status,ms
"ts,status,ms" | Out-File -FilePath $csv -Encoding utf8

$intervalMs = [math]::Floor(60000 / [math]::Max($RatePerMin,1))
$total      = $RatePerMin * $DurationMinutes
$start      = Get-Date
$jobs       = @()

for ($i=0; $i -lt $total; $i++) {
    $scheduled = $start.AddMilliseconds($i * $intervalMs)
    while ((Get-Date) -lt $scheduled) { Start-Sleep -Milliseconds 1 }

    $jobs += Start-Job -ScriptBlock {
        param($u,$auth,$accept,$timeout)
        $ts = [DateTimeOffset]::Now.ToString("o")
        try {
            $sw = [System.Diagnostics.Stopwatch]::StartNew()
            $client = [System.Net.Http.HttpClient]::new()
            $client.Timeout = [TimeSpan]::FromSeconds($timeout)
            $req = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Get, $u)
            if ($auth)   { $req.Headers.Add("Authorization", $auth) }
            if ($accept) { $req.Headers.Add("Accept", $accept) }
            $resp = $client.SendAsync($req).GetAwaiter().GetResult()
            $sw.Stop()
            $code = [int]$resp.StatusCode.value__
            $ms   = [int][math]::Round($sw.Elapsed.TotalMilliseconds)
            [pscustomobject]@{ ts=$ts; status=$code; ms=$ms }
        } catch {
            $ms = $timeout * 1000
            [pscustomobject]@{ ts=$ts; status=0; ms=$ms }
        }
    } -ArgumentList $Url,$Auth,$Accept,$TimeoutSec
}

Wait-Job -Job $jobs | Out-Null
$rows = Receive-Job $jobs
Remove-Job $jobs -Force | Out-Null

# write CSV rows in the same order as header
$rows | ForEach-Object { "$($_.ts),$($_.status),$($_.ms)" } | Out-File -Append -FilePath $csv -Encoding utf8

# stats (success = 2xx–3xx)
$all    = $rows.Count
$okRows = $rows | Where-Object { $_.status -ge 200 -and $_.status -lt 400 }
$ok     = $okRows.Count
$failRt = if ($all -gt 0) { [math]::Round(100.0 * ($all - $ok) / $all, 2) } else { 0.0 }
$avg    = if ($ok -gt 0) { [math]::Round(($okRows | Measure-Object -Property ms -Average).Average, 2) } else { 0.0 }
$sorted = @($okRows | Sort-Object ms | Select-Object -ExpandProperty ms)
$p95    = if ($ok -gt 0) { $sorted[[int][math]::Floor(0.95 * ($ok - 1))] } else { 0 }

@{
    target=$Url; ratePerMin=$RatePerMin; durationMinutes=$DurationMinutes; timeoutSec=$TimeoutSec;
    count=$all; successes=$ok; failRatePercent=$failRt; p95ms=$p95; avgms=$avg; csv=$csv
} | ConvertTo-Json | Out-File -FilePath $json -Encoding utf8

Write-Host "SUMMARY $Url -> p95=${p95}ms, avg=${avg}ms, ok=$ok/$all, failRate=${failRt}%"
Write-Host "Artifacts: CSV=$csv JSON=$json"
exit 0
