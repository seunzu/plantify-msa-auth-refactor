import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// auth_call_duration: /validate-token 직접 호출 latency
// main 브랜치에서는 /api/demo/me 요청마다 이 비용이 내부적으로 발생함
// refactor 브랜치(JWKS 로컬 검증)와 비교 시 이 overhead가 사라짐
const authCallDuration = new Trend('auth_call_duration', true);

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
    auth_call_duration: ['p(95)<200'],
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
  // auth 서버 직접 호출 latency를 별도 메트릭으로 측정
  const authRes = http.post(`${authUrl}/v1/auth/validate-token`, null, {
    headers: { Authorization: `Bearer ${data.token}` },
  });
  authCallDuration.add(authRes.timings.duration);

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
