# Verifyhub Task Board

이 문서는 `docs/plans/2026-04-25-verifyhub-ticket-breakdown.md`의 진행 현황을 빠르게 관리하기 위한 체크리스트다.

## Status Legend

- `[x]` Done: 구현과 검증이 끝난 항목
- `[~]` Partial: 일부 산출물이 있지만 티켓 acceptance criteria 전체는 아직 미완료
- `[ ]` Todo: 아직 착수 전

## Current Snapshot

- Current milestone: Milestone 3. Application Services
- Next ticket: VH-013. Idempotency 단위/통합 테스트 작성
- Last verified command: `./gradlew clean test --no-daemon`
- Last verified result: `BUILD SUCCESSFUL`

## Milestone 0. Project Bootstrap

- [x] **VH-001. Gradle Spring Boot 프로젝트 초기화**
  - Done:
    - Java 17 toolchain
    - Spring Boot 2.7.18
    - Gradle wrapper
    - 핵심 의존성 추가
    - 기본 context load test
  - Verification:
    - `./gradlew clean test --no-daemon`

- [x] **VH-002. Hexagonal 패키지 구조 생성**
  - Done:
    - `common`
    - `verification`
    - `verification.adapter.*`
    - `verification.port.*`
    - `routing`
    - `idempotency`
    - `monitoring`
    - `config`
    - `mockprovider`
    - KG/NICE provider client 패키지 분리

- [x] **VH-003. 공통 인프라 구성**
  - Done:
    - `ErrorCode`
    - `VerifyhubException`
    - 주요 커스텀 예외 클래스
    - `ApiResponse`
    - `ErrorResponse`
    - `GlobalExceptionHandler`
    - `TimeProvider`
    - `SystemTimeProvider`
    - `VerificationIdGenerator`
    - `RequestIdGenerator`
  - Verification:
    - `VerifyhubExceptionTest`
    - `VerificationIdGeneratorTest`
    - `RequestIdGeneratorTest`

## Milestone 1. Domain Core

- [x] **VH-004. Verification 도메인 enum 및 모델 구현**
  - Done:
    - `VerificationStatus`
    - `VerificationEvent`
    - `VerificationPurpose`
    - `ProviderType`
    - `Verification`
    - `VerificationHistory`
    - `ProviderCallHistory`
    - `LateCallbackHistory`
    - `OutboxEvent`
    - 개인정보 필드 미포함 검증
  - Verification:
    - `./gradlew test --tests '*verification.domain*' --no-daemon`

- [x] **VH-005. VerificationStateMachine 구현**
  - Done:
    - `VerificationStateMachine.transit(current, event)` 구현
    - 허용/금지 전이 규칙 반영
    - terminal 상태 이후 모든 전이 거부
    - `Verification` aggregate 상태 변경 메서드에서 state machine 사용
  - Verification:
    - `VerificationStateMachineTest`

- [x] **VH-006. StateMachine 단위 테스트 작성**
  - Done:
    - 허용 전이 테스트
    - 금지 전이 테스트
    - terminal 상태 전이 거부 테스트
  - Verification:
    - `./gradlew test --tests com.verifyhub.verification.domain.VerificationStateMachineTest --no-daemon`

## Milestone 2. Persistence and Initial Schema

- [x] **VH-007. Flyway V1 초기 인증 스키마 작성**
  - Done:
    - `V1__create_verification_tables.sql`
    - `verification_request`
    - `verification_history`
    - `provider_call_history`
    - `late_callback_history`
    - `provider_routing_policy`
    - `outbox_event`
    - provider return/result correlation columns
    - MySQL JSON payload columns
    - unique keys and indexes
  - Verification:
    - `FlywayInitialSchemaTest`
- [x] **VH-008. Flyway V2 초기 라우팅 정책 데이터 작성**
  - Done:
    - `V2__insert_initial_provider_routing_policy.sql`
    - KG weight 10 enabled true version 1
    - NICE weight 90 enabled true version 1
  - Verification:
    - `FlywayInitialSchemaTest`
- [x] **VH-009. JPA Entity, Repository, Mapper 구현**
  - Done:
    - `VerificationEntity`
    - `ProviderRoutingPolicyEntity`
    - `VerificationJpaRepository`
    - `ProviderRoutingPolicyJpaRepository`
    - `VerificationPersistenceMapper` (MapStruct)
    - `ProviderRoutingPolicyPersistenceMapper` (MapStruct)
    - `VerificationPersistenceAdapter`
    - `ProviderRoutingPolicyPersistenceAdapter`
    - `VerificationRepositoryPort`
    - `ProviderRoutingPolicyRepositoryPort`
  - Verification:
    - `VerificationEntityMappingTest`
    - `ProviderRoutingPolicyEntityMappingTest`
    - `VerificationPersistenceAdapterIT`
    - `ProviderRoutingPolicyPersistenceAdapterIT`
    - `./gradlew clean test --no-daemon`

## Milestone 3. Application Services

- [x] **VH-010. VerificationStateService 구현**
  - Done:
    - `VerificationStateService` 구현
    - 상태 전이 API: `routeTo`, `startProviderCall`, `markSuccess`, `markFail`, `markTimeout`, `cancel`
    - 상태 전이 성공 시 `verification_request` 저장 후 `verification_history` 저장
    - 상태 전이 실패 시 예외 전파, history 미저장
  - Verification:
    - `VerificationStateServiceTest`
    - `./gradlew clean test --no-daemon`
- [x] **VH-011. History 및 Outbox 서비스 구현**
  - Done:
    - `VerificationHistoryService`
    - `ProviderCallHistoryService`
    - `LateCallbackHistoryService`
    - `OutboxEventService`
    - `ProviderCallHistoryRepositoryPort`
    - `LateCallbackHistoryRepositoryPort`
    - `OutboxEventPort`
    - `ProviderCallHistoryEntity`
    - `LateCallbackHistoryEntity`
    - `OutboxEventEntity`
    - `ProviderCallHistoryJpaRepository`
    - `LateCallbackHistoryJpaRepository`
    - `OutboxEventJpaRepository`
    - `ProviderCallHistoryPersistenceMapper`
    - `LateCallbackHistoryPersistenceMapper`
    - `OutboxEventPersistenceMapper`
    - `ProviderCallHistoryPersistenceAdapter`
    - `LateCallbackHistoryPersistenceAdapter`
    - `OutboxEventPersistenceAdapter`
  - Verification:
    - `VerificationHistoryServiceTest`
    - `ProviderCallHistoryServiceTest`
    - `LateCallbackHistoryServiceTest`
    - `OutboxEventServiceTest`
    - `./gradlew clean test --no-daemon`
- [x] **VH-012. IdempotencyService 구현**
  - Done:
    - `IdempotencyService.getOrCreate(...)`
    - `requestId + purpose + idempotencyKey` 기반 기존 verification 조회
    - 기존 verification이 없으면 creator 결과 저장
    - unique constraint 충돌 시 재조회 후 기존 verification 반환
    - requestId 기반 설계로 로그인 본인인증처럼 userId를 모르는 플로우 지원
  - Verification:
    - `IdempotencyServiceTest`
    - `RequestIdGeneratorTest`
    - `./gradlew clean test --no-daemon`
- [ ] **VH-013. Idempotency 단위/통합 테스트 작성**

## Milestone 4. Routing

- [ ] **VH-014. 라우팅 도메인 및 정책 조회 구현**
- [ ] **VH-015. WeightedProviderRoutingStrategy 구현**
- [ ] **VH-016. Routing 단위 테스트 작성**

## Milestone 5. Provider Integration and Resilience

- [ ] **VH-017. Provider Port 및 Provider HTTP Client 구현**
- [~] **VH-018. Resilience4j 설정 적용**
  - Partial:
    - `application.yml`에 `kgProvider`, `niceProvider` CircuitBreaker/TimeLimiter/Retry 기본 설정 추가
  - Remaining:
    - 실제 provider client와 resilience decorator 연결
    - retry 대상/비대상 정책 구현

- [ ] **VH-019. Provider 호출 흐름 구현**

## Milestone 6. Verification API

- [ ] **VH-020. 인증 요청 생성 API 구현**
- [ ] **VH-021. 인증 조회 및 이력 조회 API 구현**
- [ ] **VH-022. VerificationFlow 통합 테스트 작성**

## Milestone 7. Provider Return and Late Result

- [ ] **VH-023. Provider Return URL 및 Result Retrieval 구현**
- [ ] **VH-024. Late/Duplicate Return Result 정책 구현**
- [ ] **VH-025. Provider Return/Result 단위/통합 테스트 작성**

## Milestone 8. Admin and Operations

- [ ] **VH-026. 관리자 재처리 API 구현**
- [ ] **VH-027. 라우팅 정책 Admin API 구현**
- [~] **VH-028. Internal Mock Provider API 구현**
  - Partial:
    - `mockprovider` 패키지 골격 추가
    - KG/NICE provider client 패키지와 mock provider API 책임 분리 문서화
  - Remaining:
    - `/mock/providers/{provider}/verifications`
    - `/mock/providers/{provider}/returns`
    - `/mock/providers/{provider}/results`
    - `/mock/providers/{provider}/scenario`
    - scenario service 구현

- [~] **VH-029. Metrics 및 Actuator 구성**
  - Partial:
    - Actuator/Prometheus endpoint 노출 설정 추가
  - Remaining:
    - verification/provider metric 코드 구현

## Milestone 9. Resilience and End-to-End Tests

- [ ] **VH-030. TimeoutFlow 통합 테스트 작성**
- [ ] **VH-031. CircuitBreaker 통합 테스트 작성**
- [ ] **VH-032. 전체 테스트 안정화**

## Milestone 10. Local Runtime and Documentation

- [x] **VH-033. docker-compose 작성**
  - Done:
    - MySQL 8.0
    - Redis 7
    - Prometheus
    - Prometheus scrape target: `host.docker.internal:8080/actuator/prometheus`
  - Verification:
    - `docker compose config`

- [~] **VH-034. README 작성**
  - Partial:
    - 프로젝트 한 줄 설명과 현재 bootstrap 범위 작성
  - Remaining:
    - 전체 아키텍처
    - 상태 머신
    - API 예시
    - 운영 지표
    - Design Decisions

- [ ] **VH-035. Java 21 호환성 검증 스파이크**

## Post-MVP Extension Tickets

- [ ] **VH-101. Outbox Relay 구현**
- [ ] **VH-102. AI 운영 분석 기능 설계 및 구현**
- [ ] **VH-103. 관리자 대시보드 구현**

## How To Update

1. 티켓을 시작하면 `[ ]`를 `[~]`로 바꾼다.
2. acceptance criteria와 검증이 끝나면 `[x]`로 바꾼다.
3. `Current Snapshot`의 `Current milestone`, `Next ticket`, `Last verified command`, `Last verified result`를 갱신한다.
4. 세부 구현 계획은 `docs/plans/2026-04-25-verifyhub-ticket-breakdown.md`를 기준으로 유지한다.
