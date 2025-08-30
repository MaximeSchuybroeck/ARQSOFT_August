param(
  [Parameter(Mandatory=$true)][string]$Url,
  [int]$PeriodSec = 3600,
  [int]$IntervalSec = 5,
  [int]$TimeoutSec = 2
)

$ErrorActionPreference = "Stop"

# Always write artifacts next to THIS script (absolute path)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$artDir = Join-Path $scriptDir "artifacts"
New-Item -ItemType Directory -Force -Path $artDir | Out-Null

$stamp = Get-Date -Format "yyyyMMddHHmmss"
$out = Join-Path $artDir "availability_$stamp.csv"
"ts,ms,status" | Out-File -FilePath $out -Encoding utf8

$deadline = (Get-Date).AddSeconds($PeriodSec)
$ok = 0; $fail = 0

while ((Get-Date) -lt $deadline) {
  $ts = (Get-Date).ToString("o")
  try {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $resp = Invoke-WebRequest -Uri $Url -Method GET -TimeoutSec $TimeoutSec
    $sw.Stop()
    $code = [int]$resp.StatusCode
    $ms = [int][Math]::Round($sw.Elapsed.TotalMilliseconds)
    if ($code -ge 200 -and $code -lt 400) { $ok++ } else { $fail++ }
  } catch {
    $ms = [int]($TimeoutSec * 1000)
    $code = 0
    $fail++
  }
  "$ts,$ms,$code" | Out-File -Append -FilePath $out -Encoding utf8
  Start-Sleep -Seconds $IntervalSec
}

$total = $ok + $fail
$avail = if ($total -eq 0) { 0.0 } else { [Math]::Round(100.0 * $ok / $total, 2) }
Write-Host ("Availability: {0}% (OK={1} / TOTAL={2})" -f $avail, $ok, $total)
