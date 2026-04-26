# VH-018/VH-019 Provider Flow Work Summary

## Scope

VH-018/VH-019는 VH-017에서 만든 provider HTTP client를 실제 verification application 흐름에 연결하는 작업이다.

- VH-018: provider client 호출에 Resilience4j decorator 적용
- VH-019: routing 결과로 provider를 선택하고 provider 호출, 상태 전이, provider call history 저장을 하나의 application flow로 구성

## VH-018. Resilience4j Decorator

추가한 class:

- `ProviderClientResilienceDecorator`

역할:

- provider별 Resilience4j instance 이름을 사용한다.
  - KG -> `kgProvider`
  - NICE -> `niceProvider`
- `CircuitBreakerRegistry`
- `RetryRegistry`
- `TimeLimiterRegistry`
- `ExecutorService`

적용 대상:

- `requestVerification(...)`
- `requestResult(...)`

예외 변환:

- TimeLimiter timeout -> `ProviderTimeoutException`
- 그 외 provider 호출 실패 -> `ProviderCallFailedException`

현재 retry 횟수는 `ProviderRequestResult`에 직접 반영하지 않는다. retry count를 provider call history에 정확히 저장하려면 Retry event listener 기반 후속 작업이 필요하다.

## VH-019. Provider Verification Flow

추가한 application service:

- `ProviderVerificationFlowService`

흐름:

1. `ProviderRoutingService.route(...)`로 provider 선택
2. `VerificationStateService.routeTo(...)`로 `REQUESTED -> ROUTED`
3. 선택된 `ProviderClientPort` 조회
4. `ProviderClientResilienceDecorator.requestVerification(...)`로 provider 호출
5. `ProviderCallHistoryService.record(...)`로 provider 호출 이력 저장
6. `VerificationStateService.startProviderCall(...)`로 `ROUTED -> IN_PROGRESS`
7. provider request result가 terminal이면 상태를 완료 처리
   - `SUCCESS` -> `SUCCESS`
   - `FAIL` -> `FAIL`
   - `TIMEOUT` -> `TIMEOUT`
   - `ACCEPTED` 또는 `ERROR` -> 현재는 `IN_PROGRESS` 유지

추가한 command/result:

- `ProviderVerificationCommand`
- `ProviderVerificationResult`

## Current Boundary

이번 작업은 application flow의 뼈대를 만든다. 아직 아래 항목은 포함하지 않는다.

- 외부 inbound verification API와 연결
- `mockprovider` HTTP endpoint 구현
- retry count를 provider call history에 정확히 반영
- provider `ERROR` 결과를 즉시 실패로 볼지, 별도 재처리 대상으로 둘지에 대한 정책 확정
- NICE 실제 token/url/result 세부 프로토콜 구현

## Tests

추가한 테스트:

- `ProviderClientResilienceDecoratorTest`
  - 일시 실패 후 retry로 provider verification 호출 성공 검증
- `ProviderVerificationFlowServiceTest`
  - routing decision 기반 provider 선택
  - provider request 생성
  - resilience decorator를 통한 provider 호출
  - provider call history 저장
  - `ROUTED -> IN_PROGRESS` 상태 전이 호출
  - `authUrl` 포함 결과 반환

## Verification

```bash
rtk ./gradlew test --tests com.verifyhub.verification.application.ProviderVerificationFlowServiceTest --tests com.verifyhub.verification.adapter.out.provider.ProviderClientResilienceDecoratorTest --no-daemon
```

결과: `BUILD SUCCESSFUL`
