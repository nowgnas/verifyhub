# VH-028 Internal Mock Provider API Design

## Context

MVP에서는 실제 KG/NICE 서버를 호출하지 않고 같은 Spring Boot 애플리케이션 내부의 mock provider API를 외부 provider처럼 호출한다. 기존 구현은 NICE/KG 형태의 일부 endpoint만 있었고, provider별 scenario를 바꾸는 API와 공통 verification/result endpoint가 없었다.

## API

### POST /mock/providers/{provider}/scenario

provider별 다음 mock 동작을 설정한다.

Request:

```json
{
  "scenario": "INVALID_INTEGRITY_RESULT"
}
```

Response:

```json
{
  "provider": "NICE",
  "scenario": "INVALID_INTEGRITY_RESULT"
}
```

지원 scenario:

- `SUCCESS`
- `FAIL`
- `TIMEOUT`
- `HTTP_500`
- `DELAYED_RETURN`
- `DUPLICATE_RETURN`
- `INVALID_INTEGRITY_RESULT`

### POST /mock/providers/{provider}/verifications

공통 provider verification mock endpoint다. `MockProviderHttpClient`의 기본 verification endpoint 계약을 따른다.

### POST /mock/providers/{provider}/results

공통 provider result mock endpoint다. provider return flow에서 `ProviderClientPort.requestResult(...)`가 호출하는 endpoint다.

### GET/POST /mock/providers/{provider}/returns

수동 테스트와 후속 프론트 콘솔에서 provider return scenario 상태를 확인하거나 호출할 수 있는 mock endpoint다.

## Scenario Behavior

- `SUCCESS`: 인증 시작은 `ACCEPTED`, 결과 조회는 `SUCCESS + integrityVerified=true`.
- `FAIL`: KG/common 인증 시작은 `FAIL`, 결과 조회는 `FAIL`.
- `TIMEOUT`: 2초 time limiter보다 길게 지연해 resilience timeout 검증에 사용할 수 있게 한다.
- `HTTP_500`: mock provider가 HTTP 500을 반환한다.
- `INVALID_INTEGRITY_RESULT`: 결과 조회는 `SUCCESS`지만 `integrityVerified=false`.
- `DELAYED_RETURN`, `DUPLICATE_RETURN`: scenario 상태로 노출하며, 후속 frontend/E2E에서 return 호출 버튼과 결합한다.

## Boundaries

- mock provider는 verification application service를 직접 호출하지 않는다.
- scenario 상태는 in-memory로 유지한다.
- 실제 callback 자동 발송은 이번 범위에 넣지 않는다.
