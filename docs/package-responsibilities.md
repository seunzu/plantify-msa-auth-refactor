# 패키지 책임

## auth-service

```text
com.plantify.auth
  client
  config
  controller
  domain
  global
  jwt
  repository
  service
```

### `client`

외부 API 클라이언트

- `KakaoApiClient`: 기존 OAuth 로그인 흐름에서 사용

로컬 실험 환경은 인증 성능 테스트 시 OAuth 노이즈를 제거하기 위해 주로 `/v1/auth/dev-token`을 사용

### `config`

애플리케이션 설정 및 로컬 실험 구성

- `ClientConfig`: HTTP 클라이언트 컴포넌트 생성
- `LocalDataInitializer`: `/v1/auth/dev-token`을 위한 로컬 사용자 시드
- `RsaKeyProvider`: RSA 키를 로드하거나 로컬 폴백 키 쌍 생성
- `SecurityConfig`: 로컬 실험 환경을 위한 auth 엔드포인트 허용
- `SwaggerConfig`: 기존 서비스의 OpenAPI 설정
- `WebConfig`: CORS 설정

### `controller`

HTTP 엔드포인트

- `AuthController`: 로그인, 리프레시, 레거시 검증, 로컬 토큰 발급, 사용자 조회
- `JwksController`: `/.well-known/jwks.json` 노출
- `HealthCheck`: 기존 서비스의 단순 헬스 엔드포인트

### `domain`

도메인 모델 및 응답 DTO

- `User`, `Role`
- `LoginResponse`, `UserResponse`, 카카오 응답 DTO

### `global.response`

공통 API 응답

- `ApiResponse<T>`: `success`, `code`, `message`, `data`

### `global.exception`

공통 예외 모델 및 핸들러

- `ErrorCode`: 공유 에러 코드 계약
- `ApplicationException`: `ErrorCode`를 담는 비즈니스 예외
- `GlobalExceptionHandler`: 예외를 `ApiResponse.fail`로 변환

### `global.exception.errorcode`

에러 코드 enum

- `CommonErrorCode`: 공통 요청/서버 오류, `Cxxx`
- `AuthErrorCode`: 인증/토큰 오류, `Axxx`
- `UserErrorCode`: 사용자 오류, `Uxxx`

### `jwt`

JWT 발급 및 토큰 파싱

- `JwtAuthProvider`: RS256으로 JWT를 서명하고 로컬 공개키를 사용하여 토큰 검증

### `repository`

영속성 경계

- `UserRepository`: 토큰 검증 및 로컬 실험 토큰 발급을 위한 사용자 조회

### `service`

애플리케이션 로직

- `AuthService`, `AuthServiceImpl`: 로그인, 리프레시, 검증, 사용자 조회, 로컬 실험 토큰 발급
- `JwtTokenService`, `JwtTokenServiceImpl`: `JwtAuthProvider`에 대한 JWT 추상화

---

## demo-resource-service

```text
com.plantify.demo
  controller
```

### `controller`

최소 보호 API

- `DemoController`: 인증된 principal과 권한을 검증하기 위해 `/api/demo/me` 노출

보안 설정은 `common-auth-lib`에서 가져옴

---

## common-auth-lib

```text
com.plantify.authlib
```

### `PlantifyAuthProperties`

리소스 서비스가 공유하는 설정 프로퍼티

```yaml
auth:
  jwk-set-uri: http://localhost:8081/.well-known/jwks.json
  permit-all:
    - /actuator/**
    - /health
```

### `PlantifyResourceServerAutoConfiguration`

리소스 서비스를 위한 Spring Boot 자동 설정

책임:

- `auth.jwk-set-uri`로 `JwtDecoder` 생성
- stateless Spring Security 설정
- 설정된 공개 엔드포인트 허용
- 나머지 모든 엔드포인트 보호
- JWT `role` 클레임을 `ROLE_*`으로 매핑

이를 통해 리소스 서비스가 동등한 `SecurityConfig` 또는 커스텀 `JwtFilter` 코드를 반복하지 않아도 됨

---

## experiments

트래픽 생성 스크립트

- `baseline-auth-call.js`: 베이스라인 부하 시나리오
- `auth-failure-window.js`: auth-service가 중단될 때의 동작을 비교하는 장애 윈도우 시나리오
