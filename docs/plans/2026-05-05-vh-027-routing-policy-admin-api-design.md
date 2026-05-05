# VH-027 Routing Policy Admin API Design

## Context

Provider routing policy는 인증 생성 시 provider 선택에 사용된다. 이미 생성된 verification에는 선택 당시 `routingPolicyVersion`이 저장되므로, 운영 중 정책 변경은 기존 row update가 아니라 새 version insert로 처리해야 한다.

## API

### GET /admin/v1/routing-policies

최신 routing policy version의 전체 provider 정책을 조회한다. disabled provider도 운영자가 볼 수 있어야 하므로 enabled 정책만 반환하지 않는다.

Response:

```json
{
  "data": {
    "version": 1,
    "policies": [
      { "provider": "KG", "weight": 10, "enabled": true },
      { "provider": "NICE", "weight": 90, "enabled": true }
    ]
  }
}
```

### PUT /admin/v1/routing-policies

전체 provider 정책 세트를 받아 `latestVersion + 1`로 새 row들을 insert한다.

Request:

```json
{
  "policies": [
    { "provider": "KG", "weight": 0, "enabled": false },
    { "provider": "NICE", "weight": 100, "enabled": true }
  ]
}
```

## Rules

- 요청은 모든 `ProviderType`을 정확히 한 번씩 포함해야 한다.
- `weight`는 0 이상이어야 한다.
- `enabled=true`인 policy의 `weight`는 1 이상이어야 한다.
- 기존 version row는 수정하지 않는다.
- 모든 provider가 disabled인 emergency stop 정책은 허용한다. 이 경우 routing은 `NO_ENABLED_POLICY`로 이어진다.

## Persistence

- `ProviderRoutingPolicyRepositoryPort.findLatestPolicies()`는 최신 version의 전체 정책을 반환한다.
- `findLatestEnabledPolicies()`는 기존 routing flow에서 계속 사용하며 최신 version 중 enabled policy만 반환한다.
- JPA entity의 `version` 필드는 optimistic lock이 아니라 business policy version이므로 `@Version`을 사용하지 않는다.

## Verification

- Admin API integration test로 GET, PUT, incomplete provider set rejection을 검증한다.
- PUT 이후 version 1 row가 유지되고 version 2 row가 추가되는지 DB로 검증한다.
