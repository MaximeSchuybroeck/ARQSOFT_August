import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        high_demand: {
            executor: 'constant-arrival-rate',
            rate: Number(__ENV.RATE || 200),        // requests per TIME_UNIT
            timeUnit: __ENV.TIME_UNIT || '1m',      // '1s' or '1m'
            duration: __ENV.DURATION || '5m',       // test window
            preAllocatedVUs: Number(__ENV.VUS || 100),
            maxVUs: Number(__ENV.MAX_VUS || 1000)
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'], // <1% errors
        // set a generous default; we’ll compare P1 vs P2 anyway
        http_req_duration: [`p(95)<${__ENV.P95 || 2000}`],
    },
};

const headers = {};
if (__ENV.AUTH) headers['Authorization'] = __ENV.AUTH;
if (__ENV.ACCEPT) headers['Accept'] = __ENV.ACCEPT;

export default function () {
    const url = __ENV.TARGET;
    const res = http.get(url, { headers });
    check(res, {
        'status 2xx/3xx': (r) => r.status >= 200 && r.status < 400,
    });
}
