# VH-024 Late/Duplicate Return Result 정책 설계

## 목적

provider return/result가 이미 terminal 상태인 verification에 도착해도 기존 상태를 오염시키지 않는다. 늦게 도착한 callback과 중복 callback은 `late_callback_history`에 audit trail로 남긴다.

## 정책

`ProviderReturnService`에서 verification을 조회한 뒤 provider path 검증을 먼저 수행한다.

- `IN_PROGRESS`
  - 기존 VH-023 흐름을 유지한다.
  - provider result를 조회하고 `SUCCESS` 또는 `FAIL` 상태 전이를 수행한다.
  - outbox event를 저장한다.
- terminal 상태
  - 상태 전이를 수행하지 않는다.
  - outbox event를 저장하지 않는다.
  - provider result를 조회한 뒤 `late_callback_history`에 저장한다.
  - 저장된 `webTransactionId`와 incoming `webTransactionId`가 같으면 `duplicate=true`로 기록한다.
  - 저장된 `webTransactionId`와 incoming `webTransactionId`가 다르면 `duplicate=false`로 기록한다.

## 범위 제한

- optimistic lock 충돌 후 재조회 정책은 후속 고도화로 둔다.
- webTransactionId 외 providerTransactionId 기반 중복 판정은 provider session 저장 정책이 생긴 뒤 확장한다.
- invalid signature/integrity failure 세부 보안 이벤트 분리는 VH-025 이후 테스트와 함께 확장한다.

## 테스트

- `ProviderReturnServiceTest`
  - `TIMEOUT` verification에 `SUCCESS` result가 도착해도 상태는 `TIMEOUT`으로 유지되고 late history가 저장된다.
  - 이미 `SUCCESS` verification에 같은 `webTransactionId`가 다시 오면 `duplicate=true`로 late history가 저장된다.
  - terminal callback 처리 시 markSuccess/markFail/outbox는 호출되지 않는다.
