# 검증과 테스트

이 문서는 인증 구조 변경에서 확인해야 할 검증 관점, k6 시나리오, 저장소에 기록한 부하 수치와 해석을 정리

두 브랜치의 현재 k6 요청 구성은 동일하지 않음. `main`은 k6가 `/validate-token`을 직접 측정하는 요청을 추가로 보내지만 refactor는 보호 API만 호출. 따라서 전체 `http_req_duration`, `http_reqs`, 요청 속도를 그대로 성능 개선율로 해석하지 않음

## 환경

- Auth 서버: `auth-service`
- 리소스 서버: `demo-resource-service`
- 부하 도구: k6 v0.53.0
- 실행 환경: MacBook, Docker Compose
- VU: 20
- 기본 부하 지속 시간: 1분
- 실험용 토큰 발급: `POST /v1/auth/dev-token`
- 보호 API: `GET /api/demo/me`

## 검증 관점

- 보호 요청마다 발생하던 auth-service 동기 검증 호출이 제거되는가
- JWT의 `role` 클레임이 리소스 서비스 권한으로 매핑되는가
- JWKS 캐시 이후 auth-service가 중단되어도 보호 API가 계속 처리되는가
- 측정 조건이 다른 수치를 성능 개선율처럼 과장하지 않는가

## 시나리오 A: 기본 부하

```bash
docker compose --profile test run --rm k6 run /scripts/baseline-auth-call.js
```

### main 결과

| 메트릭 | 값 |
| --- | ---: |
| 실행 시간 | 1분 |
| 전체 요청 수 (`/api/demo/me` + `/validate-token`) | 2,321 |
| 요청 속도 | 38.36 req/s |
| 실패율 | 0.00% |
| 반복 횟수 (`iterations`) | 1,160 |
| 평균 레이턴시 (`http_req_duration`) | 20.02 ms |
| p90 레이턴시 | 23.95 ms |
| p95 레이턴시 | 52.30 ms |
| auth_call_duration 평균 | 16.11 ms |
| auth_call_duration p95 | 40.02 ms |

`http_reqs: 2,321`은 `/api/demo/me` 1,160회, k6가 직접 호출한 `/validate-token` 1,160회, setup 요청 1회를 합친 값

`/api/demo/me` 내부에서 발생한 demo 서버의 `/validate-token` 호출은 k6의 `http_reqs`에 포함되지 않음

### refactor 결과

| 메트릭 | 값 |
| --- | ---: |
| 실행 시간 | 1분 |
| 전체 요청 수 | 1,201 |
| 요청 속도 | 19.55 req/s |
| 실패율 | 0.00% |
| 반복 횟수 (`iterations`) | 1,200 |
| 평균 레이턴시 (`http_req_duration`) | 14.55 ms |
| p90 레이턴시 | 15.05 ms |
| p95 레이턴시 | 17.77 ms |

`http_reqs: 1,201`은 `/api/demo/me` 1,200회와 setup 토큰 발급 1회를 합친 값

refactor에서는 매 반복마다 `/validate-token`을 호출하지 않으므로 `iterations`와 `http_reqs`가 거의 같음

## 시나리오 B: Refactor 장애 윈도우

```bash
docker compose --profile test run --rm k6 run /scripts/auth-failure-window.js
docker compose stop auth-service
```

JWKS 캐시 워밍 업 이후 auth-service가 중단되어도 보호 API가 계속 처리되는지 확인

현재 스크립트는 auth-service 중단과 JWKS 캐시 워밍 시점을 자동 기록하지 않으므로 첫 보호 요청 성공을 확인한 뒤 별도 터미널에서 auth-service를 수동으로 중단해야 함

| 메트릭 | 값 |
| --- | ---: |
| 실행 시간 | 2분 |
| 전체 요청 수 | 2,381 |
| 요청 속도 | 19.69 req/s |
| 실패율 | 0.00% |
| 반복 횟수 (`iterations`) | 2,380 |
| 평균 레이턴시 (`http_req_duration`) | 10.82 ms |
| p90 레이턴시 | 14.03 ms |
| p95 레이턴시 | 17.54 ms |
| checks | 100.00% |

## 해석

- refactor에서는 JWKS 캐시 후 보호 요청이 동기 auth-service 검증 없이 처리됨
- auth-service가 중단되어도 이미 캐시된 공개키와 유효한 Access Token이 있으면 보호 API는 계속 성공
- 로그인, 리프레시, 최초 JWKS 조회, 키 교체, 토큰 블랙리스트 조회까지 auth-service 의존성이 사라진 것은 아님
- 레이턴시 개선율을 주장하려면 두 브랜치 모두 k6가 `/api/demo/me`만 호출하도록 요청 구성을 맞추고 동일 환경에서 다시 측정해야 함
