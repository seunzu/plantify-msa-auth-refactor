# 부하 테스트 보고서

두 가지 수준의 검증을 분리

- 로컬에서 이미 수행된 스모크/장애 윈도우 검증
- 동일한 시나리오로 두 브랜치를 실행하여 수집한 전체 k6 부하 수치

테스트 환경: MacBook, Docker Compose, k6 v0.53.0, 20 VU, 1분

## 환경

- Auth 서버: `auth-service`
- 리소스 서버: `demo-resource-service`
- 부하 도구: k6
- 로컬 오케스트레이션: Docker Compose
- 실험용 토큰 발급 엔드포인트: `POST /v1/auth/dev-token`
- 보호된 엔드포인트: `GET /api/demo/me`

---

## 시나리오 A: 베이스라인 부하

스크립트:

```bash
docker compose --profile test run --rm k6 run /scripts/baseline-auth-call.js
```

기본 k6 옵션:

- VU: 20
- 지속 시간: 1분
- 실패 임계값: `http_req_failed < 5%`
- 레이턴시 임계값: `p95 < 500ms`

### 베이스라인 결과 (main 브랜치 — 요청마다 `/validate-token` 호출)

| 메트릭 | 값 |
| --- | ---: |
| 전체 요청 수 (`/api/demo/me` + `/validate-token`) | 2,361 |
| 요청 속도 | 39.0 req/s |
| 실패율 | 0.00% |
| 평균 레이턴시 (`http_req_duration`) | 11.77 ms |
| p90 레이턴시 | 15.54 ms |
| p95 레이턴시 | 20.01 ms |
| auth_call_duration 평균 (`/validate-token` 직접 측정) | 8.07 ms |
| auth_call_duration p95 | 15.07 ms |

> `http_reqs: 2,361` = `/api/demo/me` 1,180회 + `/validate-token` 직접 측정 1,180회 + setup 1회
> 실제 서비스 구조에서 `/api/demo/me` 1건당 내부적으로 `/validate-token` 1번 추가 발생

---

## 시나리오 B: Auth 장애 윈도우

스크립트:

```bash
docker compose --profile test run --rm k6 run /scripts/auth-failure-window.js
docker compose stop auth-service
```

### 베이스라인 예상 결과

| 관찰 항목 | 베이스라인 예상 동작 |
| --- | --- |
| auth-service 중단 전 | 보호된 요청이 200 반환 |
| auth-service 중단 후 | 보호된 요청 실패 |
| 주요 실패 상태 | 리소스 서비스에서 403 |
| 이유 | `/v1/auth/validate-token`이 불가하므로 리소스 서비스가 인증 설정 불가 |

---

## 리팩토링 비교

| 메트릭 | main (validate-token) | refactor (JWKS 로컬 검증) | 변화 |
| --- | ---: | ---: | ---: |
| `/api/demo/me` 반복 횟수 | 1,180 | 1,200 | +20 |
| 요청 속도 (전체) | 39.0 req/s | 19.6 req/s | — ※ |
| 실패율 | 0.00% | 0.00% | — |
| 평균 레이턴시 | 11.77 ms | 12.85 ms | — ※ |
| p90 레이턴시 | 15.54 ms | 13.94 ms | **-10%** |
| p95 레이턴시 | 20.01 ms | 16.43 ms | **-18%** |
| 보호된 요청당 auth-service 호출 수 | 2회 (me + validate) | 1회 (me만) | **-50%** |
| auth_call_duration p95 | 15.07 ms | N/A (로컬 검증) | 제거 |

> ※ main의 `http_reqs`가 높은 이유: k6에서 `/validate-token`을 직접 별도 호출하므로 요청 수가 2배.
> 실제 서비스 부하 비교는 auth-service가 받는 요청 수 기준이 의미있음.
> auth-service 관점: main은 `1,180 /api/demo/me` + `1,180 /validate-token` = 2,360회 → refactor는 `1,200 /api/demo/me`만 (JWKS는 캐싱으로 1회) = **인증 관련 auth-service 부하 약 50% 감소**

---

## 리팩토링 스모크 테스트

`refactor/jwks-local-validation`에서 수동 검증:

| 확인 항목 | 결과 |
| --- | --- |
| `POST /v1/auth/dev-token`이 RS256 JWT 발급 | 통과 |
| `GET /.well-known/jwks.json`이 `kid`와 함께 RSA 공개키 노출 | 통과 |
| `GET /api/demo/me` with Bearer token이 보호된 사용자 정보 반환 | 통과, `200` |
| auth-service 중단 후 JWKS 캐시 워밍 업 상태에서 보호된 API | 통과, `200` |
| `demo-resource-service`가 `common-auth-lib` composite build 사용 | 통과 |

이는 전체 k6 비교 실행 전 아키텍처 동작을 확인: JWKS가 캐시된 후, 보호된 API 요청은 더 이상 동기 `/v1/auth/validate-token` 호출에 의존하지 않음

로컬 폴백 RSA 키가 재생성되면 `auth-service`도 새 `kid`를 생성. 변경된 서명 키가 동일한 `kid`를 사용하면 리소스 서비스가 오래된 캐시된 공개키를 계속 사용할 수 있기 때문에 중요

---

## 빌드 검증

| 프로젝트 | 결과 |
| --- | --- |
| `common-auth-lib` 테스트 | 통과 |
| `auth-service` 테스트 | 통과 |
| `demo-resource-service` 테스트 | 통과 |

---

## 참고

- Prometheus/Grafana는 나중에 JVM, HTTP, 컨테이너 메트릭 시각화를 위해 추가할 수 있음
- k6는 트래픽 생성을 담당합니다. Prometheus/Grafana는 시스템 관찰을 위한 것
- 첫 번째 목표는 아키텍처 차이를 증명하는 것이며, 랩톱에서 프로덕션 용량 수치를 주장하는 것이 아님
