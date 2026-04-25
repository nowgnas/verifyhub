# Verifyhub Ticket Breakdown

> **For Claude:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task.

**Goal:** `ARCHITECTURE.md` 기준 Server-to-Server 본인인증 오케스트레이터 `verifyhub`를 구현 가능한 티켓 단위로 분해한다.

**Architecture:** 단일 Spring Boot 애플리케이션으로 시작하되 Hexagonal Architecture / Port & Adapter 패키지 구조를 따른다. 상태 변경은 상태 머신과 `VerificationStateService`로 제한하고, 외부 Provider 장애는 Resilience4j와 Mock Provider 시나리오로 검증한다.

**Tech Stack:** Java 17, Spring Boot 2.7.18, Spring Framework 5.3.x, Gradle, MySQL, Redis, Spring Data JPA, Flyway, Resilience4j, Actuator, Micrometer, Prometheus, Testcontainers, JUnit5, AssertJ.

---

## Milestone 0. Project Bootstrap

### VH-001. Gradle Spring Boot 프로젝트 초기화

**Type:** Chore  
**Priority:** P0  
**Dependencies:** None

**Scope**
- Java 17, Spring Boot 2.7.18, Gradle 프로젝트를 생성한다.
- Spring Web, Validation, Data JPA, MySQL, Flyway, Redis, Resilience4j, Actuator, Micrometer Prometheus, Testcontainers, JUnit5, AssertJ 의존성을 추가한다.
- Lombok은 추가하지 않는다.

**Acceptance Criteria**
- `./gradlew test`가 실행된다.
- 기본 애플리케이션 컨텍스트가 로드된다.
- `com.verifyhub` 루트 패키지가 존재한다.
- JPA/Validation import는 Spring Boot 2.7.x 기준에 맞춰 `javax.*` 계열을 사용한다.
- Java 21은 별도 호환성 검증 후보로만 남기고, MVP의 기본 toolchain은 Java 17로 설정한다.

### VH-002. Hexagonal 패키지 구조 생성

**Type:** Chore  
**Priority:** P0  
**Dependencies:** VH-001

**Scope**
- `common`, `verification`, `mockprovider`, `routing`, `idempotency`, `monitoring`, `config` 패키지 트리를 생성한다.
- `domain`, `application`, `port.in`, `port.out`, `adapter.in.web`, `adapter.out.persistence`, `adapter.out.provider`, `adapter.out.redis`, `adapter.out.outbox` 계층을 준비한다.
- `verification.adapter.out.provider.kg/nice`는 Provider HTTP client 책임으로 분리하고, `mockprovider`는 KG/NICE 외부 서버 역할의 fake API 책임으로 분리한다.

**Acceptance Criteria**
- `ARCHITECTURE.md`의 패키지 구조와 큰 방향이 일치한다.
- Controller, Service, Port, Adapter의 책임 경계가 문서화된다.

### VH-003. 공통 인프라 구성

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-001

**Scope**
- 공통 시간 Provider, ID 생성기, API 응답 모델, 에러 응답 모델을 구현한다.
- 전역 예외 핸들러와 traceId 포함 에러 응답을 구현한다.
- 주요 커스텀 예외 클래스를 정의한다.

**Acceptance Criteria**
- 예외 발생 시 `{ code, message, traceId }` 형태로 응답한다.
- `VerificationNotFoundException`, `InvalidStateTransitionException`, `ProviderUnavailableException`, `InvalidCallbackSignatureException`, `RetryNotAllowedException`이 존재한다.

## Milestone 1. Domain Core

### VH-004. Verification 도메인 enum 및 모델 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-003

**Scope**
- `VerificationStatus`, `VerificationEvent`, `VerificationPurpose`, `ProviderType` enum을 구현한다.
- `Verification`, `VerificationHistory`, `ProviderCallHistory`, `LateCallbackHistory`, `OutboxEvent` 도메인 모델을 정의한다.
- 개인정보는 DB 저장 모델에 포함하지 않는다.

**Acceptance Criteria**
- 종료 상태는 `SUCCESS`, `FAIL`, `TIMEOUT`, `CANCELED`로 판정된다.
- request DTO의 `name`, `phoneNumber`, `birthDate`는 저장 대상 도메인에 포함되지 않는다.

### VH-005. VerificationStateMachine 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-004

**Scope**
- `VerificationStateMachine.transit(current, event)`를 구현한다.
- 허용 전이와 금지 전이를 `ARCHITECTURE.md` 기준으로 반영한다.
- 전이 실패 시 `InvalidStateTransitionException`을 발생시킨다.

**Acceptance Criteria**
- `REQUESTED -> ROUTED`, `ROUTED -> IN_PROGRESS`, `IN_PROGRESS -> SUCCESS/FAIL/TIMEOUT`, `REQUESTED/ROUTED/IN_PROGRESS -> CANCELED`가 허용된다.
- terminal 상태에서 모든 전이는 거부된다.
- `REQUESTED -> SUCCESS`, `ROUTED -> FAIL`은 거부된다.

### VH-006. StateMachine 단위 테스트 작성

**Type:** Test  
**Priority:** P0  
**Dependencies:** VH-005

**Scope**
- `StateMachineTest`를 작성한다.
- 성공 전이와 실패 전이를 모두 검증한다.

**Acceptance Criteria**
- `ARCHITECTURE.md`의 StateMachineTest 요구사항 8개 케이스가 모두 포함된다.
- `./gradlew test --tests '*StateMachineTest'`가 통과한다.

## Milestone 2. Persistence and Initial Schema

### VH-007. Flyway V1 초기 인증 스키마 작성

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-004

**Scope**
- 기존 운영 DB 변경이 아니라 신규 verifyhub 서비스의 최초 DB 스키마를 Flyway로 버전 관리한다.
- `verification_request`, `verification_history`, `provider_call_history`, `late_callback_history`, `provider_routing_policy`, `outbox_event` 테이블을 생성한다.
- 모든 unique key와 index를 문서 기준으로 생성한다.
- `verification_request`에는 provider 결과 조회와 return/result 중복 판정을 위한 `provider_transaction_id`, `provider_request_no`, `web_transaction_id`를 포함하고 provider 단위 unique key로 중복 association을 막는다.
- Provider별 인증 진입 URL은 표준 영속 컬럼으로 저장하지 않는다. 요청 멱등성은 idempotency key와 verification 상태를 기준으로 관리한다.

**Acceptance Criteria**
- MySQL 8에서 초기 스키마 적용이 성공한다.
- `verification_request.version`은 optimistic lock에 사용할 수 있다.
- JSON payload 컬럼은 개인정보 마스킹 전제하에 저장 가능하다.

### VH-008. Flyway V2 초기 라우팅 정책 데이터 작성

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-007

**Scope**
- KG weight 10 enabled true version 1을 insert한다.
- NICE weight 90 enabled true version 1을 insert한다.

**Acceptance Criteria**
- 최신 version 정책 조회 시 KG/NICE 두 건이 반환된다.

### VH-009. JPA Entity, Repository, Mapper 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-007

**Scope**
- 인증 요청, 이력, Provider 호출 이력, late callback, 라우팅 정책, outbox entity를 구현한다.
- repository adapter가 port out 인터페이스를 구현하도록 구성한다.
- `@Version` 기반 optimistic lock을 적용한다.

**Acceptance Criteria**
- `findByVerificationId`, `findByUserIdAndPurposeAndIdempotencyKey`, `save`가 동작한다.
- domain과 persistence 간 변환 책임이 adapter 계층에 있다.

## Milestone 3. Application Services

### VH-010. VerificationStateService 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-005, VH-009

**Scope**
- 모든 상태 변경을 `VerificationStateService`로 집중시킨다.
- 상태 전이 성공 시 verification과 history를 함께 저장한다.
- 실패 전이는 history에 저장하지 않는다.

**Acceptance Criteria**
- application service가 entity setter로 상태를 직접 변경하지 않는다.
- 상태 변경마다 `verification_history`가 남는다.

### VH-011. History 및 Outbox 서비스 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-009

**Scope**
- `VerificationHistoryService`, `ProviderCallHistoryService`, `LateCallbackHistoryService`를 구현한다.
- `OutboxEventPort`와 persistence adapter를 구현한다.
- MVP에서는 relay 없이 event 저장까지만 구현한다.

**Acceptance Criteria**
- 인증 요청, 라우팅, provider 호출, callback 처리 흐름에서 필요한 이력이 저장된다.
- outbox event는 `PENDING` 상태로 생성된다.

### VH-012. IdempotencyService 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-009

**Scope**
- `Idempotency-Key` 헤더 기반 기존 verification 조회를 구현한다.
- 동일 `userId + purpose + idempotencyKey` 요청은 기존 결과를 반환한다.
- 동시 insert 충돌은 unique constraint 예외 후 재조회로 처리한다.

**Acceptance Criteria**
- 동일 idempotency key 요청 시 verification row가 1건만 생성된다.
- Redis lock 없이 DB unique key 중심으로 동작한다.

### VH-013. Idempotency 단위/통합 테스트 작성

**Type:** Test  
**Priority:** P1  
**Dependencies:** VH-012

**Scope**
- `IdempotencyServiceTest`와 `IdempotencyIntegrationTest`를 작성한다.
- 같은 key와 다른 key 케이스를 구분한다.

**Acceptance Criteria**
- 같은 key 2회 요청 시 동일 verification이 반환된다.
- 다른 key 요청 시 신규 verification이 생성된다.

## Milestone 4. Routing

### VH-014. 라우팅 도메인 및 정책 조회 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-008, VH-009

**Scope**
- `ProviderRoutingPolicy`, `ProviderHealthSnapshot`, `RoutingDecision`, `RoutingReason`을 구현한다.
- 최신 version의 enabled 정책 조회를 구현한다.
- CircuitBreaker 상태를 routing context에 포함할 수 있게 설계한다.

**Acceptance Criteria**
- routing decision에 `selectedProvider`, `reason`, `policyVersion`, `candidateProviders`가 포함된다.

### VH-015. WeightedProviderRoutingStrategy 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-014

**Scope**
- enabled=false provider를 제외한다.
- CircuitBreaker OPEN provider를 제외한다.
- weight 합산 후 random 기반 provider를 선택한다.
- 후보가 없으면 `ProviderUnavailableException`을 발생시킨다.

**Acceptance Criteria**
- KG 10 / NICE 90 정책에서 두 provider 모두 후보가 될 수 있다.
- disabled/open provider는 선택되지 않는다.

### VH-016. Routing 단위 테스트 작성

**Type:** Test  
**Priority:** P0  
**Dependencies:** VH-015

**Scope**
- `WeightedProviderRoutingStrategyTest`를 작성한다.
- disabled, circuit open, 후보 없음 케이스를 포함한다.

**Acceptance Criteria**
- `ARCHITECTURE.md`의 라우팅 테스트 요구사항이 모두 통과한다.

## Milestone 5. Provider Integration and Resilience

### VH-017. Provider Port 및 Provider HTTP Client 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-010, VH-015

**Scope**
- `ProviderClientPort`, `ProviderRequest`, `ProviderRequestResult`, `ProviderResultRequest`, `ProviderResult`를 구현한다.
- `verification.adapter.out.provider.kg.KgMockProviderClient`, `verification.adapter.out.provider.nice.NiceMockProviderClient`를 구현한다.
- Provider client는 내부 메서드를 직접 호출하지 않고 설정된 base url의 mock provider HTTP API를 호출한다.
- provider base url은 `verifyhub.provider.kg.base-url`, `verifyhub.provider.nice.base-url` 설정으로 분리한다.
- NICE 표준창 방식에 맞춰 requestVerification 결과는 `authUrl`, `transactionId`, `requestNo`를 표현할 수 있게 한다.
- Provider client는 return_url에서 받은 `webTransactionId`로 provider 결과 조회를 수행할 수 있어야 한다.

**Acceptance Criteria**
- 실제 KG/NICE API를 호출하지 않는다.
- 같은 앱 안의 `/mock/providers/{provider}/verifications` HTTP endpoint를 호출해 HTTP status, latency, timeout, retry, circuit breaker를 검증할 수 있다.
- provider 응답은 provider call history 저장에 필요한 raw response, http status, latency, error 정보를 포함한다.
- NICE 결과 조회 응답의 무결성 검증/복호화 실패는 retry 대상이 아닌 business/security fail로 분류할 수 있다.

### VH-018. Resilience4j 설정 적용

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-017

**Scope**
- Provider별 CircuitBreaker, Retry, TimeLimiter instance를 분리한다.
- Retry 대상과 비대상을 구분한다.
- `application.yml`에 설정을 둔다.

**Acceptance Criteria**
- `kgProvider`, `niceProvider` instance가 존재한다.
- TimeLimiter timeoutDuration은 2s, Retry maxAttempts는 2로 설정된다.
- 4xx, business fail, invalid signature는 retry하지 않는다.

### VH-019. Provider 호출 흐름 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-010, VH-011, VH-017, VH-018

**Scope**
- provider call 시작 시 `IN_PROGRESS`로 전이한다.
- 즉시 성공/실패 mock scenario는 상태에 반영한다.
- NICE 표준창 기반 scenario는 `authUrl` 반환 후 `IN_PROGRESS`를 유지한다.
- provider call history와 outbox event를 저장한다.

**Acceptance Criteria**
- provider 호출 결과별 `SUCCESS`, `FAIL`, `TIMEOUT`, `ERROR/ACCEPTED` 처리가 명확하다.
- timeout 발생 시 `TIMEOUT` 상태로 전이된다.

## Milestone 6. Verification API

### VH-020. 인증 요청 생성 API 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-012, VH-015, VH-019

**Scope**
- `POST /api/v1/verifications`를 구현한다.
- request validation과 `Idempotency-Key` 필수 검증을 구현한다.
- 요청 생성부터 provider 호출까지 command flow를 연결한다.

**Acceptance Criteria**
- 성공 응답은 `verificationId`, `status`, `provider`를 포함한다.
- Controller에는 비즈니스 로직이 없다.
- 개인정보는 로그와 raw payload에 마스킹된다.

### VH-021. 인증 조회 및 이력 조회 API 구현

**Type:** Feature  
**Priority:** P1  
**Dependencies:** VH-011, VH-020

**Scope**
- `GET /api/v1/verifications/{verificationId}`를 구현한다.
- `GET /api/v1/verifications/{verificationId}/histories`를 구현한다.

**Acceptance Criteria**
- 상태 조회 응답에 `verificationId`, `status`, `provider`, `purpose`, `requestedAt`, `completedAt`이 포함된다.
- 이력 조회는 시간순으로 반환된다.

### VH-022. VerificationFlow 통합 테스트 작성

**Type:** Test  
**Priority:** P0  
**Dependencies:** VH-020, VH-021

**Scope**
- 인증 요청 생성부터 provider 선택, 상태 확인, history 저장, provider call history 저장까지 검증한다.

**Acceptance Criteria**
- `VerificationFlowIntegrationTest`가 Testcontainers MySQL 기반으로 통과한다.

## Milestone 7. Provider Return and Late Result

### VH-023. Provider Return URL 및 Result Retrieval 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-011, VH-019

**Scope**
- `GET/POST /api/v1/providers/{provider}/returns`를 구현한다.
- path provider 검증, `webTransactionId` 수신, verification 조회를 수행한다.
- `IN_PROGRESS` 상태에서 provider adapter로 인증 결과를 조회한다.
- NICE 응답의 `integrity_value`를 검증하고 `enc_data`를 복호화한 뒤 SUCCESS/FAIL 상태 전이에 반영한다.

**Acceptance Criteria**
- 무결성 검증 실패 또는 복호화 실패는 상태 오염 없이 FAIL 또는 보안 실패 이력으로 기록된다.
- 정상 return/result 처리는 history와 outbox event를 저장한다.

### VH-024. Late/Duplicate Return Result 정책 구현

**Type:** Feature  
**Priority:** P0  
**Dependencies:** VH-023

**Scope**
- terminal 상태에서 도착한 return_url 또는 결과 조회 요청은 상태를 변경하지 않는다.
- late callback history에 기존 상태, provider 결과, provider, raw payload, reason을 저장한다.
- webTransactionId 또는 providerTransactionId 기준 중복 여부를 판정한다.
- optimistic lock 충돌 시 재조회 후 terminal 여부를 다시 확인한다.

**Acceptance Criteria**
- `TIMEOUT` 이후 `SUCCESS` 결과가 조회되어도 상태는 `TIMEOUT`으로 유지된다.
- 중복 return/result는 `duplicate=true`로 기록된다.

### VH-025. Provider Return/Result 단위/통합 테스트 작성

**Type:** Test  
**Priority:** P0  
**Dependencies:** VH-024

**Scope**
- `ProviderResultApplicationServiceTest`와 `LateCallbackIntegrationTest`를 작성한다.
- 성공, 실패, timeout 이후 late return/result, duplicate return/result, integrity 검증 실패를 검증한다.

**Acceptance Criteria**
- `ARCHITECTURE.md`의 return/result 테스트 요구사항이 모두 통과한다.

## Milestone 8. Admin and Operations

### VH-026. 관리자 재처리 API 구현

**Type:** Feature  
**Priority:** P1  
**Dependencies:** VH-019, VH-021

**Scope**
- `POST /admin/v1/verifications/{verificationId}/retry`를 구현한다.
- `TIMEOUT` 또는 retryable `FAIL`만 재처리 가능하게 제한한다.
- MVP에서는 같은 verification_id로 provider call을 재수행하고 `ADMIN_RETRY_REQUESTED` 성격의 history를 남긴다.

**Acceptance Criteria**
- `SUCCESS`, `CANCELED` 상태는 재처리할 수 없다.
- 재처리 시 provider call history가 추가된다.

### VH-027. 라우팅 정책 Admin API 구현

**Type:** Feature  
**Priority:** P1  
**Dependencies:** VH-014

**Scope**
- `GET /admin/v1/routing-policies`를 구현한다.
- `PUT /admin/v1/routing-policies`를 구현한다.
- 변경 시 기존 row update가 아니라 version 증가 insert를 수행한다.

**Acceptance Criteria**
- verification에는 선택 당시 policyVersion이 저장된다.
- 최신 version 정책만 라우팅에 사용된다.

### VH-028. Internal Mock Provider API 구현

**Type:** Feature  
**Priority:** P1  
**Dependencies:** VH-017

**Scope**
- `mockprovider` bounded context를 구현한다.
- `POST /mock/providers/{provider}/verifications`를 구현한다.
- `GET/POST /mock/providers/{provider}/returns`를 구현한다.
- `POST /mock/providers/{provider}/results`를 구현한다.
- `POST /mock/providers/{provider}/scenario`를 구현한다.
- provider별 scenario 상태를 변경할 수 있게 한다.
- scenario별 `SUCCESS`, `FAIL`, `TIMEOUT`, `HTTP_500`, `DELAYED_RETURN`, `DUPLICATE_RETURN`, `INVALID_INTEGRITY_RESULT` 동작을 지원한다.

**Acceptance Criteria**
- Mock Provider API는 `verification` application service를 직접 호출하지 않는다.
- scenario 변경 후 다음 provider 호출에 반영된다.
- 지원 scenario 외 값은 validation error로 응답한다.

### VH-029. Metrics 및 Actuator 구성

**Type:** Feature  
**Priority:** P1  
**Dependencies:** VH-019, VH-024

**Scope**
- verification/provider counter, timer, gauge를 구현한다.
- `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus` 노출을 설정한다.
- circuit breaker state metric을 확인 가능하게 한다.

**Acceptance Criteria**
- timeout, success, fail, late callback, duplicate callback metric이 증가한다.
- Prometheus scrape endpoint가 동작한다.

## Milestone 9. Resilience and End-to-End Tests

### VH-030. TimeoutFlow 통합 테스트 작성

**Type:** Test  
**Priority:** P0  
**Dependencies:** VH-018, VH-020, VH-029

**Scope**
- Mock Provider TIMEOUT scenario 설정 후 인증 요청을 수행한다.
- 상태와 metrics를 검증한다.

**Acceptance Criteria**
- verification status가 `TIMEOUT`으로 전이된다.
- provider timeout metric이 증가한다.

### VH-031. CircuitBreaker 통합 테스트 작성

**Type:** Test  
**Priority:** P1  
**Dependencies:** VH-015, VH-018, VH-028

**Scope**
- provider 500 에러를 반복 발생시킨다.
- CircuitBreaker OPEN 상태를 확인한다.
- OPEN provider가 routing에서 제외되는지 검증한다.

**Acceptance Criteria**
- CircuitBreaker가 OPEN인 provider는 선택되지 않는다.
- 사용 가능한 provider가 없으면 `ProviderUnavailableException` 계열 응답이 반환된다.

### VH-032. 전체 테스트 안정화

**Type:** Test  
**Priority:** P0  
**Dependencies:** VH-006, VH-013, VH-016, VH-022, VH-025, VH-030, VH-031

**Scope**
- 단위 테스트와 Testcontainers 통합 테스트를 정리한다.
- 테스트 데이터 격리와 실행 순서 의존성을 제거한다.

**Acceptance Criteria**
- `./gradlew test`가 로컬에서 통과한다.
- 주요 실패 케이스가 명확한 assertion message를 가진다.

## Milestone 10. Local Runtime and Documentation

### VH-033. docker-compose 작성

**Type:** Chore  
**Priority:** P1  
**Dependencies:** VH-007, VH-029

**Scope**
- MySQL 8, Redis 7, Prometheus를 구성한다.
- MySQL database/user/password는 `verifyhub`로 맞춘다.
- Prometheus가 `/actuator/prometheus`를 scrape하도록 설정한다.

**Acceptance Criteria**
- `docker compose up`으로 로컬 인프라가 올라온다.
- Spring Boot 앱이 MySQL/Redis에 연결된다.

### VH-034. README 작성

**Type:** Docs  
**Priority:** P0  
**Dependencies:** VH-032, VH-033

**Scope**
- 프로젝트 목적, 아키텍처, 상태 머신, 라우팅 정책, Resilience4j 정책, 멱등성 정책, late callback 정책, ERD 요약, API 예시, mock scenario 사용법, metrics 확인법, 실행/테스트 방법, Design Decisions, 향후 확장 방향을 작성한다.
- 포트폴리오용 설명 문구를 포함한다.

**Acceptance Criteria**
- README만 보고 로컬 실행과 주요 API 호출이 가능하다.
- 주요 설계 결정이 `Design Decisions` 섹션에 기록된다.

### VH-035. Java 21 호환성 검증 스파이크

**Type:** Spike  
**Priority:** P2  
**Dependencies:** VH-032

**Scope**
- 기본 구현은 Java 17 toolchain으로 유지한다.
- 별도 CI job 또는 로컬 검증 프로파일에서 Java 21로 test suite를 실행한다.
- Spring Boot 2.7.18, Gradle, Testcontainers, Resilience4j, MySQL driver, Redis client의 런타임 호환성을 확인한다.

**Acceptance Criteria**
- Java 21에서 `./gradlew test`가 통과하는지 결과를 기록한다.
- 실패 시 원인 dependency와 조치 방안을 README `Design Decisions` 또는 별도 note에 남긴다.
- Java 21 전환 여부는 이 스파이크 결과와 회사 운영 런타임 계획을 기준으로 별도 결정한다.

## Post-MVP Extension Tickets

### VH-101. Outbox Relay 구현

**Type:** Feature  
**Priority:** P2  
**Dependencies:** VH-011, VH-032

**Scope**
- `PENDING` outbox event를 주기적으로 조회한다.
- publish 성공 시 `PUBLISHED`, 실패 시 retry_count 증가를 처리한다.
- Publisher 인터페이스를 Kafka/SQS로 확장 가능하게 유지한다.

**Acceptance Criteria**
- relay 재시도와 상태 변경이 테스트된다.

### VH-102. AI 운영 분석 기능 설계 및 구현

**Type:** Feature  
**Priority:** P3  
**Dependencies:** VH-029

**Scope**
- 최근 30분 provider 실패율, timeout, late callback을 요약한다.
- 운영자용 장애 리포트와 Slack 알림 문장을 생성한다.

**Acceptance Criteria**
- 민감 정보 없이 운영 지표만 사용한다.

### VH-103. 관리자 대시보드 구현

**Type:** Feature  
**Priority:** P3  
**Dependencies:** VH-029

**Scope**
- provider별 성공률, timeout rate, p95 latency, late callback count, circuit breaker state를 표시한다.

**Acceptance Criteria**
- 운영자가 provider 장애 상태를 빠르게 확인할 수 있다.

## Suggested Execution Order

1. VH-001 ~ VH-006: 프로젝트 기반과 상태 머신 완성
2. VH-007 ~ VH-013: DB, repository, 멱등성 완성
3. VH-014 ~ VH-019: 라우팅, Provider, Resilience4j 완성
4. VH-020 ~ VH-025: 사용자 API와 callback 정책 완성
5. VH-026 ~ VH-029: Admin, Mock scenario, metrics 완성
6. VH-030 ~ VH-034: 통합 테스트, docker-compose, README 완성
7. VH-101 ~ VH-103: MVP 이후 확장
