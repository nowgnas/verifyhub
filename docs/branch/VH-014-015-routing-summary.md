# VH-014/VH-015 Routing Work Summary

## Scope

VH-014/VH-015는 verification 상태 전이가 아니라, 인증 요청을 처리할 외부 provider를 선택하는 provider selection routing 작업이다.

- VH-014: 라우팅 도메인 모델과 최신 정책 조회 기반 후보 구성
- VH-015: 후보 provider 중 weight 기반 최종 provider 선택

## VH-014. Routing Domain and Policy Lookup

추가한 도메인 모델:

- `ProviderHealthSnapshot`
  - provider별 사용 가능 여부를 표현한다.
  - circuit breaker OPEN, provider 장애 같은 상태를 routing 후보 제외 조건으로 전달할 수 있다.
- `RoutingDecision`
  - `selectedProvider`
  - `reason`
  - `policyVersion`
  - `candidateProviders`
- `RoutingReason`
  - `POLICY_LOADED`
  - `NO_ENABLED_POLICY`
  - `NO_AVAILABLE_PROVIDER`
  - `WEIGHTED_SELECTED`

추가한 application service:

- `ProviderRoutingService`
  - `ProviderRoutingPolicyRepositoryPort.findLatestEnabledPolicies()`로 최신 enabled 정책을 조회한다.
  - `ProviderHealthSnapshot` 기준으로 unavailable provider를 후보에서 제외한다.
  - 후보가 없으면 selected provider 없이 `NO_ENABLED_POLICY` 또는 `NO_AVAILABLE_PROVIDER` decision을 반환한다.

## VH-015. Weighted Provider Selection

추가한 전략:

- `WeightedProviderRoutingStrategy`
  - 후보 policy의 weight 합계를 계산한다.
  - `0 <= random < totalWeight` 값을 생성한다.
  - 누적 weight 구간에 따라 provider를 선택한다.
  - 후보가 없거나 total weight가 0 이하이면 `ProviderUnavailableException`을 던진다.

테스트 가능한 random source:

- `RandomBoundedNumberGenerator`
  - production 기본 구현은 `SecureRandom`을 사용한다.
  - 테스트에서는 deterministic generator를 주입해 KG/NICE 선택 구간을 고정 검증한다.

## Current Behavior

현재 `ProviderRoutingService.route(...)` 흐름:

1. 최신 enabled routing policy 조회
2. provider health snapshot으로 후보 필터링
3. 후보 policy를 `WeightedProviderRoutingStrategy`에 전달
4. weight 기반 selected provider가 포함된 `RoutingDecision` 반환

예시:

- KG weight 10
- NICE weight 90
- random ticket이 0-9이면 KG
- random ticket이 10-99이면 NICE

## Routing Policy Cache Policy

provider weight 정보는 DB를 원본으로 저장하고 Redis는 조회 성능을 위한 cache-aside 계층으로 사용한다.

- source of truth: `provider_routing_policy`
- cache key 예시: `verifyhub:routing-policy:latest`
- cache value: policy version, enabled provider 목록, weight
- policy 변경 순서:
  1. DB에 새 version 저장
  2. Redis cache 삭제 또는 새 값으로 교체
- Redis miss 또는 장애 시 DB에서 최신 policy를 조회한다.
- DB 조회까지 실패하면 stale cache로 routing하지 않고 provider unavailable로 fail closed 처리한다.

TTL 없는 Redis cache는 명시적 invalidation이 보장될 때만 허용한다. 운영 실수를 줄이려면 invalidation과 함께 30초 또는 60초 수준의 짧은 TTL을 안전장치로 두는 편이 낫다.

emergency stop은 최신 version에서 모든 provider를 disabled 처리하는 방식으로 표현한다. 따라서 최신 version은 enabled 여부와 무관하게 먼저 선택하고, 그 version 안에서 enabled provider만 routing 후보로 사용해야 한다. 최신 version이 모두 disabled이면 과거 enabled version으로 fallback하지 않는다.

현재 VH-014/VH-015 구현은 DB repository port 기반 routing까지만 포함한다. Redis cache adapter와 admin policy invalidation은 별도 infrastructure/admin 작업으로 분리한다.

## Tests

추가/갱신한 테스트:

- `ProviderRoutingServiceTest`
  - 최신 enabled policy 기반 routing decision 생성
  - enabled policy가 없을 때 `NO_ENABLED_POLICY`
  - unavailable provider 후보 제외
  - 모든 provider가 unavailable일 때 `NO_AVAILABLE_PROVIDER`
  - health snapshot이 없는 provider는 available로 간주
  - weighted strategy를 통한 selected provider 반환
- `WeightedProviderRoutingStrategyTest`
  - weight range에 따른 provider 선택
  - 낮은 weight provider도 random 구간에 따라 선택 가능
  - total weight가 0이면 `ProviderUnavailableException`
  - zero weight provider는 선택 구간에서 제외
  - 후보가 없으면 `ProviderUnavailableException`

## Verification

```bash
rtk ./gradlew test --tests com.verifyhub.routing.application.ProviderRoutingServiceTest --tests com.verifyhub.routing.domain.WeightedProviderRoutingStrategyTest --no-daemon
rtk ./gradlew clean test --no-daemon
```
