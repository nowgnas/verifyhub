# Remove Admin API Scope

## Decision

휴대폰 본인인증은 고객이 인증을 시도한 시점에 처리되지 않으면 관리자 재처리로 복구하지 않는다. 고객은 필요한 경우 새 인증을 시작해야 하며, 기존 인증 건을 운영자가 재시도하는 API는 제공하지 않는다.

## Scope Change

- `VH-026. 관리자 재처리 API 구현`을 제거한다.
- `VH-027. 라우팅 정책 Admin API 구현`을 제거한다.
- `/admin/v1` API surface를 MVP 범위에서 제외한다.
- 다음 작업은 `VH-028. Internal Mock Provider API 구현`으로 진행한다.

## Rationale

본인인증 결과는 고객의 현재 인증 시도와 강하게 결합된다. 시점이 지난 인증을 운영자가 재처리하면 사용자 경험, 상태 해석, 감사 이력, provider transaction 상관관계가 불필요하게 복잡해진다. MVP에서는 실패/timeout/late callback을 관측하고 기록하되, 관리자 재처리나 정책 변경 API는 제공하지 않는다.
