// k6 load test: an authenticated user repeatedly reading their profile (JWT validation, rate limiter,
// cache and database). Run it with scripts/load-test.sh; see that script for the target app's settings.
//
// Environment: BASE_URL (default http://localhost:8080), VUS (default 20), DURATION (default 30s),
// THINK_TIME seconds each user waits between requests (default 0.1; use 0 to measure maximum throughput).
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const THINK_TIME = Number(__ENV.THINK_TIME ?? 0.1);
const JSON_HEADERS = { headers: { 'Content-Type': 'application/json' } };

export const options = {
  vus: Number(__ENV.VUS || 20),
  duration: __ENV.DURATION || '30s',
  // The run fails if more than 1% of requests fail or the 95th percentile exceeds 300 ms.
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<300'],
  },
};

export function setup() {
  const username = `load_${Date.now()}`;
  const password = 'Load#12345';

  const registered = http.post(`${BASE_URL}/api/v1/auth/register`,
    JSON.stringify({ username, email: `${username}@example.com`, password }), JSON_HEADERS);
  if (registered.status !== 201) {
    throw new Error(`Registration failed: ${registered.status} ${registered.body}`);
  }

  const login = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({ identifier: username, password }), JSON_HEADERS);
  if (login.status !== 200) {
    throw new Error(`Login failed: ${login.status} ${login.body} (is REQUIRE_VERIFIED_EMAIL off?)`);
  }
  return { token: login.json('accessToken') };
}

export default function (data) {
  const response = http.get(`${BASE_URL}/api/v1/users/me`, { headers: { Authorization: `Bearer ${data.token}` } });
  check(response, {
    'status is 200': (r) => r.status === 200,
    'not rate limited': (r) => r.status !== 429,
  });
  if (THINK_TIME > 0) {
    sleep(THINK_TIME);
  }
}
