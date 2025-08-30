# nfr/01_availability.sh
#!/usr/bin/env bash
set -euo pipefail
URL="${1:?Usage: $0 BASE_URL}"; PERIOD_SEC="${PERIOD_SEC:-3600}"; INTERVAL="${INTERVAL:-5}"
DEADLINE=$((SECONDS+PERIOD_SEC)); OK=0; FAIL=0
mkdir -p artifacts; OUT="artifacts/availability_$(date +%Y%m%d%H%M%S).csv"
echo "ts,ms,status" > "$OUT"
while [ $SECONDS -lt $DEADLINE ]; do
  TS=$(date -Iseconds)
  RES=$(curl -sS -w "%{http_code},%{time_total}" -o /dev/null --max-time 2 "$URL" || echo "000,2.000")
  code="${RES%%,*}"; t_ms=$(awk -v s="${RES#*,}" 'BEGIN{printf "%.0f", s*1000}')
  if [[ "$code" =~ ^[23] ]]; then OK=$((OK+1)); else FAIL=$((FAIL+1)); fi
  echo "$TS,$t_ms,$code" >> "$OUT"
  sleep "$INTERVAL"
done
TOTAL=$((OK+FAIL))
AVAIL=$(python - <<PY
ok=$OK; total=$TOTAL
print(round(100.0*ok/max(total,1),2))
PY
)
echo "Availability: $AVAIL% (OK=$OK / TOTAL=$TOTAL)"
