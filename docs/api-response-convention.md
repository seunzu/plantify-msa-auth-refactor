# API 응답 컨벤션

## 성공 응답

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

---

## 실패 응답

```json
{
  "success": false,
  "code": "A001",
  "message": "유효하지 않은 토큰입니다.",
  "data": null
}
```

---

## 필드 설명

| 필드 | 의미 |
| --- | --- |
| `success` | 애플리케이션 수준에서 요청이 정상 처리되었는지 여부 |
| `code` | 클라이언트/서비스 분기에 사용할 안정적인 애플리케이션 코드 |
| `message` | 사람이 읽을 수 있는 메시지 |
| `data` | 성공 시 응답 본문. 실패 시 `null` |

---

## 에러 코드 접두사

| 접두사 | 범위 | 예시 |
| --- | --- | --- |
| `SUCCESS` | 성공 응답 | `SUCCESS` |
| `Cxxx` | 공통 요청/서버 오류 | `C002 BAD_REQUEST` |
| `Axxx` | 인증/토큰 오류 | `A001 INVALID_TOKEN` |
| `Uxxx` | 사용자 관련 오류 | `U001 USER_NOT_FOUND` |

---

## 예외 흐름

```text
Service 또는 JWT 컴포넌트
  -> ApplicationException(ErrorCode) 발생
  -> GlobalExceptionHandler
  -> ResponseEntity<ApiResponse.fail(...)>
```

공통 프레임워크 예외도 매핑됨

- 유효성 검사 실패
- 잘못된 형식의 JSON 본문
- 지원하지 않는 HTTP 메서드
- 요청 파라미터 누락
- 요청 헤더 누락
- 예상치 못한 서버 예외

---

## JWKS 예외

`/.well-known/jwks.json`은 OAuth2 Resource Server 라이브러리가 소비하는 표준 JSON 형식이기 때문에 사용하지 않음

예상 형식:

```json
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "...",
      "use": "sig",
      "alg": "RS256",
      "n": "...",
      "e": "..."
    }
  ]
}
```

---

## 현재 예시

성공:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": "eyJraWQiOi..."
}
```

실패:

```json
{
  "success": false,
  "code": "C002",
  "message": "Required request header 'Authorization' for method parameter type String is not present",
  "data": null
}
```
