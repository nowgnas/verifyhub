# VH-025 Provider Return/Result Test Design

## Context

VH-023에서 provider return/result 처리 API와 상태 전이를 구현했고, VH-024에서 terminal 상태 이후 late/duplicate callback 정책을 추가했다. VH-025의 목표는 이 정책들이 단위 테스트 수준을 넘어 실제 API, persistence, history, outbox 저장 흐름에서 깨지지 않는지 검증하는 것이다.

## Scope

- `GET/POST /api/v1/providers/{provider}/returns`가 같은 application service 경로를 사용한다는 전제를 유지한다.
- provider result 조회는 실제 외부 provider 대신 `ProviderClientResilienceDecorator`를 mock 처리한다.
- DB 검증은 Testcontainers MySQL과 Flyway schema를 사용한다.
- 검증 대상은 `verification_request`, `verification_history`, `outbox_event`, `late_callback_history`다.

## Test Cases

1. Provider result success
   - 인증 생성 후 provider return을 호출한다.
   - `IN_PROGRESS -> SUCCESS` 전이를 확인한다.
   - `CALLBACK_SUCCESS`, `PROVIDER_CALL_SUCCEEDED`, `VERIFICATION_SUCCEEDED` outbox 저장을 확인한다.

2. Integrity verification failure
   - provider result는 `SUCCESS`지만 `integrityVerified=false`로 응답한다.
   - 최종 상태가 `FAIL`인지 확인한다.
   - `CALLBACK_FAIL`, `PROVIDER_CALL_FAILED`, `VERIFICATION_FAILED` outbox 저장을 확인한다.

3. Duplicate terminal return
   - 정상 success callback으로 terminal 상태를 만든 뒤 동일 `webTransactionId`로 return을 재호출한다.
   - 상태가 `SUCCESS`로 유지되는지 확인한다.
   - `late_callback_history.duplicate=true`로 저장되는지 확인한다.
   - terminal duplicate 처리에서 outbox가 중복 저장되지 않는지 확인한다.

## Non-Goals

- provider client HTTP parsing 자체는 기존 KG/NICE client 테스트 범위로 둔다.
- admin 재처리 API와 수동 result polling은 VH-026에서 다룬다.
- 실제 mock provider scenario 화면은 후속 프론트 프로젝트 작업에서 다룬다.
