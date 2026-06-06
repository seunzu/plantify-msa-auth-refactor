import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    auth_failure_window: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 20),
      duration: __ENV.DURATION || '2m',
    },
  },
};

const authUrl = __ENV.AUTH_URL || 'http://localhost:8081';
const resourceUrl = __ENV.RESOURCE_URL || 'http://localhost:8082';

export function setup() {
  const response = http.post(`${authUrl}/v1/auth/dev-token`);
  check(response, {
    'dev token issued before failure test': (res) => res.status === 200 && res.json('data'),
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
    'resource response is 200 with cached JWKS': (res) => res.status === 200,
  });
  sleep(1);
}
