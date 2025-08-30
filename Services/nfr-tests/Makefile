P1_URL?=https://p1.example
P2_URL?=https://p2.example
.PHONY: availability
availability:
	@echo "P1 run"; k6 run -e TARGET="$(P1_URL)" nfr/bg_load_k6.js & \
	 ./nfr/01_availability.sh "$(P1_URL)/api/health"
	@echo "P2 run"; k6 run -e TARGET="$(P2_URL)" nfr/bg_load_k6.js & \
	 ./nfr/01_availability.sh "$(P2_URL)/api/health"
