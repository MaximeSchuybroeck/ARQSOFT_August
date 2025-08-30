// nfr/bg_load_k6.js
import http from 'k6/http'; import { sleep } from 'k6';
export let options = { vus: 20, duration: '60m', thresholds: { http_req_failed: ['rate<0.02'] } };
export default function () {
    http.get(`${__ENV.TARGET}/api/books?limit=10`);
    sleep(1.0);
}