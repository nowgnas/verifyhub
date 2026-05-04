# VH-023 Provider Return URL 및 Result Retrieval 설계

## 목적

provider 인증창 또는 provider callback에서 돌아온 return 요청을 받아 인증 결과를 조회하고, verifyhub의 인증 상태를 `SUCCESS` 또는 `FAIL`로 마무리한다.

## 접근

MVP에서는 provider별 실제 암복호화 세부 구현을 controller에 넣지 않는다. `GET/POST /api/v1/providers/{provider}/returns`가 공통 command를 만들고, `ProviderReturnService`가 application layer에서 다음 흐름을 수행한다.

1. `verificationId`로 인증 요청을 조회한다.
2. path provider와 저장된 verification provider가 일치하는지 검증한다.
3. `webTransactionId`를 verification에 저장한다.
4. 저장된 `providerTransactionId`, `providerRequestNo`, `webTransactionId`로 `ProviderClientPort.requestResult(...)`를 호출한다.
5. provider result가 `SUCCESS`이고 integrity 검증이 통과하면 verification을 `SUCCESS`로 전이한다.
6. provider result가 `FAIL`이거나 integrity 검증이 실패하면 verification을 `FAIL`로 전이한다.
7. terminal 전이 결과에 따라 outbox event를 저장한다.

## API

### GET /api/v1/providers/{provider}/returns

Query parameters:

- `verificationId`
- `webTransactionId`

### POST /api/v1/providers/{provider}/returns

Body:

```json
{
  "verificationId": "verif_...",
  "webTransactionId": "web-tx-..."
}
```

Response:

```json
{
  "data": {
    "verificationId": "verif_...",
    "provider": "NICE",
    "status": "SUCCESS",
    "result": "SUCCESS",
    "integrityVerified": true
  }
}
```

## 범위 제한

- NICE `enc_data` 복호화와 실제 `integrity_value` 검증 구현은 provider adapter 내부 책임으로 남긴다.
- terminal 상태 이후 late/duplicate return 정책은 `VH-024`에서 구현한다.
- provider별 KG Notiurl 특화 payload는 후속 작업에서 별도 DTO로 확장한다.

## 테스트

- `ProviderReturnServiceTest`
  - `IN_PROGRESS` verification + successful provider result -> `SUCCESS`, outbox 저장
  - provider path mismatch -> `INVALID_REQUEST`
  - integrity 검증 실패 -> `FAIL`
- `ProviderReturnControllerTest`
  - GET return endpoint
  - POST return endpoint
  - validation 실패
