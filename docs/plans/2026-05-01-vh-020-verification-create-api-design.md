# VH-020 인증 요청 생성 API 설계

## 목적

`POST /api/v1/verifications`를 구현해 인증 요청 생성부터 provider 초기화까지 연결한다. API는 `Idempotency-Key`를 필수로 받고, 같은 `requestId + purpose + idempotencyKey` 요청은 기존 인증 요청을 재사용한다.

## 권장 접근

`VerificationCreateService`를 application layer에 추가한다. Controller는 요청 validation, `Idempotency-Key` header 확인, 응답 DTO 변환만 담당한다. 생성 서비스는 다음 책임을 가진다.

- `VerificationIdGenerator`와 `TimeProvider`로 `Verification.requested(...)` 생성
- `IdempotencyService.getOrCreate(...)`로 중복 요청 방지
- 새로 만들었거나 기존 조회한 `Verification`을 `ProviderVerificationFlowService`에 전달
- provider flow 결과를 API 응답 모델로 반환

## API

### Request

`POST /api/v1/verifications`

Headers:

- `Idempotency-Key`: 필수

Body:

```json
{
  "requestId": "req_123",
  "purpose": "LOGIN",
  "returnUrl": "https://client.example/verification/return",
  "closeUrl": "https://client.example/verification/close",
  "svcTypes": ["M"]
}
```

`returnUrl`과 `svcTypes`는 필수다. `closeUrl`은 provider별로 선택값일 수 있으므로 DTO에서는 선택값으로 받고, application command까지 그대로 전달한다.

### Response

```json
{
  "data": {
    "verificationId": "verif_...",
    "status": "IN_PROGRESS",
    "provider": "NICE",
    "authEntry": {
      "provider": "NICE",
      "type": "REDIRECT_URL",
      "url": "https://nice.example/auth",
      "method": "GET",
      "charset": "UTF-8",
      "fields": {}
    }
  }
}
```

## 오류 처리

- `Idempotency-Key`가 비어 있으면 `INVALID_REQUEST`로 응답한다.
- 요청 body validation 실패는 기존 `GlobalExceptionHandler`의 `MethodArgumentNotValidException` 처리에 맡긴다.
- provider 선택 실패, provider timeout/fail 등 기존 `VerifyhubException` 계열은 그대로 전파한다.

## 테스트

- `VerificationCreateServiceTest`
  - 신규 요청이면 verification을 생성하고 provider flow를 호출한다.
  - 같은 idempotency key로 기존 verification이 조회되면 기존 verification으로 provider flow를 호출한다.
- `VerificationCreateControllerTest`
  - 정상 요청은 `verificationId`, `status`, `provider`, `authEntry`를 반환한다.
  - `Idempotency-Key` 누락/blank는 400을 반환한다.
  - request validation 실패는 400을 반환한다.
- 전체 검증: `./gradlew clean test --no-daemon`
