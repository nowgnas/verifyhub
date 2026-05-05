# Verifyhub 진행 현황 및 다음 작업

작성일: 2026-05-01

## 현재 상태

현재 `main`은 Milestone 6의 `VH-020`과 `VH-021`까지 반영된 상태다. 다음 작업은 `VH-022. VerificationFlow 통합 테스트 작성`이다.

마지막 전체 검증 기준:

```bash
./gradlew clean test --no-daemon
```

결과: `BUILD SUCCESSFUL`

## 지금까지 작업된 내용

### Milestone 0. Project Bootstrap

- Java 17, Spring Boot 2.7.18, Gradle 기반 프로젝트를 초기화했다.
- Hexagonal Architecture 기준 패키지 구조를 구성했다.
- 공통 응답, 공통 예외, global exception handler, time/id generator를 추가했다.

### Milestone 1. Domain Core

- 인증 도메인 핵심 모델을 구현했다.
  - `Verification`
  - `VerificationStatus`
  - `VerificationEvent`
  - `VerificationPurpose`
  - `ProviderType`
  - `VerificationHistory`
  - `ProviderCallHistory`
  - `LateCallbackHistory`
  - `OutboxEvent`
- `VerificationStateMachine`을 구현하고 허용/금지 상태 전이를 검증했다.
- terminal 상태 이후 추가 전이를 거부하도록 했다.

### Milestone 2. Persistence and Initial Schema

- Flyway V1 초기 인증 스키마를 작성했다.
  - `verification_request`
  - `verification_history`
  - `provider_call_history`
  - `late_callback_history`
  - `provider_routing_policy`
  - `outbox_event`
- Flyway V2 초기 provider routing policy seed를 추가했다.
- JPA entity, repository, MapStruct mapper, persistence adapter를 구현했다.
- Testcontainers MySQL 기반 persistence 통합 테스트를 추가했다.

### Milestone 3. Application Services

- `VerificationStateService`를 구현했다.
  - `routeTo`
  - `startProviderCall`
  - `markSuccess`
  - `markFail`
  - `markTimeout`
  - `cancel`
- 상태 전이 시 `verification_request`와 `verification_history`가 함께 저장되도록 했다.
- history, provider call history, late callback history, outbox event service와 persistence adapter를 구현했다.
- `IdempotencyService`를 구현했다.
  - `requestId + purpose + idempotencyKey` 기준 중복 요청을 재사용한다.
  - unique constraint 충돌 시 재조회한다.

### Milestone 4. Routing

- routing domain과 service를 구현했다.
  - `ProviderHealthSnapshot`
  - `RoutingDecision`
  - `RoutingReason`
  - `ProviderRoutingService`
- weighted routing 전략을 구현했다.
- provider health snapshot 기준 unavailable provider를 제외하도록 했다.
- 최신 enabled routing policy 조회와 weighted provider 선택을 검증했다.

### Milestone 5. Provider Integration and Resilience

- provider port와 request/result model을 추가했다.
  - `ProviderClientPort`
  - `ProviderRequest`
  - `ProviderRequestResult`
  - `ProviderResultRequest`
  - `ProviderResult`
- KG/NICE provider client 패키지를 분리했다.
- Mock provider HTTP client를 추가했다.
- Resilience4j 기반 circuit breaker, retry, time limiter decorator를 연결했다.
- provider 호출 timeout/fail을 공통 예외로 변환하도록 했다.
- `ProviderVerificationFlowService`를 구현했다.
  - provider routing
  - `REQUESTED -> ROUTED -> IN_PROGRESS` 상태 전이
  - provider init 호출
  - provider call history 저장
  - provider terminal result hook 처리

### Milestone 6. Verification API

#### VH-020. 인증 요청 생성 API

- `POST /api/v1/verifications`를 구현했다.
- `Idempotency-Key` header 필수 검증을 추가했다.
- request body validation을 추가했다.
- `VerificationCreateService`에서 다음 흐름을 연결했다.
  - verification id 생성
  - `IdempotencyService.getOrCreate(...)`
  - `ProviderVerificationFlowService.requestProviderVerification(...)`
- 생성 응답에 다음 값을 포함한다.
  - `verificationId`
  - `status`
  - `provider`
  - `authEntry`
- 설계/구현 계획 문서를 추가했다.
  - `docs/plans/2026-05-01-vh-020-verification-create-api-design.md`
  - `docs/plans/2026-05-01-vh-020-verification-create-api.md`

#### VH-021. 인증 조회 및 이력 조회 API

- `GET /api/v1/verifications/{verificationId}`를 구현했다.
- `GET /api/v1/verifications/{verificationId}/histories`를 구현했다.
- 조회 전용 `VerificationQueryService`를 추가했다.
- 이력 조회는 `createdAt ASC` 순서로 반환한다.
- 조회 응답에 다음 값을 포함한다.
  - `verificationId`
  - `status`
  - `provider`
  - `purpose`
  - `requestedAt`
  - `completedAt`
- 구현 계획 문서를 추가했다.
  - `docs/plans/2026-05-01-vh-021-verification-query-api.md`

### 운영/로컬 환경

- `docker-compose.yml`을 작성했다.
  - MySQL 8.0
  - Redis 7
  - Prometheus
- Actuator/Prometheus endpoint 노출 설정을 일부 추가했다.
- internal mock provider API 골격과 일부 KG/NICE mock endpoint를 추가했다.

## 앞으로 작업할 내용

### 1. VH-022. VerificationFlow 통합 테스트 작성

다음 최우선 작업이다.

검증 범위:

- `POST /api/v1/verifications` 호출
- idempotency 적용
- provider routing
- provider init 호출
- `verification_request` 상태 저장
- `verification_history` 저장
- `provider_call_history` 저장
- `GET /api/v1/verifications/{verificationId}` 상태 조회
- `GET /api/v1/verifications/{verificationId}/histories` 이력 조회

권장 방식:

- Testcontainers MySQL 기반 `@SpringBootTest` 통합 테스트로 작성한다.
- provider client는 실제 외부 호출 대신 mock provider 또는 테스트용 stub port를 사용한다.
- 성공 기준은 `VerificationFlowIntegrationTest` 통과다.

### 2. VH-023. Provider Return URL 및 Result Retrieval 구현

provider 인증 완료 후 return/result 흐름을 구현한다.

주요 작업:

- provider return endpoint 설계
- NICE return 이후 result retrieval 흐름 연결
- KG Notiurl 중심 callback 흐름 정리
- `webTransactionId`, `providerTransactionId`, `providerRequestNo` 기반 correlation 처리
- 성공/실패/timeout 상태 전이와 history 저장 연결

### 3. VH-024. Late/Duplicate Return Result 정책 구현

늦게 도착하거나 중복 도착한 provider result를 처리한다.

주요 작업:

- terminal 상태 이후 callback 처리 정책 확정
- late callback history 저장
- duplicate callback idempotency 처리
- 상태 변경 없이 audit trail만 남기는 케이스 검증

### 4. VH-025. Provider Return/Result 단위 및 통합 테스트 작성

VH-023, VH-024의 회귀 테스트를 추가한다.

검증 범위:

- 정상 return/result
- 실패 result
- timeout 이후 late success
- duplicate callback
- invalid callback signature
- correlation key 불일치

### 5. VH-027. 라우팅 정책 Admin API 구현

provider routing policy를 운영 중 변경할 수 있는 API를 구현한다.

주요 작업:

- provider weight 변경
- provider enable/disable
- policy version 증가
- cache invalidation 또는 replacement 전략
- emergency stop이 과거 enabled 정책으로 fallback하지 않는지 검증

### 6. VH-028. Internal Mock Provider API 완성

현재 mock provider는 일부 endpoint만 구현되어 있다.

남은 작업:

- `/mock/providers/{provider}/scenario`
- scenario service
- timeout, fail, duplicate callback, late callback scenario
- VH-030/VH-031 통합 테스트에서 사용할 mock behavior 고도화

### 7. VH-029. Metrics 및 Actuator 고도화

Actuator/Prometheus endpoint 노출은 일부 완료되어 있다.

남은 작업:

- verification 생성/성공/실패/timeout metric
- provider별 호출 수, latency, 실패율 metric
- circuit breaker 상태 metric 확인
- 운영 dashboard에서 사용할 label 정책 정리

### 9. Milestone 9. Resilience and E2E Tests

남은 티켓:

- `VH-030. TimeoutFlow 통합 테스트 작성`
- `VH-031. CircuitBreaker 통합 테스트 작성`
- `VH-032. 전체 테스트 안정화`

목표:

- provider timeout 시 상태와 history가 일관되게 남는지 검증한다.
- circuit breaker open/half-open/close 흐름에서 provider fallback 또는 실패 응답이 일관적인지 검증한다.
- Testcontainers 기반 통합 테스트의 실행 시간을 관리하고 flaky test를 줄인다.

### 10. Milestone 10. Local Runtime and Documentation

남은 티켓:

- `VH-034. README 작성`
- `VH-035. Java 21 호환성 검증 스파이크`

README에 포함할 내용:

- 전체 아키텍처
- 상태 머신
- API 예시
- provider routing 정책
- local docker compose 실행 방법
- 운영 지표
- 주요 design decisions

### 11. 별도 Frontend 테스트 콘솔 프로젝트

API와 mock provider 흐름을 수동으로 확인할 수 있는 간단한 화면을 만든다. 이 화면은 verifyhub backend 프로젝트 안에 두지 않는다. `orchestrator` 디렉터리 밖에 별도 frontend 프로젝트를 생성해서 작업한다.

권장 위치:

```text
/Users/nowgnas/project/verifyhub-console
```

목표:

- 버튼 클릭으로 verifyhub API를 호출한다.
- mock provider API를 사용해 success, fail, timeout, duplicate, late callback scenario를 수동 테스트한다.
- 개발 중 backend API 흐름을 브라우저에서 빠르게 확인한다.

초기 화면 구성:

- 인증 요청 생성 영역
  - `requestId`
  - `purpose`
  - `Idempotency-Key`
  - `returnUrl`
  - `closeUrl`
  - `svcTypes`
  - `POST /api/v1/verifications` 호출 버튼
- 인증 상태 조회 영역
  - `verificationId`
  - `GET /api/v1/verifications/{verificationId}` 호출 버튼
- 인증 이력 조회 영역
  - `GET /api/v1/verifications/{verificationId}/histories` 호출 버튼
- mock provider scenario 영역
  - provider 선택: `NICE`, `KG`
  - scenario 선택: success, fail, timeout, duplicate callback, late callback
  - `/mock/providers/{provider}/scenario` 호출 버튼
- provider return/result 테스트 영역
  - NICE return/result 흐름 호출 버튼
  - KG Notiurl callback 호출 버튼
- 응답 패널
  - 최근 요청 payload
  - 최근 응답 body
  - HTTP status
  - 오류 메시지

기술 선택:

- Vite + React + TypeScript를 기본 후보로 둔다.
- UI는 테스트 콘솔 목적에 맞게 단순한 form, button, response panel 중심으로 만든다.
- backend base URL은 환경 변수로 분리한다.
  - 예: `VITE_VERIFYHUB_BASE_URL=http://localhost:8080`

진행 순서:

1. `orchestrator` 밖에 frontend 프로젝트를 생성한다.
2. verifyhub local backend와 docker compose 실행 방법을 README에 적는다.
3. `VH-020`, `VH-021` API 호출 화면을 먼저 만든다.
4. `VH-028` mock provider scenario API가 완성되면 scenario 버튼을 연결한다.
5. `VH-023~VH-025` provider return/result 구현 후 return/result 테스트 버튼을 연결한다.
6. `VH-030~VH-031` resilience 테스트를 위해 timeout/circuit breaker scenario 버튼을 보강한다.

## 우선순위 제안

1. `VH-022`로 생성 API와 조회 API를 하나의 통합 흐름으로 고정한다.
2. `VH-023~VH-025`로 provider return/result 흐름을 완성한다.
3. `orchestrator` 밖에 별도 frontend 테스트 콘솔 프로젝트를 만들고 `VH-020`, `VH-021` API 버튼을 먼저 연결한다.
4. `VH-028` mock scenario를 고도화해 timeout/late/duplicate 테스트 기반을 만든 뒤 frontend 버튼과 연결한다.
5. `VH-030~VH-032`로 resilience와 전체 테스트 안정화를 진행한다.
6. 운영 API와 문서는 API 흐름이 안정된 뒤 `VH-027`, `VH-034` 순서로 정리한다.
