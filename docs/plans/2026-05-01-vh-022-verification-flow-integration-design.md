# VH-022 VerificationFlow 통합 테스트 설계

## 목적

`VH-020` 생성 API와 `VH-021` 조회 API를 하나의 통합 흐름으로 검증한다. 인증 요청 생성부터 provider 선택, 상태 저장, history 저장, provider call history 저장, 상태 조회, 이력 조회까지 실제 Spring context와 MySQL schema 위에서 확인한다.

## 접근

`@SpringBootTest` + `MockMvc` + Testcontainers MySQL 기반 통합 테스트를 작성한다. 실제 외부 provider 네트워크 호출은 사용하지 않고, `ProviderClientResilienceDecorator`를 `@MockBean`으로 대체해 deterministic한 `ProviderRequestResult.ACCEPTED`를 반환한다.

## 검증 범위

- `POST /api/v1/verifications`
  - `Idempotency-Key` header 포함
  - `requestId`, `purpose`, `returnUrl`, `closeUrl`, `svcTypes` body 포함
- 생성 응답
  - `verificationId`
  - `status=IN_PROGRESS`
  - `provider`
  - `authEntry`
- DB 상태
  - `verification_request` row가 `IN_PROGRESS`로 저장된다.
  - `verification_history`에 `ROUTE_SELECTED`, `PROVIDER_CALL_STARTED` 이력이 저장된다.
  - `provider_call_history`가 1건 저장된다.
- 조회 API
  - `GET /api/v1/verifications/{verificationId}`
  - `GET /api/v1/verifications/{verificationId}/histories`
- idempotency replay
  - 같은 `requestId + purpose + Idempotency-Key` 요청은 같은 `verificationId`를 반환한다.
  - 이미 `IN_PROGRESS`인 기존 인증 요청은 provider flow를 다시 실행하지 않는다.
  - provider call history가 중복 저장되지 않는다.

## 필요한 보정

현재 `VerificationCreateService`는 idempotency로 기존 verification이 반환되어도 provider flow를 다시 호출할 수 있다. 통합 테스트에서 이 문제를 먼저 드러내고, 기존 verification이 `REQUESTED`가 아닌 경우에는 provider flow를 재실행하지 않도록 보정한다.

기존 요청 재응답의 `authEntry`는 DB에 저장되어 있지 않으므로 replay 응답에서는 `null`일 수 있다. 후속으로 auth entry/session 저장 정책이 생기면 같은 entry를 재반환하도록 확장한다.
