# plantify-msa-auth-refactor

Spring Boot MSA 환경에서 인증 구조를 비교하는 로컬 실험 레포

- Baseline (`main`): 리소스 서비스가 보호 요청마다 `auth-service /v1/auth/validate-token`을 호출
- Refactor target (`refactor/jwks-local-validation`): 리소스 서비스가 JWKS로 JWT를 로컬 검증

## 핵심 변경

- 보호 API 요청마다 발생하던 auth-service 네트워크 호출 제거
- 리소스 서비스가 JWKS를 캐싱하고 JWT 서명을 로컬에서 검증
- auth-service 장애 시에도 캐시된 공개키로 일정 시간 인증 처리 가능
- 공통 인증 설정을 `common-auth-lib`로 분리

## 서비스

| 서비스 | 역할 |
| --- | --- |
| `auth-service` | 로그인, 실험용 토큰 발급, JWKS 노출 |
| `common-auth-lib` | 리소스 서비스용 Spring Security Resource Server 자동 설정 |
| `demo-resource-service` | 인증 구조 비교를 위한 최소 보호 API |
| `experiments/k6` | 부하 테스트와 장애 윈도우 테스트 스크립트 |

## 로컬 실행

```bash
docker compose up --build auth-service demo-resource-service
```

실험용 토큰 발급:

```bash
curl -X POST http://localhost:8081/v1/auth/dev-token
```

보호 API 호출:

```bash
curl http://localhost:8082/api/demo/me \
  -H "Authorization: Bearer {accessToken}"
```

## 실험 실행

기본 부하 테스트:

```bash
docker compose --profile test run --rm k6 run /scripts/baseline-auth-call.js
```

JWKS 캐시 후 auth-service 중단 테스트:

```bash
docker compose --profile test run --rm k6 run /scripts/auth-failure-window.js
docker compose stop auth-service
```

## 문서

- [Architecture](docs/architecture.md)
- [Validation and Tests](docs/validation-and-tests.md)
