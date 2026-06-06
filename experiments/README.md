# Experiments

## k6 부하 테스트

### baseline-auth-call.js
JWKS 로컬 검증 방식의 기본 부하 테스트.
매 요청마다 Auth 서버 호출 없이 각 서비스에서 직접 JWT 검증.

```bash
docker compose --profile test run k6 run /scripts/baseline-auth-call.js
```

### auth-failure-window.js
Auth 서버 다운 시 JWKS 캐시로 인증이 계속 동작하는지 검증.
테스트 도중 auth-service를 중단시켜 동작 확인.

```bash
docker compose --profile test run k6 run /scripts/auth-failure-window.js
```
