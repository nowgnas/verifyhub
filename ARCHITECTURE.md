너는 시니어 백엔드 아키텍트이자 Java/Spring Boot 개발자다.
아래 요구사항을 기준으로 "Server-to-Server 본인인증 오케스트레이터" 프로젝트를 설계하고 구현해줘.

목표:

- 실제 KG/NICE 본인인증 연동 키 없이 Mock Provider를 사용한다.
- 핵심은 외부 Provider 연동 안정성, 상태 정합성, 멱등성, 재처리 가능성, 운영 관측성이다.
- 단순 CRUD가 아니라 실무형 백엔드 아키텍처 프로젝트로 구현한다.
- 회사 적용 가능성을 우선하여 Java 17, Spring Boot 2.7.18, MySQL, Redis, Resilience4j, Actuator, Micrometer를 사용한다.
- 초기에는 단일 Spring Boot 애플리케이션으로 시작하되, 패키지는 Hexagonal Architecture / Port & Adapter 스타일로 나눈다.
- Kafka는 MVP에서는 필수 구현하지 말고, Outbox 테이블과 Publisher 인터페이스까지만 설계한다. 이후 Kafka/SQS로 확장 가능하게 만든다.

프로젝트명:

- verifyhub

한 줄 설명:

- 여러 본인인증 연동사를 안정적으로 호출하고, 장애 상황에서도 인증 상태와 이력을 정합성 있게 관리하는 Server-to-Server 본인인증 오케스트레이터

핵심 도메인:

- 사용자가 본인인증 요청을 생성한다.
- 오케스트레이터가 KG/NICE 중 하나의 Provider를 선택한다.
- 선택된 Provider Adapter를 통해 인증 요청을 전달한다.
- Provider는 Mock API로 동작하며 성공, 실패, 지연, 타임아웃, 중복 콜백, 늦은 콜백을 시뮬레이션할 수 있다.
- 인증 요청 상태는 상태 머신을 통해서만 변경된다.
- 종료 상태 이후 도착한 return_url 또는 결과 조회는 최종 상태를 변경하지 않고 late callback history로 기록한다.
- 모든 요청/응답/상태전이/콜백은 추적 가능해야 한다.

기술 스택:

- Java 17
- Spring Boot 2.7.18
- Spring Framework 5.3.x
- Gradle
- Spring Web
- Spring Validation
- Spring Data JPA
- MySQL
- Redis
- Resilience4j
  - CircuitBreaker
  - Retry
  - TimeLimiter
- Spring Boot Actuator
- Micrometer
- Prometheus endpoint
- Testcontainers
- JUnit5
- AssertJ
- WireMock 또는 Spring MockMvc 기반 Mock Provider
- Lombok은 사용하지 말고 명시적인 코드로 작성한다.

런타임 버전 결정:

- MVP 기준 런타임은 Java 17 + Spring Boot 2.7.18로 고정한다.
- 목적은 회사 환경에 적용하기 전 동일한 기술 제약에서 설계와 구현을 검증하는 것이다.
- Spring Boot 2.7.18은 공식적으로 Java 8 이상, Java 21 이하와 호환된다. 따라서 Java 21은 별도 호환성 검증 대상으로 둘 수 있다.
- Java 21은 장기적으로 검토 가능하지만, 회사 운영 런타임이 Java 17이라면 MVP의 기본 개발/테스트/배포 기준으로 삼지 않는다.
- Java 25는 Spring Boot 2.7.18의 공식 호환 범위를 벗어나므로 이 프로젝트의 MVP 기준에서 제외한다. Java 25를 사용하려면 Spring Boot 4.x 이상 전환과 전체 의존성 검증을 별도 과제로 다룬다.
- Kotlin은 현재 회사 표준이 아니므로 MVP에서는 사용하지 않는다. 설계 자체는 언어에 크게 종속되지 않지만, 회사 적용 목적상 Java로 구현한다.
- Spring Boot 2.7.x 기반이므로 JPA/Validation 관련 import는 `jakarta.*`가 아니라 `javax.*` 계열을 사용한다.
- Lombok은 사용하지 않는다. 상태 변경 메서드와 생성자 의도를 코드에 명시하고, `@Setter`, `@Data`로 도메인 불변식이 우회되는 것을 막기 위함이다.

아키텍처:
Client
-> VerificationController
-> VerificationCommandService
-> IdempotencyService
-> ProviderRoutingService
-> VerificationStateService
-> ProviderClientPort
-> VerificationHistoryService
-> ProviderCallHistoryService
-> OutboxEventPort
-> MySQL

Provider Return URL / Result Retrieval
-> ProviderReturnController
-> ProviderResultApplicationService
-> ProviderResultVerifier
-> ProviderClientPort
-> VerificationStateService
-> VerificationHistoryService
-> LateCallbackHistoryService

패키지 구조:
src/main/java/com/verifyhub
common
exception
time
id
response
verification
domain
Verification
VerificationStatus
VerificationEvent
VerificationPurpose
ProviderType
VerificationHistory
ProviderCallHistory
LateCallbackHistory
OutboxEvent
application
VerificationCommandService
VerificationQueryService
ProviderResultApplicationService
VerificationStateService
VerificationHistoryService
ProviderCallHistoryService
LateCallbackHistoryService
port
in
RequestVerificationUseCase
QueryVerificationUseCase
HandleProviderReturnUseCase
out
VerificationRepositoryPort
VerificationHistoryRepositoryPort
ProviderCallHistoryRepositoryPort
LateCallbackHistoryRepositoryPort
ProviderClientPort
ProviderRoutingPolicyRepositoryPort
OutboxEventPort
DistributedLockPort
adapter
in
web
VerificationController
ProviderReturnController
AdminVerificationController
dto
out
persistence
entity
repository
mapper
provider
kg
KgMockProviderClient
KgProviderProperties
nice
NiceMockProviderClient
NiceProviderProperties
redis
outbox
mockprovider
domain
MockProviderScenario
MockProviderRequest
MockProviderResponse
MockProviderResult
application
MockProviderScenarioService
MockProviderVerificationService
adapter
in
web
MockProviderScenarioController
MockProviderVerificationController
dto
routing
domain
ProviderRoutingPolicy
ProviderHealthSnapshot
RoutingDecision
RoutingReason
application
ProviderRoutingService
WeightedProviderRoutingStrategy
ProviderHealthService
idempotency
application
IdempotencyService
monitoring
VerificationMetrics
ProviderMetrics
config
Resilience4jConfig
WebClientConfig
JpaConfig
RedisConfig
ActuatorConfig

상태 정의:
VerificationStatus:

- REQUESTED
- ROUTED
- IN_PROGRESS
- SUCCESS
- FAIL
- TIMEOUT
- CANCELED

VerificationEvent:

- VERIFICATION_REQUESTED
- ROUTE_SELECTED
- PROVIDER_CALL_STARTED
- PROVIDER_CALL_SUCCEEDED
- PROVIDER_CALL_FAILED
- PROVIDER_TIMEOUT
- CALLBACK_SUCCESS
- CALLBACK_FAIL
- CANCEL_REQUESTED

허용 상태 전이:

- REQUESTED -> ROUTED
- ROUTED -> IN_PROGRESS
- IN_PROGRESS -> SUCCESS
- IN_PROGRESS -> FAIL
- IN_PROGRESS -> TIMEOUT
- REQUESTED -> CANCELED
- ROUTED -> CANCELED
- IN_PROGRESS -> CANCELED

금지 상태 전이:

- SUCCESS -> any
- FAIL -> any
- TIMEOUT -> any
- CANCELED -> any
- REQUESTED -> SUCCESS
- REQUESTED -> FAIL
- ROUTED -> SUCCESS
- ROUTED -> FAIL

종료 상태:

- SUCCESS
- FAIL
- TIMEOUT
- CANCELED

종료 상태 이후 return/result 정책:

- 이미 SUCCESS, FAIL, TIMEOUT, CANCELED 상태인 인증 요청에 return_url 또는 결과 조회 요청이 도착하면 상태를 변경하지 않는다.
- late_callback_history 테이블에 저장한다.
- 기존 상태, provider 결과, provider, raw payload, reason을 기록한다.
- 중복 return_url 수신이나 중복 결과 조회도 상태를 변경하지 않고 duplicate 여부를 기록한다.
- 운영자가 추적할 수 있도록 조회 API를 제공한다.

Provider:

- KG
- NICE

Mock Provider 구현 정책:

- MVP에서는 별도 KG/NICE mock 서버 컨테이너를 만들지 않는다.
- 동일 Spring Boot 애플리케이션 안에 `mockprovider` bounded context를 두고 KG/NICE 외부 서버 역할의 HTTP API를 제공한다.
- 오케스트레이터의 provider outbound adapter는 내부 메서드를 직접 호출하지 않고 HTTP client로 mock provider API를 호출한다.
- 이를 통해 실제 외부 연동과 같은 HTTP status, latency, timeout, retry, circuit breaker 동작을 검증한다.
- `verification.adapter.out.provider.kg`와 `verification.adapter.out.provider.nice`는 외부 Provider 호출 client 역할만 담당한다.
- `mockprovider` 패키지는 KG/NICE가 제공한다고 가정하는 fake API, scenario 저장/조회, 지연/오류/중복 return_url/result 시뮬레이션만 담당한다.
- provider base url은 설정으로 분리한다.

NICE 표준창 기반 본인인증 정책:

- NICE는 순수 server-to-server 단일 호출이 아니라 표준창을 포함한다.
- Provider adapter는 NICE `/ido/intc/v1.0/auth/token` 호출로 access_token, ticket, iterators를 받고, `/ido/intc/v1.0/auth/url` 호출로 auth_url, transaction_id, request_no를 받는다.
- verifyhub는 Client에게 Provider별 인증 진입 URL을 반환하고, Client는 해당 URL로 NICE 표준창을 연다.
- 인증 진입 URL은 Provider 표준 영속 필드로 보지 않는다. 요청 멱등성은 `request_id`, `purpose`, `idempotency_key`와 저장된 verification 상태로 관리한다.
- 사용자가 표준창에서 개인정보를 입력하고 인증을 완료하면 NICE 표준창은 verifyhub의 `return_url`로 `web_transaction_id`를 전달한다.
- verifyhub는 `web_transaction_id`, `transaction_id`, `request_no`로 NICE `/ido/intc/v1.0/auth/result`를 호출한다.
- 결과 응답의 `integrity_value`를 검증하고 `enc_data`를 AES/GCM으로 복호화한 뒤 성공/실패 상태를 반영한다.
- 복호화된 name, birthdate, mobile_no 등 개인정보는 DB에 저장하지 않는다.
- 무결성 검증 실패, 복호화 실패, 인증 실패 결과는 retry 대상이 아니라 business/security fail로 처리한다.

provider base url 예시:

verifyhub:
provider:
kg:
base-url: http://localhost:8080/mock/providers/KG
nice:
base-url: http://localhost:8080/mock/providers/NICE

Provider 선택 정책:

- 기본 가중치 기반 라우팅
  - KG: 10
  - NICE: 90
- enabled=false인 Provider는 선택하지 않는다.
- CircuitBreaker가 OPEN인 Provider는 선택하지 않는다.
- 사용 가능한 Provider가 없으면 503 성격의 예외를 발생시킨다.
- 라우팅 결과는 verification_request에 provider로 저장하고, routing reason을 history에 남긴다.
- 향후 확장을 위해 RoutingDecision 객체를 둔다.
  - selectedProvider
  - reason
  - policyVersion
  - candidateProviders

Resilience4j 정책:

- Provider별 CircuitBreaker를 분리한다.
  - kgProvider
  - niceProvider
- Provider별 TimeLimiter를 분리한다.
- Provider별 Retry를 분리한다.
- Retry 대상:
  - timeout
  - 5xx
  - network error
- Retry 비대상:
  - 4xx
  - business fail
  - invalid signature
  - invalid request
- CircuitBreaker 설정 예시:
  - slidingWindowType: COUNT_BASED
  - slidingWindowSize: 50
  - minimumNumberOfCalls: 20
  - failureRateThreshold: 50
  - waitDurationInOpenState: 30s
  - permittedNumberOfCallsInHalfOpenState: 5
- TimeLimiter:
  - timeoutDuration: 2s
- Retry:
  - maxAttempts: 2
  - waitDuration: 300ms

멱등성 정책:

- 인증 요청 API는 Idempotency-Key 헤더를 받는다.
- 동일 requestId + purpose + idempotencyKey 조합으로 요청이 들어오면 기존 verification을 반환한다.
- DB unique key를 사용한다.
- 동시 요청 충돌 시 unique constraint를 기반으로 기존 데이터를 다시 조회해서 반환한다.
- Redis lock은 선택 사항으로 두되, MVP에서는 DB unique constraint 중심으로 구현한다.

requestId 생성 정책:

- requestId는 본인인증 결과로 얻는 CI나 회원 userId가 아니다.
- requestId는 인증 전 로그인/가입/회원인증 플로우를 식별하는 요청 흐름 ID다.
- 로그인처럼 userId를 모르는 플로우에서도 requestId는 인증 표준창을 열기 전에 존재해야 한다.
- requestId는 인증 플로우 시작 시 `RequestIdGenerator`로 생성한다.
- 클라이언트 또는 서버 세션은 같은 인증 플로우의 재시도 동안 동일 requestId를 유지해야 한다.
- 매 HTTP 재시도마다 requestId를 새로 생성하면 idempotency scope가 달라져 중복 인증 요청을 막을 수 없다.
- CI는 인증 결과 처리 이후 회원 DB에서 로그인/회원 매칭 용도로 사용하며, verifyhub의 verification_request에는 저장하지 않는다.

DB 테이블:

1. verification_request
   컬럼:

- id BIGINT PK AUTO_INCREMENT
- verification_id VARCHAR(64) NOT NULL UNIQUE
- request_id VARCHAR(64) NOT NULL
- purpose VARCHAR(30) NOT NULL
- idempotency_key VARCHAR(128) NOT NULL
- provider VARCHAR(20)
- status VARCHAR(30) NOT NULL
- provider_transaction_id VARCHAR(100)
- provider_request_no VARCHAR(100)
- web_transaction_id VARCHAR(100)
- routing_policy_version BIGINT
- requested_at DATETIME(6) NOT NULL
- routed_at DATETIME(6)
- provider_called_at DATETIME(6)
- completed_at DATETIME(6)
- version BIGINT NOT NULL DEFAULT 0
- created_at DATETIME(6) NOT NULL
- updated_at DATETIME(6) NOT NULL

인덱스:

- UNIQUE uk_verification_id (verification_id)
- UNIQUE uk_idempotency (request_id, purpose, idempotency_key)
- UNIQUE uk_provider_transaction (provider, provider_transaction_id)
- UNIQUE uk_provider_request_no (provider, provider_request_no)
- UNIQUE uk_provider_web_transaction (provider, web_transaction_id)
- INDEX idx_verification_status_created_at (status, created_at)
- INDEX idx_verification_provider_created_at (provider, created_at)

2. verification_history
   컬럼:

- id BIGINT PK AUTO_INCREMENT
- verification_id VARCHAR(64) NOT NULL
- from_status VARCHAR(30)
- to_status VARCHAR(30) NOT NULL
- event_type VARCHAR(50) NOT NULL
- reason VARCHAR(255)
- provider VARCHAR(20)
- raw_payload JSON
- created_at DATETIME(6) NOT NULL

인덱스:

- INDEX idx_verification_history_01 (verification_id, created_at)

3. provider_call_history
   컬럼:

- id BIGINT PK AUTO_INCREMENT
- verification_id VARCHAR(64) NOT NULL
- provider VARCHAR(20) NOT NULL
- request_payload JSON
- response_payload JSON
- http_status INT
- result_type VARCHAR(30)
- latency_ms BIGINT
- error_message VARCHAR(500)
- retry_count INT NOT NULL DEFAULT 0
- created_at DATETIME(6) NOT NULL

인덱스:

- INDEX idx_provider_call_01 (verification_id)
- INDEX idx_provider_call_02 (provider, created_at)
- INDEX idx_provider_call_03 (result_type, created_at)

4. late_callback_history
   컬럼:

- id BIGINT PK AUTO_INCREMENT
- verification_id VARCHAR(64) NOT NULL
- provider VARCHAR(20) NOT NULL
- current_status VARCHAR(30) NOT NULL
- callback_result VARCHAR(30) NOT NULL
- duplicate BOOLEAN NOT NULL DEFAULT FALSE
- raw_payload JSON
- reason VARCHAR(255)
- created_at DATETIME(6) NOT NULL

인덱스:

- INDEX idx_late_callback_01 (verification_id, created_at)
- INDEX idx_late_callback_02 (provider, created_at)

5. provider_routing_policy
   컬럼:

- id BIGINT PK AUTO_INCREMENT
- provider VARCHAR(20) NOT NULL
- weight INT NOT NULL
- enabled BOOLEAN NOT NULL
- version BIGINT NOT NULL
- created_at DATETIME(6) NOT NULL
- updated_at DATETIME(6) NOT NULL

인덱스:

- UNIQUE uk_provider_policy (provider, version)
- INDEX idx_provider_policy_01 (enabled, version)

초기 데이터:

- KG, weight 10, enabled true, version 1
- NICE, weight 90, enabled true, version 1

6. outbox_event
   컬럼:

- id BIGINT PK AUTO_INCREMENT
- aggregate_type VARCHAR(50) NOT NULL
- aggregate_id VARCHAR(64) NOT NULL
- event_type VARCHAR(100) NOT NULL
- payload JSON NOT NULL
- status VARCHAR(30) NOT NULL
- retry_count INT NOT NULL DEFAULT 0
- created_at DATETIME(6) NOT NULL
- published_at DATETIME(6)

인덱스:

- INDEX idx_outbox_01 (status, created_at)
- INDEX idx_outbox_02 (aggregate_id, created_at)

API 설계:

1. 인증 요청 생성
   POST /api/v1/verifications
   Header:

- Idempotency-Key: signup-req_123-20260425

Request:
{
"requestId": "req_8f3f0d3c4d284a4d9f1a43f9c7a2c001",
"name": "홍길동",
"phoneNumber": "01012345678",
"birthDate": "19900101",
"purpose": "SIGN_UP"
}

Response:
{
"verificationId": "verif_01HX...",
"status": "IN_PROGRESS",
"provider": "NICE"
}

처리 흐름:

1. 요청 validation
2. Idempotency-Key 확인
3. 기존 요청 존재 시 기존 결과 반환
4. verification 생성, status REQUESTED
5. history 기록
6. provider routing
7. status ROUTED 전이
8. provider call 시작
9. status IN_PROGRESS 전이
10. provider adapter 호출
11. provider 호출 결과 저장
12. provider가 즉시 성공/실패를 반환하는 mock scenario면 상태 반영
13. NICE 표준창 기반 scenario면 authUrl 반환 후 IN_PROGRESS 유지
14. outbox event 저장

15. 인증 상태 조회
    GET /api/v1/verifications/{verificationId}

Response:
{
"verificationId": "verif_01HX...",
"status": "SUCCESS",
"provider": "NICE",
"purpose": "SIGN_UP",
"requestedAt": "2026-04-25T12:00:00",
"completedAt": "2026-04-25T12:00:03"
}

3. 인증 이력 조회
   GET /api/v1/verifications/{verificationId}/histories

Response:
{
"verificationId": "verif_01HX...",
"histories": [
{
"fromStatus": null,
"toStatus": "REQUESTED",
"eventType": "VERIFICATION_REQUESTED",
"reason": "verification requested",
"createdAt": "..."
}
]
}

4. Provider Return URL 수신
   GET 또는 POST /api/v1/providers/{provider}/returns

Request:
{
"verificationId": "verif_01HX...",
"webTransactionId": "ZGIxOGZkYjUtMjE4NC00MDZmLTkxZjgtM2ZhNjA0OTdiZTY2"
}

처리 흐름:

1. provider path variable 검증
2. verification 조회
3. webTransactionId 중복 여부 확인
4. 현재 상태가 terminal이면 late_callback_history 저장 후 200 반환
5. IN_PROGRESS 상태면 provider adapter를 통해 인증 결과 요청
6. NICE 응답의 integrity_value 검증
7. enc_data 복호화
8. 인증 결과에 따라 SUCCESS 또는 FAIL 전이
9. history 저장
10. outbox event 저장
11. metrics 증가

5. 관리자 재처리
    POST /admin/v1/verifications/{verificationId}/retry

정책:

- TIMEOUT 또는 retryable FAIL만 재처리 가능
- SUCCESS, CANCELED는 재처리 불가
- 재처리는 기존 verification_id를 유지하지 말고 retry attempt history를 남긴다.
- MVP에서는 단순히 같은 verification_id에 대해 provider call을 다시 수행하되, history에 ADMIN_RETRY_REQUESTED 이벤트를 남긴다.

6. 라우팅 정책 조회
   GET /admin/v1/routing-policies

7. 라우팅 정책 변경
   PUT /admin/v1/routing-policies

Request:
{
"policies": [
{
"provider": "KG",
"weight": 50,
"enabled": true
},
{
"provider": "NICE",
"weight": 50,
"enabled": true
}
]
}

정책:

- 변경 시 version을 증가시킨다.
- 기존 policy row를 update하지 말고 새 version row를 insert한다.
- Verification에는 선택 당시 policyVersion을 저장한다.

8. Mock Provider Scenario 변경
   POST /mock/providers/{provider}/scenario

Request:
{
"scenario": "SUCCESS"
}

지원 scenario:

- SUCCESS
- FAIL
- TIMEOUT
- HTTP_500
- DELAYED_RETURN
- DUPLICATE_RETURN
- INVALID_INTEGRITY_RESULT

Mock Provider 동작:

- 인증 요청 endpoint:
  - POST /mock/providers/{provider}/verifications
- SUCCESS: 즉시 성공 응답
- FAIL: 즉시 실패 응답
- TIMEOUT: 2초 이상 지연하여 TimeLimiter 유도
- HTTP_500: 500 응답
- DELAYED_RETURN: 최초 요청은 authUrl accepted로 받고, return_url 수신 또는 결과 조회를 지연
- DUPLICATE_RETURN: 같은 webTransactionId return을 2번 발생
- INVALID_INTEGRITY_RESULT: 결과 조회 응답의 integrity_value 검증 실패를 시뮬레이션

도메인 클래스 설계:

Verification:
필드:

- id
- verificationId
- requestId
- purpose
- idempotencyKey
- provider
- status
- providerTransactionId
- providerRequestNo
- providerAuthUrl
- webTransactionId
- routingPolicyVersion
- requestedAt
- routedAt
- providerCalledAt
- completedAt
- version

메서드:

- routeTo(provider, policyVersion, now)
- startProviderCall(now)
- completeSuccess(providerTransactionId, now)
- completeFail(providerTransactionId, reason, now)
- timeout(reason, now)
- cancel(reason, now)
- isTerminal()
- assertTransitionAllowed(event)

중요:

- 상태 변경 로직은 VerificationStateMachine 또는 VerificationStateService를 통해서만 수행한다.
- Entity setter를 열어두지 않는다.
- JPA entity와 domain model을 분리해도 되고, MVP에서는 JPA entity를 domain으로 사용해도 된다. 단 상태 변경 메서드는 명시적으로 둔다.

StateMachine:
class VerificationStateMachine {
VerificationStatus transit(VerificationStatus current, VerificationEvent event)
}

전이 실패 시:

- InvalidStateTransitionException 발생
- history에는 실패한 전이를 저장하지 않는다.
- 단 late return/result는 별도 저장한다.

Provider Port:

interface ProviderClientPort {
ProviderType providerType();
ProviderRequestResult requestVerification(ProviderRequest request);
ProviderResult requestResult(ProviderResultRequest request);
}

ProviderRequest:

- verificationId
- requestId
- name
- phoneNumber
- birthDate
- purpose

ProviderRequestResult:

- provider
- providerTransactionId
- providerRequestNo
- authUrl
  - Provider별 인증 진입 URL이다.
  - DB 영속 필드로 표준화하지 않고 API 응답 및 masked provider call history 중심으로 다룬다.
- resultType
  - ACCEPTED
  - SUCCESS
  - FAIL
  - TIMEOUT
  - ERROR
- rawResponse
- httpStatus
- latencyMs
- errorMessage

ProviderResultRequest:

- provider
- providerTransactionId
- providerRequestNo
- verificationId
- webTransactionId

ProviderResult:

- provider
- providerTransactionId
- verificationId
- result
- integrityVerified
- rawPayload

Routing Strategy:

interface ProviderRoutingStrategy {
RoutingDecision select(RoutingContext context);
}

RoutingContext:

- request purpose
- enabled policies
- provider health
- circuit breaker states

WeightedProviderRoutingStrategy:

- enabled=true인 policy만 후보
- circuit breaker OPEN provider 제외
- weight 총합 계산
- random number 기반 provider 선택
- 선택 이유를 RoutingDecision에 포함

Repository Port:

- save
- findByVerificationId
- findByRequestIdAndPurposeAndIdempotencyKey
- findForUpdateByVerificationId는 선택 사항
- optimistic lock 사용

동시성 정책:

- verification_request.version에 @Version 적용
- callback 중복 처리 시 optimistic lock 충돌이 발생하면 재조회 후 terminal 여부를 확인한다.
- 중복 콜백으로 인한 상태 오염을 막는다.
- 동일 Idempotency-Key 중복 요청은 unique constraint로 방어한다.

예외 설계:
Common exceptions:

- VerificationNotFoundException
- DuplicateVerificationRequestException
- InvalidStateTransitionException
- TerminalStateAlreadyReachedException
- ProviderUnavailableException
- ProviderCallFailedException
- ProviderTimeoutException
- InvalidCallbackSignatureException
- RetryNotAllowedException

Error response:
{
"code": "PROVIDER_UNAVAILABLE",
"message": "No available verification provider",
"traceId": "..."
}

Validation:

- requestId not blank
- name not blank
- phoneNumber regex
- birthDate yyyyMMdd
- purpose enum
- Idempotency-Key required

운영 지표:
Micrometer Counter/Gauge/Timer를 사용한다.

Metrics:

- verification.request.count
- verification.success.count
- verification.fail.count
- verification.timeout.count
- verification.status.count
- verification.late_callback.count
- verification.duplicate_callback.count
- provider.request.count
- provider.success.count
- provider.fail.count
- provider.timeout.count
- provider.latency
- provider.circuit.state

Actuator:

- /actuator/health
- /actuator/metrics
- /actuator/prometheus

application.yml 예시:
server:
port: 8080

spring:
datasource:
url: jdbc:mysql://localhost:3306/verifyhub
username: verifyhub
password: verifyhub
jpa:
hibernate:
ddl-auto: validate
properties:
hibernate:
format_sql: true
flyway:
enabled: true

management:
endpoints:
web:
exposure:
include: health,info,metrics,prometheus
endpoint:
health:
show-details: always

resilience4j:
circuitbreaker:
instances:
kgProvider:
slidingWindowType: COUNT_BASED
slidingWindowSize: 50
minimumNumberOfCalls: 20
failureRateThreshold: 50
waitDurationInOpenState: 30s
permittedNumberOfCallsInHalfOpenState: 5
niceProvider:
slidingWindowType: COUNT_BASED
slidingWindowSize: 50
minimumNumberOfCalls: 20
failureRateThreshold: 50
waitDurationInOpenState: 30s
permittedNumberOfCallsInHalfOpenState: 5
timelimiter:
instances:
kgProvider:
timeoutDuration: 2s
niceProvider:
timeoutDuration: 2s
retry:
instances:
kgProvider:
maxAttempts: 2
waitDuration: 300ms
niceProvider:
maxAttempts: 2
waitDuration: 300ms

Docker Compose:

- mysql:8.0
- redis:7
- prometheus
- grafana는 선택

docker-compose.yml 요구사항:

- MySQL database verifyhub 생성
- username/password verifyhub
- Redis 6379
- Prometheus가 /actuator/prometheus scrape

Flyway 초기 스키마:
V1\_\_create_verification_tables.sql

- 신규 verifyhub 서비스의 최초 DB 스키마를 버전 관리하기 위해 위 DB 테이블을 생성한다.
  V2\_\_insert_initial_provider_routing_policy.sql
- KG/NICE 초기 정책 insert

구현 순서:

1. Gradle 프로젝트 생성
2. 기본 패키지 구조 생성
3. 공통 응답/예외/시간 provider 구현
4. VerificationStatus, VerificationEvent, ProviderType, Purpose enum 구현
5. VerificationStateMachine 구현 및 단위 테스트 작성
6. JPA Entity 및 Repository 구현
7. Flyway 초기 스키마 작성
8. VerificationCommandService 구현
9. IdempotencyService 구현
10. RoutingPolicy Repository 구현
11. WeightedProviderRoutingStrategy 구현 및 테스트
12. Mock Provider Client 구현
13. Resilience4j 적용
14. Provider call history 저장
15. ProviderResultApplicationService 구현
16. Late return/result 정책 구현
17. REST Controller 구현
18. Admin API 구현
19. Metrics 구현
20. Testcontainers 기반 통합 테스트 작성
21. README 작성
22. docker-compose 작성

테스트 요구사항:

단위 테스트:

1. StateMachineTest

- REQUESTED -> ROUTED 성공
- ROUTED -> IN_PROGRESS 성공
- IN_PROGRESS -> SUCCESS 성공
- IN_PROGRESS -> FAIL 성공
- IN_PROGRESS -> TIMEOUT 성공
- SUCCESS -> FAIL 실패
- TIMEOUT -> SUCCESS 실패
- REQUESTED -> SUCCESS 실패

2. WeightedProviderRoutingStrategyTest

- KG 10 / NICE 90 가중치에서 후보 선택 가능
- disabled provider 제외
- circuit open provider 제외
- 후보가 없으면 ProviderUnavailableException

3. IdempotencyServiceTest

- 동일 idempotency key 요청 시 기존 verification 반환
- 다른 idempotency key 요청 시 신규 생성

4. ProviderResultApplicationServiceTest

- IN_PROGRESS 상태에서 SUCCESS result 조회 시 SUCCESS 전이
- IN_PROGRESS 상태에서 FAIL result 조회 시 FAIL 전이
- TIMEOUT 상태에서 SUCCESS result 조회 시 상태 변경하지 않고 late history 저장
- duplicate return/result 수신 시 상태 변경하지 않고 duplicate 기록
- integrity 검증 실패면 상태 오염 없이 실패 이력 기록

통합 테스트:

1. VerificationFlowIntegrationTest

- 인증 요청 생성 -> provider 선택 -> IN_PROGRESS 또는 SUCCESS 상태 확인
- history 저장 확인
- provider call history 저장 확인

2. TimeoutFlowIntegrationTest

- Mock Provider TIMEOUT scenario 설정
- 인증 요청
- TIMEOUT 상태 전이 확인
- provider timeout metrics 증가 확인

3. CircuitBreakerIntegrationTest

- Provider 500 에러 반복
- CircuitBreaker OPEN 확인
- 해당 provider routing 제외 확인

4. IdempotencyIntegrationTest

- 같은 Idempotency-Key로 2번 요청
- verification_request 1건만 생성 확인

5. LateCallbackIntegrationTest

- TIMEOUT 상태 생성
- 이후 SUCCESS result 조회
- verification status는 TIMEOUT 유지
- late_callback_history 저장 확인

README에 포함할 내용:

- 프로젝트 목적
- 전체 아키텍처
- 상태 머신
- Provider 라우팅 정책
- Retry/Timeout/CircuitBreaker 정책
- 멱등성 정책
- Late Callback 처리 정책
- DB ERD 요약
- API 사용 예시
- Mock Provider scenario 사용법
- Prometheus metrics 확인 방법
- 실행 방법
- 테스트 실행 방법
- 향후 확장 방향

README의 포트폴리오용 설명:
"Server-to-Server 본인인증 환경에서 KG/NICE 등 복수 연동사를 안정적으로 호출하기 위한 본인인증 오케스트레이터를 설계하고 구현했습니다. Provider Adapter, 가중치 기반 Routing Strategy, 상태 머신, Timeout/Retry/CircuitBreaker를 적용하여 외부 연동 장애 상황에서도 인증 요청 상태를 정합성 있게 관리할 수 있도록 구성했습니다. 또한 Idempotency-Key와 Optimistic Lock 기반 중복 요청 방지, 종료 상태 이후 유입되는 늦은 Callback 기록 정책, Outbox 기반 이벤트 발행 구조를 설계하여 운영 중 추적 가능성과 복구 가능성을 높였습니다."

주의사항:

- 실제 KG/NICE API를 호출하지 않는다.
- 개인정보는 저장하지 않는 방향으로 구현한다. MVP에서는 name, phoneNumber, birthDate를 request DTO로만 받고 DB에는 저장하지 않는다.
- raw_payload에 개인정보가 들어가지 않도록 masking 처리한다.
- 전화번호는 로그에 남길 때 가운데 자리를 마스킹한다.
- Controller에서 비즈니스 로직을 구현하지 않는다.
- 상태 변경은 반드시 VerificationStateService 또는 StateMachine을 거친다.
- Provider별 장애 정책은 하드코딩하지 말고 config 기반으로 둔다.
- 테스트 가능한 구조로 interface를 먼저 정의한다.
- 처음부터 과한 MSA로 나누지 말고 모듈형 모놀리스로 구현한다.
- 모든 주요 결정은 README의 "Design Decisions" 섹션에 남긴다.

추가 확장 과제:

1. Outbox Relay 구현

- PENDING outbox_event를 주기적으로 조회
- publish 성공 시 PUBLISHED 처리
- 실패 시 retry_count 증가

2. AI 운영 분석 기능은 MVP 이후에 추가

- 최근 30분 provider 실패율/timeout/late callback을 요약
- 운영자용 장애 리포트 생성
- Slack 알림 문장 생성

3. 관리자 대시보드

- provider별 성공률
- provider별 timeout rate
- provider별 p95 latency
- late callback count
- circuit breaker state
