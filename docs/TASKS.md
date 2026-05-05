# Verifyhub Task Board

이 문서는 `docs/plans/2026-04-25-verifyhub-ticket-breakdown.md`의 진행 현황을 빠르게 관리하기 위한 체크리스트다.

## Status Legend

- `[x]` Done: 구현과 검증이 끝난 항목
- `[~]` Partial: 일부 산출물이 있지만 티켓 acceptance criteria 전체는 아직 미완료
- `[ ]` Todo: 아직 착수 전

## Current Snapshot

- Current milestone: Milestone 8. Admin and Operations
- Next ticket: VH-028. Internal Mock Provider API 구현
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
- [x] **VH-013. Idempotency 단위/통합 테스트 작성**
  - Done:
    - `IdempotencyServiceTest`
    - `IdempotencyIntegrationTest`
    - 같은 `requestId + purpose + idempotencyKey` 요청은 동일 verification row 반환 검증
    - 같은 `requestId + purpose`라도 다른 idempotency key면 신규 verification row 생성 검증
  - Verification:
    - `./gradlew test --tests com.verifyhub.idempotency.application.IdempotencyIntegrationTest --no-daemon`
    - `./gradlew clean test --no-daemon`

## Milestone 4. Routing

- [x] **VH-014. 라우팅 도메인 및 정책 조회 구현**
  - Done:
    - `ProviderHealthSnapshot`
    - `RoutingDecision`
    - `RoutingReason`
    - `ProviderRoutingService`
    - 최신 enabled routing policy 조회
    - provider health snapshot 기준 unavailable provider 후보 제외
    - DB source of truth + Redis cache-aside routing policy 운영 기준 문서화
  - Verification:
    - `ProviderRoutingServiceTest`
- [x] **VH-015. WeightedProviderRoutingStrategy 구현**
  - Done:
    - `WeightedProviderRoutingStrategy`
    - `RandomBoundedNumberGenerator`
    - weight 합산 후 random 구간 기반 provider 선택
    - 후보 없음 또는 total weight 0 이하일 때 `ProviderUnavailableException`
  - Verification:
    - `WeightedProviderRoutingStrategyTest`
    - `./gradlew clean test --no-daemon`
- [x] **VH-016. Routing 단위 테스트 작성**
  - Done:
    - 모든 provider가 unavailable일 때 `NO_AVAILABLE_PROVIDER` decision 반환 검증
    - provider health snapshot이 없으면 available로 간주하는 기본 정책 검증
    - total weight가 0이면 `ProviderUnavailableException` 발생 검증
    - zero weight provider는 선택 구간을 갖지 않는다는 점 검증
  - Verification:
    - `./gradlew test --tests com.verifyhub.routing.application.ProviderRoutingServiceTest --tests com.verifyhub.routing.domain.WeightedProviderRoutingStrategyTest --no-daemon`

Routing policy cache 후속 작업:

- Redis cache adapter 구현
- routing policy 변경 시 cache invalidation 또는 replacement 구현
- latest version 조회와 enabled filtering 분리 유지
- emergency stop 최신 version이 과거 enabled 정책으로 fallback하지 않는지 검증

## Milestone 5. Provider Integration and Resilience

- [x] **VH-017. Provider Port 및 Provider HTTP Client 구현**
  - Done:
    - `ProviderClientPort`
    - `ProviderRequest`
    - `ProviderRequestResult`
    - `ProviderRequestResultType`
    - `ProviderResultRequest`
    - `ProviderResult`
    - `ProviderResultStatus`
    - `MockProviderHttpClient`
    - `KgProviderClient`
    - `NiceProviderClient`
    - KG/NICE provider client 패키지 분리 유지
  - Verification:
    - `./gradlew test --tests com.verifyhub.verification.adapter.out.provider.kg.KgProviderClientTest --tests com.verifyhub.verification.adapter.out.provider.nice.NiceProviderClientTest --no-daemon`
- [x] **VH-018. Resilience4j 설정 적용**
  - Partial:
    - `application.yml`에 `kgProvider`, `niceProvider` CircuitBreaker/TimeLimiter/Retry 기본 설정 추가
  - Done:
    - `ProviderClientResilienceDecorator`
    - 실제 provider client와 CircuitBreaker/Retry/TimeLimiter decorator 연결
    - provider별 resilience instance 이름 매핑
    - timeout을 `ProviderTimeoutException`으로 변환
    - provider 호출 실패를 `ProviderCallFailedException`으로 변환
  - Remaining:
    - retry 대상/비대상 세부 정책 고도화
    - retry count event 수집 후 provider call history 반영

- [x] **VH-019. Provider 호출 흐름 구현**
  - Done:
    - `ProviderVerificationCommand`
    - `ProviderVerificationResult`
    - `ProviderVerificationFlowService`
    - routing 결과 기반 provider client 선택
    - `REQUESTED -> ROUTED -> IN_PROGRESS` 상태 전이 연결
    - provider request verification 호출
    - provider call history 저장
    - provider terminal request result에 따른 success/fail/timeout 상태 반영 hook
  - Verification:
    - `./gradlew test --tests com.verifyhub.verification.application.ProviderVerificationFlowServiceTest --tests com.verifyhub.verification.adapter.out.provider.ProviderClientResilienceDecoratorTest --no-daemon`

## Milestone 6. Verification API

- [x] **VH-020. 인증 요청 생성 API 구현**
  - Prep:
    - provider init 요청 모델을 개인정보 입력값이 아닌 `returnUrl`, `closeUrl`, `svcTypes`, `providerRequestNo` 중심으로 변경
    - `ProviderAuthEntry` / `AuthEntryType` 추가
    - VH-020 API 응답에서 provider별 진입 방식을 표현할 수 있도록 준비
  - Done:
    - `POST /api/v1/verifications` 구현
    - `Idempotency-Key` 필수 검증 구현
    - request validation 구현
    - `VerificationCreateService`에서 `IdempotencyService`와 `ProviderVerificationFlowService` 연결
    - 응답에 `verificationId`, `status`, `provider`, `authEntry` 포함
  - Verification:
    - `VerificationCreateServiceTest`
    - `VerificationCreateControllerTest`
    - `./gradlew clean test --no-daemon`
- [x] **VH-021. 인증 조회 및 이력 조회 API 구현**
  - Done:
    - `GET /api/v1/verifications/{verificationId}` 구현
    - `GET /api/v1/verifications/{verificationId}/histories` 구현
    - 조회 전용 `VerificationQueryService` 추가
    - 이력 `createdAt ASC` 조회 port/JPA adapter 구현
  - Verification:
    - `VerificationQueryServiceTest`
    - `VerificationHistoryPersistenceAdapterIT`
    - `VerificationQueryControllerTest`
    - `./gradlew clean test --no-daemon`
- [x] **VH-022. VerificationFlow 통합 테스트 작성**
  - Done:
    - `VerificationFlowIntegrationTest` 추가
    - `POST /api/v1/verifications` 생성 흐름 검증
    - 상태 조회와 이력 조회 API 연결 검증
    - `verification_request`, `verification_history`, `provider_call_history` 저장 검증
    - 같은 idempotency 요청 replay 시 provider flow 재실행 방지
  - Verification:
    - `VerificationCreateServiceTest`
    - `VerificationFlowIntegrationTest`
    - `./gradlew clean test --no-daemon`

## Milestone 7. Provider Return and Late Result

- [x] **VH-023. Provider Return URL 및 Result Retrieval 구현**
  - Done:
    - `GET /api/v1/providers/{provider}/returns` 구현
    - `POST /api/v1/providers/{provider}/returns` 구현
    - path provider와 verification provider 일치 검증
    - `webTransactionId` 저장 및 callback history 기록
    - `ProviderClientPort.requestResult(...)` 기반 결과 조회
    - provider result success/fail과 integrity 검증 결과에 따른 상태 전이
    - terminal 처리 후 outbox event 저장
  - Verification:
    - `ProviderReturnServiceTest`
    - `ProviderReturnControllerTest`
    - `./gradlew clean test --no-daemon`
- [x] **VH-024. Late/Duplicate Return Result 정책 구현**
  - Done:
    - terminal 상태 provider return/result 수신 시 상태 변경 방지
    - terminal 상태 callback에 대한 `late_callback_history` 저장
    - 기존 `webTransactionId`와 incoming `webTransactionId`가 같으면 duplicate로 기록
    - terminal callback 처리 시 outbox event 중복 저장 방지
  - Verification:
    - `ProviderReturnServiceTest`
    - `./gradlew clean test --no-daemon`
- [x] **VH-025. Provider Return/Result 단위/통합 테스트 작성**
  - Done:
    - provider return/result success 통합 테스트 추가
    - provider result integrity failure 통합 테스트 추가
    - terminal duplicate return 통합 테스트 추가
    - 상태, callback history, outbox, late callback history 저장 검증
  - Verification:
    - `./gradlew test --tests com.verifyhub.verification.application.ProviderReturnIntegrationTest --no-daemon`
    - `./gradlew clean test --no-daemon`

## Milestone 8. Admin and Operations

- [x] **VH-027. 라우팅 정책 Admin API 구현**
  - Done:
    - `GET /admin/v1/routing-policies` 구현
    - `PUT /admin/v1/routing-policies` 구현
    - 최신 version 전체 정책 조회 구현
    - 정책 변경 시 기존 row update 대신 next version insert 구현
    - provider set 누락/중복과 invalid weight 검증
    - `provider_routing_policy.version`을 business version으로 사용하도록 JPA `@Version` 제거
    - 관리자 인증 재처리 범위와 `RetryNotAllowedException` 제거
  - Verification:
    - `./gradlew test --tests com.verifyhub.routing.adapter.in.web.AdminRoutingPolicyIntegrationTest --no-daemon`
    - `./gradlew clean test --no-daemon`
- [~] **VH-028. Internal Mock Provider API 구현**
  - Partial:
    - `mockprovider` 패키지 골격 추가
    - KG/NICE provider client 패키지와 mock provider API 책임 분리 문서화
    - NICE 임시 endpoint 추가
      - `/mock/providers/NICE/ido/intc/{version}/auth/token`
      - `/mock/providers/NICE/ido/intc/{version}/auth/url`
      - `/mock/providers/NICE/ido/intc/{version}/auth/result`
    - KG 임시 endpoint 추가
      - `/mock/providers/KG/goCashMain.mcash`
      - `/mock/providers/KG/noti`
  - Remaining:
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
