import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    baseline: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 20),
      duration: __ENV.DURATION || '1m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<500'],
  },
};

const authUrl = __ENV.AUTH_URL || 'http://localhost:8081';
const resourceUrl = __ENV.RESOURCE_URL || 'http://localhost:8082';

export function setup() {
  const response = http.post(`${authUrl}/v1/auth/dev-token`);
  check(response, {
    'dev token issued': (res) => res.status === 200 && res.json('data'),
  });
  return { token: response.json('data') };
}

export default function (data) {
  const response = http.get(`${resourceUrl}/api/demo/me`, {
    headers: {
      Authorization: `Bearer ${data.token}`,
    },
  });

  check(response, {
    'resource response is 200': (res) => res.status === 200,
    'resource has user': (res) => res.json('userId') === 1,
  });
  sleep(1);
}
