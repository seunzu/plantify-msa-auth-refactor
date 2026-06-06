# 아키텍처 개요

## 목표

기존 구조는 보호된 리소스 요청마다 `auth-service`를 호출하여 JWT를 검증. 이 실험 환경은 그 베이스라인과 JWKS 기반 로컬 검증을 비교

---

## 현재 브랜치

`refactor/jwks-local-validation`은 리팩토링된 구조를 구현:

```text
Client
  -> demo-resource-service
      -> 캐시된 JWKS로 JWT 로컬 검증

auth-service
  -> RS256 JWT 발급
  -> /.well-known/jwks.json 노출
```

---

## 저장소 구조

```text
plantify-msa-auth-refactor
  auth-service
    src/main/java/com/plantify/auth
      config
      controller
      domain
      global
      jwt
      repository
      service
    src/main/resources
    build.gradle
    Dockerfile

  demo-resource-service
    src/main/java/com/plantify/demo
      config
      controller
    src/main/resources
    build.gradle
    Dockerfile

  common-auth-lib
    src/main/java/com/plantify/authlib
      PlantifyAuthProperties.java
      PlantifyResourceServerAutoConfiguration.java

  experiments
    k6

  docs

  docker-compose.yml
  .github/workflows/ci.yml
```

---

## 서비스 역할

| 컴포넌트 | 역할 |
| --- | --- |
| `auth-service` | JWT 발급, 베이스라인 비교용 레거시 토큰 검증, JWKS 노출 |
| `common-auth-lib` | 공유 Resource Server 보안 설정, JWKS 디코더 설정, JWT role 매핑 제공 |
| `demo-resource-service` | 아이템/도메인 복잡성 없이 인증 동작을 검증하기 위한 최소 보호 API |
| `experiments/k6` | 부하 및 장애 윈도우 트래픽 생성 |
| `docs` | 아키텍처, API 계약, 응답 컨벤션, 시퀀스 다이어그램, 테스트 결과 문서화 |
| `docker-compose.yml` | 로컬 실험 환경 실행 |
| `.github/workflows/ci.yml` | CI 전용: 빌드, 테스트, Docker 이미지 빌드 확인 |

---

## 데모 리소스 서비스를 사용하는 이유

`demo-resource-service`는 테스트 범위를 집중시킴:

- JWT 검증 동작
- auth-service 의존성
- auth-service 중단 시 장애 동작
- 로컬 검증 vs 네트워크 검증의 레이턴시 영향

---

## common-auth-lib 위치

`common-auth-lib`은 리소스 서비스에서 반복되는 보안 설정을 제거

커스텀 `JwtFilter`를 구현하지 않고 Spring Security OAuth2 Resource Server를 설정:

- `auth.jwk-set-uri` 읽기
- `JwtDecoder` 생성
- JWT `role` 클레임을 `ROLE_*`으로 매핑
- 기본 stateless `SecurityFilterChain` 제공
- 기본적으로 health/actuator 엔드포인트 허용

리소스 서비스는 다음을 추가하여 재사용할 수 있음:

```gradle
implementation 'com.plantify:common-auth-lib'
```

이 로컬 저장소에서 `demo-resource-service`는 Gradle composite build를 사용:

```gradle
includeBuild('../common-auth-lib')
```

---

## Gateway

```text
Auth 네트워크 검증 -> JWKS 로컬 검증
```

Gateway는 라우팅, 레이트 리미팅, 로깅, 엣지 정책을 위해 나중에 추가할 수 있음

---

## 키 관리

로컬 실험에서 `auth-service`는 PEM 파일이 제공되지 않으면 인메모리 RSA 키 쌍을 생성할 수 있음

프로덕션 키는 안정적이어야 하며 다음 중 하나를 통해 외부에서 관리해야 함:

- Kubernetes Secret
- AWS KMS 또는 Secrets Manager
- Vault
- 기타 시크릿 매니저

오래된 key id로 새 서명 키를 제공하는 것을 방지하기 위해 로컬 폴백 키가 변경되면 `auth-service`도 `kid`를 변경 
