# VH-009 Branch Work Summary

## Branch

- Branch: `VH-009`
- Base progress: Milestone 2 persistence work에서 시작해 Milestone 3 application service 일부까지 진행
- Current next ticket: `VH-012. IdempotencyService 구현`

## Completed Tickets

### VH-009. JPA Entity, Repository, Mapper 구현

인증 요청과 라우팅 정책을 저장/조회할 수 있는 persistence 계층을 구현했다.

- `VerificationEntity`
- `ProviderRoutingPolicyEntity`
- `VerificationJpaRepository`
- `ProviderRoutingPolicyJpaRepository`
- `VerificationPersistenceMapper`
- `ProviderRoutingPolicyPersistenceMapper`
- `VerificationPersistenceAdapter`
- `ProviderRoutingPolicyPersistenceAdapter`
- `VerificationRepositoryPort`
- `ProviderRoutingPolicyRepositoryPort`

검증:

- `VerificationEntityMappingTest`
- `ProviderRoutingPolicyEntityMappingTest`
- `VerificationPersistenceAdapterIT`
- `ProviderRoutingPolicyPersistenceAdapterIT`

### VH-010. VerificationStateService 구현

상태 변경 책임을 application service로 모으고, 상태 전이 성공 시 인증 상태와 인증 이력이 함께 저장되도록 구성했다.

- `VerificationStateService`
- 상태 변경 메서드에 `@Transactional` 적용
- `routeTo`
- `startProviderCall`
- `markSuccess`
- `markFail`
- `markTimeout`
- `cancel`
- 실패한 상태 전이는 history를 저장하지 않도록 테스트로 고정
- `VerificationHistoryService`를 사용하도록 상태 이력 저장 책임 분리

검증:

- `VerificationStateServiceTest`

### VH-011. History 및 Outbox 서비스 구현

운영 추적과 후속 이벤트 발행을 위해 history/outbox 저장 경로를 구현했다.

History 구분:

- `verification_history`: 인증 상태 전이 이력
- `provider_call_history`: 외부 provider 호출 요청/응답/latency/retry 이력
- `late_callback_history`: terminal 상태 이후 늦게 도착한 callback/result 이력

Application services:

- `VerificationHistoryService`
- `ProviderCallHistoryService`
- `LateCallbackHistoryService`
- `OutboxEventService`

Outbound ports:

- `VerificationHistoryRepositoryPort`
- `ProviderCallHistoryRepositoryPort`
- `LateCallbackHistoryRepositoryPort`
- `OutboxEventPort`

Persistence adapters:

- `VerificationHistoryPersistenceAdapter`
- `ProviderCallHistoryPersistenceAdapter`
- `LateCallbackHistoryPersistenceAdapter`
- `OutboxEventPersistenceAdapter`

Entities:

- `VerificationHistoryEntity`
- `ProviderCallHistoryEntity`
- `LateCallbackHistoryEntity`
- `OutboxEventEntity`

Repositories:

- `VerificationHistoryJpaRepository`
- `ProviderCallHistoryJpaRepository`
- `LateCallbackHistoryJpaRepository`
- `OutboxEventJpaRepository`

Mappers:

- `VerificationHistoryPersistenceMapper`
- `ProviderCallHistoryPersistenceMapper`
- `LateCallbackHistoryPersistenceMapper`
- `OutboxEventPersistenceMapper`

검증:

- `VerificationHistoryServiceTest`
- `ProviderCallHistoryServiceTest`
- `LateCallbackHistoryServiceTest`
- `OutboxEventServiceTest`

## Outbox Role

현재 outbox는 외부 브로커로 직접 발행하는 역할이 아니라, 메인 트랜잭션 안에서 발행해야 할 이벤트를 `PENDING` 상태로 저장하는 역할이다.

정합성을 위해 상태/이력 저장과 `outbox_event` insert는 같은 트랜잭션에서 처리한다. 실제 Kafka/SQS 등 외부 발행은 이후 relay가 `PENDING` 이벤트를 읽어 비동기로 처리하는 구조로 확장한다.

## Package Cleanup

실제 클래스가 생성된 패키지의 `package-info.java`를 제거했다. 빈 패키지 경계 표시용 파일은 클래스가 생긴 뒤에는 유지하지 않는 방향으로 정리했다.

## Verification

최종 확인 명령:

```bash
rtk ./gradlew clean test --no-daemon
```

결과:

- `BUILD SUCCESSFUL`

