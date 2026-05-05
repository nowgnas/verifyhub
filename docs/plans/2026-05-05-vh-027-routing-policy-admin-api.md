# VH-027 Routing Policy Admin API Plan

## Goal

운영자가 provider routing policy를 조회하고 새 version으로 교체할 수 있는 Admin API를 구현한다.

## Steps

1. Admin API integration test를 먼저 작성한다.
2. routing policy repository port에 최신 전체 정책 조회, 최신 version 조회, bulk save를 추가한다.
3. persistence adapter와 JPA repository query를 확장한다.
4. `RoutingPolicyAdminService`에서 provider set 검증과 next version insert를 처리한다.
5. `AdminRoutingPolicyController`와 request/response DTO를 추가한다.
6. `provider_routing_policy.version`의 JPA optimistic lock annotation을 제거한다.
7. `RetryNotAllowedException`과 관련 문서 항목을 제거한다.
8. focused test와 전체 테스트를 실행한다.

## Verification

- `./gradlew test --tests com.verifyhub.routing.adapter.in.web.AdminRoutingPolicyIntegrationTest --no-daemon`
- `./gradlew clean test --no-daemon`
