# API 명세

Swagger/OpenAPI를 통한 자동 생성 문서도 사용 가능

## auth-service

Base URL:

```text
http://localhost:8081
```

### POST `/v1/auth/dev-token`

사용자 `1`에 대한 로컬 실험용 액세스 토큰을 발급

목적:

- 부하 테스트에서 Kakao OAuth를 제거
- 보호된 API 테스트를 위한 유효한 JWT 생성

응답:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": "access-token"
}
```

### GET `/.well-known/jwks.json`

리소스 서비스를 위한 RSA 공개 키 세트를 노출

이 엔드포인트는 표준 JWKS JSON을 반환하며, `ApiResponse`를 사용하지 않음

응답:

```json
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "local-generated-...",
      "use": "sig",
      "alg": "RS256",
      "n": "...",
      "e": "..."
    }
  ]
}
```

### ~~POST `/v1/auth/validate-token`~~ (refactor에서 삭제됨)

베이스라인 아키텍처에서 리소스 서비스는 보호된 요청마다 이 엔드포인트를 호출하여 동기 DB 조회를 통해 토큰에서 `userId`와 `role`을 추출

`refactor/jwks-local-validation`에서는 이 엔드포인트가 삭제됨 

role은 이제 토큰 발급 시점에 JWT 클레임에 포함되며, 리소스 서비스는 JWKS를 사용하여 로컬에서 검증

### POST `/v1/auth/refresh`

제공된 토큰으로 새 액세스 토큰을 발급함

요청 헤더:

```text
Authorization: Bearer {refreshToken}
```

응답:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": "new-access-token"
}
```

### POST `/v1/auth/login`

카카오 OAuth 로그인 엔드포인트

요청 파라미터:

```text
code={authorizationCode}
```

응답:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 1,
    "username": "local-user",
    "accessToken": "...",
    "refreshToken": "..."
  }
}
```

### GET `/v1/auth/users/search`

username으로 사용자 id를 조회

요청 파라미터:

```text
username={username}
```

응답:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": 1
}
```

---

## demo-resource-service

Base URL:

```text
http://localhost:8082
```

### GET `/api/demo/me`

보호된 엔드포인트

리소스 서비스가 JWKS를 사용하여 JWT를 로컬에서 검증함

요청 헤더:

```text
Authorization: Bearer {accessToken}
```

응답:

```json
{
  "userId": 1,
  "authorities": [
    {
      "authority": "ROLE_USER"
    }
  ]
}
```

참고: 이 데모 엔드포인트는 현재 Spring Security의 인증된 principal을 직접 검증하기 위해 plain map을 반환함

### GET `/health`

단순 헬스 엔드포인트

응답:

```json
{
  "status": "ok"
}
```

### GET `/actuator/health`

Spring Boot Actuator 헬스 엔드포인트

### GET `/actuator/prometheus`

Prometheus 메트릭 엔드포인트
