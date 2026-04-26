# VH-017 Provider Client Work Summary

## Scope

VH-017은 verification application이 외부 본인인증 provider를 직접 알지 않도록 provider outbound port와 HTTP client adapter를 추가하는 작업이다.

MVP에서는 실제 KG/NICE API를 호출하지 않는다. 같은 Spring Boot 애플리케이션 안에 구현될 `mockprovider` HTTP API를 외부 provider처럼 호출한다.

## Added Port

추가한 outbound port:

- `ProviderClientPort`
  - `providerType()`
  - `requestVerification(ProviderRequest request)`
  - `requestResult(ProviderResultRequest request)`

이 port는 이후 인증 요청 흐름에서 routing 결과로 선택된 provider client를 호출하는 진입점으로 사용한다.

## Added Domain DTOs

추가한 record/value object:

- `ProviderRequest`
  - `verificationId`
  - `requestId`
  - `name`
  - `phoneNumber`
  - `birthDate`
  - `purpose`
- `ProviderRequestResult`
  - `provider`
  - `providerTransactionId`
  - `providerRequestNo`
  - `authUrl`
  - `resultType`
  - `rawResponse`
  - `httpStatus`
  - `latencyMs`
  - `errorMessage`
- `ProviderResultRequest`
  - `provider`
  - `providerTransactionId`
  - `providerRequestNo`
  - `verificationId`
  - `webTransactionId`
- `ProviderResult`
  - `provider`
  - `providerTransactionId`
  - `verificationId`
  - `result`
  - `integrityVerified`
  - `rawPayload`

추가한 enum:

- `ProviderRequestResultType`
  - `ACCEPTED`
  - `SUCCESS`
  - `FAIL`
  - `TIMEOUT`
  - `ERROR`
- `ProviderResultStatus`
  - `SUCCESS`
  - `FAIL`

## Added HTTP Adapters

공통 HTTP client:

- `MockProviderHttpClient`
  - `POST {baseUrl}/verifications`
  - `POST {baseUrl}/results`
  - provider 응답을 domain result로 변환
  - request verification 호출의 HTTP status, raw response, latency를 수집
  - request verification 호출 중 provider HTTP error는 `ProviderRequestResultType.ERROR`로 변환

provider별 adapter:

- `KgProviderClient`
  - `verifyhub.provider.kg.base-url` 사용
  - `ProviderType.KG` 반환
- `NiceProviderClient`
  - `verifyhub.provider.nice.base-url` 사용
  - `ProviderType.NICE` 반환

KG/NICE adapter는 패키지를 분리해 관리한다.

## Deferred Work

VH-017은 provider HTTP 호출 골격까지만 구현한다. 아래 항목은 후속 티켓에서 처리한다.

- `mockprovider` HTTP endpoint 구현
- routing 결과로 provider client를 선택하고 verification flow에 연결
- provider call history 저장 연동
- Resilience4j CircuitBreaker, Retry, TimeLimiter decorator 연결
- NICE 실제 API의 token/url/result 상세 프로토콜 매핑
- `authUrl`을 API 응답으로 반환하는 verification command flow 구현

## Tests

추가한 테스트:

- `KgProviderClientTest`
  - `/verifications` 호출 request body와 response mapping 검증
  - `/results` 호출 request body와 response mapping 검증
- `NiceProviderClientTest`
  - NICE base URL을 사용해 `/verifications` 호출
  - `ProviderType.NICE` mapping 검증

## Verification

```bash
rtk ./gradlew test --tests com.verifyhub.verification.adapter.out.provider.kg.KgProviderClientTest --tests com.verifyhub.verification.adapter.out.provider.nice.NiceProviderClientTest --no-daemon
```

결과: `BUILD SUCCESSFUL`
