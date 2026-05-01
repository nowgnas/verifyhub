# VH-020 Phone Auth Guide Gap Analysis

## 목적

VH-020 인증 요청 생성 API를 구현하기 전에 `docs/guide/phone-auth`의 KG/NICE 본인인증 S2S 문서와 현재 verifyhub 개발 방향의 차이를 정리한다.

분석 대상:

- `docs/guide/phone-auth/phone-auth-s2s-architecture.md`
- `docs/guide/phone-auth/phone-identity-verification-analysis.md`
- `docs/guide/phone-auth/ui-bff-phone-auth-flow.md`
- `docs/guide/phone-auth/OpenAPI Specification.json`
- `docs/guide/phone-auth/kg.pdf`
- `docs/guide/phone-auth/nice.pdf`

## 결론

현재 verifyhub 방향은 큰 틀에서 맞다.

- Client가 provider 팝업 또는 표준창을 연다.
- verifyhub가 provider 선택, provider 초기화, 상태 전이, 이력, return/result 처리를 담당한다.
- Client가 CI, 이름, 생년월일, 휴대폰번호 같은 인증 결과 원문을 직접 서버에 보내지 않도록 한다.
- KG/NICE provider adapter를 패키지로 분리한다.

다만 현재 구현은 아직 mock provider 중심의 공통 JSON API 형태다. 실제 KG/NICE S2S 가이드와 맞추려면 VH-020 이후 provider별 init/result/noti 책임을 더 구체화해야 한다.

## 현재 회사/가이드 플로우 요약

### 기존 BFF/UI 방식

기존 BFF는 NICE/KG API를 직접 호출하지 않는다. UI가 인증사 팝업/SDK와 직접 통신하고, 인증 결과로 받은 CI/이름/생년월일/휴대폰번호를 BFF에 전달한다. BFF는 인증사 선택, CI 비교, Redis 인증 토큰 발급, 회원정보 갱신을 담당한다.

이 방식은 서버가 인증 결과를 provider에서 직접 조회하거나 복호화하지 않으므로, S2S 전환 목적과는 맞지 않는다.

### S2S 목표 방식

S2S 문서는 다음 방향을 제안한다.

- `/init`: 서버가 provider별 인증 진입 URL을 만들고 Client에 반환한다.
- Client는 반환된 `authUrl` 또는 KG popup URL을 연다.
- `/result` 또는 Notiurl: 서버가 provider 결과를 직접 조회/수신하고 복호화한다.
- 서버가 CI 검증, 인증 토큰 발급, 회원 DB 연동을 수행한다.

verifyhub는 회원 DB를 직접 소유하지 않는 오케스트레이터이므로 CI 저장/회원 토큰 발급은 현재 프로젝트 범위 밖이다. 대신 인증 성공 결과를 외부 consumer가 사용할 수 있는 형태로 반환하거나 이벤트화하는 방향이 맞다.

## NICE 가이드와 현재 설계 비교

### NICE 실제 흐름

NICE OpenAPI 기준 흐름:

1. `POST /ido/intc/v1.0/auth/token`
   - `Authorization: Basic base64url(client_id:client_secret)`
   - body: `grant_type=client_credentials`, `request_no`
   - response: `access_token`, `expires_in`, `ticket`, `iterators`
2. `POST /ido/intc/v1.0/auth/url`
   - `Authorization: Bearer access_token`
   - body 필수: `request_no`, `return_url`, `svc_types`
   - 선택: `close_url`, `method_type`, `exp_mods`
   - response: `auth_url`, `transaction_id`, `request_no`
3. Client가 `auth_url`로 표준창을 연다.
4. NICE 표준창이 `return_url`로 `web_transaction_id`를 전달한다.
5. `POST /ido/intc/v1.0/auth/result`
   - `Authorization: Bearer access_token`
   - body 필수: `request_no`, `transaction_id`, `web_transaction_id`
   - response: `enc_data`, `integrity_value`
6. 서버가 `ticket + transaction_id + iterators`로 KDF 키를 만들고, `integrity_value` 검증 후 `enc_data`를 AES/GCM으로 복호화한다.

### 현재 설계와 맞는 부분

- `provider_transaction_id`를 NICE `transaction_id`로 매핑하는 방향은 적절하다.
- `provider_request_no`를 NICE `request_no`로 매핑하는 방향은 적절하다.
- `web_transaction_id` 저장 컬럼을 둔 것은 return/result correlation에 필요하다.
- `authUrl`을 표준 DB 필드로 저장하지 않고 API 응답/이력 중심으로 다루는 방향은 적절하다.
- `return_url` 수신 후 결과 조회 API를 호출한다는 설계 방향은 맞다.

### 차이점

- 현재 `ProviderRequest`는 `name`, `phoneNumber`, `birthDate`를 provider init에 전달한다. NICE `/auth/url`에는 이 개인정보가 필요하지 않다.
- NICE는 token 발급 결과의 `ticket`, `iterators`, `access_token`이 결과 복호화에 필요하다. 현재 aggregate에는 `transaction_id`, `request_no`, `web_transaction_id`만 있고 `ticket`, `iterators`, token cache 개념은 없다.
- NICE `request_no`는 최소 20byte에서 최대 50byte 제약이 있다. 현재 verifyhub의 `requestId`와 provider request number 생성 정책이 분리되어 있지 않다.
- 현재 `MockProviderHttpClient`는 `POST /verifications`, `POST /results`의 공통 JSON API를 가정한다. 실제 NICE는 `/auth/token`, `/auth/url`, `/auth/result` 3단계다.
- `ProviderResult`는 `SUCCESS/FAIL`과 `integrityVerified`만 표현한다. 실제 결과에는 CI, DI, 이름, 생년월일, 성별, 내외국인, 통신사, 휴대폰번호, 연령코드 등 provider-auth result payload가 필요하다.

## KG 가이드와 현재 설계 비교

### KG 실제 흐름

문서 기준 KG는 NICE처럼 `/auth/result`를 다시 조회하는 표준 API가 명확한 구조가 아니다.

KG 방향:

- init 시 서버가 KG popup/form 파라미터를 만든다.
  - `Siteurl`
  - `Tradeid`
  - `Notiurl`
  - `Okurl`
  - `CALL_TYPE`
  - `Sendtype`
  - `CI_SVCID`
  - 기타 KG 암호화/서명 파라미터
- Client는 KG 인증창을 연다.
- KG는 인증 완료 후 서버의 `Notiurl`로 암호화된 결과를 POST한다.
- 서버는 `KgCryptoHelper`로 복호화/검증 후 결과를 저장한다.
- 필요하면 Client는 별도 result API로 처리 완료 상태를 조회한다.

### 현재 설계와 맞는 부분

- `verification.adapter.out.provider.kg` 패키지를 NICE와 분리한 것은 맞다.
- provider별 adapter 내부에서 다른 프로토콜을 감싸고 공통 `ProviderRequestResult`/`ProviderResult`로 올리는 방향은 맞다.
- `web_transaction_id`와 `provider_request_no` 같은 correlation key를 둔 것은 KG `Tradeid`류 식별자에도 확장 가능하다.

### 차이점

- KG는 결과 조회보다 `Notiurl` server callback이 중심이다. 현재 설계의 `/returns` + `/results` 흐름은 NICE에는 잘 맞지만 KG에는 그대로 맞지 않는다.
- KG init은 JSON POST 하나가 아니라 popup/form 파라미터 생성이 핵심이다. 현재 `authUrl` 하나만 반환하는 모델로는 KG의 form fields, charset, method, target URL을 충분히 표현하기 어렵다.
- KG는 `Tradeid`가 핵심 transaction key다. 현재 `providerTransactionId`와 `providerRequestNo` 중 어느 필드가 `Tradeid`인지 명확히 정해야 한다.
- KG 암복호화는 NICE AES/GCM과 다르다. 문서상 KG DLL 또는 Java 포팅, 가맹점 고유 KEY와 `CI_SVCID` 기반 key 생성이 필요하다.
- KG 인증창은 기존 UI에서 `euc-kr` charset submit 이슈가 있다. 단순 URL open 방식으로만 설계하면 실제 UI 연동에서 깨질 수 있다.

## 주요 설계 차이 요약

| 영역 | 현재 verifyhub | 가이드 기반 필요 방향 |
| --- | --- | --- |
| provider init | 공통 `ProviderRequest`로 이름/전화/생년월일 전달 | init은 provider별 요청. NICE는 returnUrl/svcTypes/requestNo, KG는 popup/form 파라미터 중심 |
| authUrl 응답 | `authUrl` 문자열 중심 | `authUrl` 외에 method, form fields, charset, popup target 표현 필요 |
| NICE token | 없음 | access token cache, ticket, iterators 저장 필요 |
| NICE result | 공통 `/results` mock 호출 | `/auth/result` 호출 후 HMAC 검증 + AES/GCM 복호화 |
| KG result | 공통 `/results` mock 호출 | Notiurl POST 수신 + 복호화/검증 + 상태 저장 |
| transaction store | DB aggregate에 일부 correlation 저장 | DB에는 감사/조회용 correlation, Redis에는 10분 TTL provider transaction/session 저장 권장 |
| provider code | enum `KG`, `NICE` | 회사 호환 API에는 `"10"` NICE, `"20"` KG 매핑 필요 |
| routing weight | 초기 KG 10 / NICE 90 | 회사 기본값은 NICE 85 / KG 15. DB 정책으로 조정 가능 |
| CI 처리 | 현재 프로젝트는 저장하지 않음 | 인증 완료 응답 또는 이벤트에 CI를 포함할지 정책 필요. DB 저장은 consumer 책임으로 유지 가능 |

## VH-020 전에 변경을 권장하는 방향

### 1. API 응답 모델을 `authUrl` 단일 문자열에서 `ProviderAuthEntry`로 확장

VH-020 인증 요청 생성 API 응답은 provider 진입 방식을 표현해야 한다.

권장 모델:

```java
public record ProviderAuthEntry(
        ProviderType provider,
        AuthEntryType type,
        String url,
        String method,
        String charset,
        Map<String, String> fields
) {
}
```

예시:

- NICE: `type=REDIRECT_URL`, `method=GET`, `url=auth_url`
- KG: `type=FORM_POST`, `method=POST`, `charset=EUC-KR`, `url=https://auth.mobilians.co.kr/goCashMain.mcash`, `fields=KG 파라미터`

현재 `ProviderVerificationResult.authUrl`은 임시 모델로 유지할 수 있지만, VH-020 API에서는 확장 응답을 준비하는 편이 낫다.

### 2. `ProviderRequest`를 실제 init 요구사항에 맞게 정리

현재 `ProviderRequest`의 개인정보 필드는 NICE init에는 맞지 않는다. 아래처럼 목적을 분리하는 편이 낫다.

- `ProviderVerificationInitRequest`
  - `verificationId`
  - `requestId`
  - `providerRequestNo`
  - `returnUrl`
  - `closeUrl`
  - `purpose`
  - `svcTypes`
  - `credentialProfile`
  - provider-specific options

개인정보는 인증 완료 결과에서 provider가 주는 값이지, init 단계에서 공통으로 넘길 값이 아니다.

### 3. provider transaction/session 저장 책임 추가

NICE result 복호화에는 `ticket`, `iterators`, `transaction_id`가 필요하고, KG는 `Tradeid`, `CI_SVCID`, key 관련 정보가 필요하다.

권장:

- DB `verification_request`
  - provider
  - provider_transaction_id
  - provider_request_no
  - web_transaction_id
  - 상태/감사/조회용 correlation
- Redis `provider transaction session`
  - key: `verifyhub:provider-session:{provider}:{providerRequestNo}`
  - TTL: 10분
  - NICE: `accessTokenRef`, `ticket`, `iterators`, `transactionId`, `requestNo`
  - KG: `tradeId`, `svcId`, `siteUrl`, `callType`, `createdAt`

access token 자체는 별도 token cache로 분리한다.

### 4. NICE adapter를 mock JSON client에서 실제 3단계 client로 확장

후속 구현 단위:

- `NiceTokenManager`
- `NiceApiClient`
- `NiceCryptoHelper`
- `NiceProviderClient`
  - `requestVerification`: token -> auth/url -> `ProviderRequestResult`
  - `requestResult`: auth/result -> integrity check -> decrypt -> `ProviderResult`

현재 `MockProviderHttpClient`는 MVP mock adapter로 유지하고, 실제 NICE 구현은 `verification.adapter.out.provider.nice` 안에서 별도 class로 분리한다.

### 5. KG adapter는 Notiurl 중심으로 설계 변경

KG는 `requestResult()` polling 중심으로 일반화하지 않는 편이 낫다.

권장:

- `KgProviderClient.requestVerification`
  - KG popup/form entry 생성
  - `Tradeid` 생성/저장
- `KgNotiController` 또는 generic provider inbound controller
  - `POST /api/v1/providers/KG/noti`
  - KG 암호화 payload 수신
  - 복호화/검증
  - verification 상태 반영
  - KG 요구 응답 문자열 반환

공통 port를 유지하려면 `ProviderClientPort.requestResult()`는 NICE 중심으로 사용하고, KG는 inbound handler port를 별도로 둔다.

### 6. provider result payload 모델 추가

현재 `ProviderResult`는 최종 상태만 표현한다. S2S 전환의 핵심은 서버가 CI를 직접 획득하고 검증하는 것이므로 결과 payload가 필요하다.

권장:

```java
public record VerifiedIdentity(
        String ci,
        String di,
        String name,
        String birthDate,
        String gender,
        String mobileNo,
        String mobileCarrier,
        String nationalInfo,
        String ageCode
) {
}
```

개인정보 DB 저장은 하지 않더라도, API 응답/이벤트/일시 저장 정책은 명확해야 한다.

### 7. provider code mapping 추가

회사 기존 연동과 호환하려면 외부 API에는 `"10"`/`"20"` 코드를 노출할 수 있다.

- `"10"` -> `ProviderType.NICE`
- `"20"` -> `ProviderType.KG`

내부 도메인은 enum을 유지하고, adapter/controller DTO에서만 변환한다.

## VH-020 구현 제안

VH-020은 인증 요청 생성 API이므로 다음 범위가 적절하다.

### VH-020에 포함

- `POST /api/v1/verifications`
- requestId 생성 또는 외부 requestId 수신 정책 확정
- idempotency key 처리
- routing 수행
- `Verification` 생성 및 `REQUESTED -> ROUTED -> IN_PROGRESS`
- provider init 호출
- Client에 provider 진입 정보 반환
- provider transaction correlation 저장

### VH-020에서 바로 고치는 것이 좋은 부분

- `ProviderVerificationCommand`의 `name`, `phoneNumber`, `birthDate` 제거 또는 optional/provider-specific 처리
- `ProviderVerificationResult.authUrl`을 `ProviderAuthEntry`로 확장
- provider request number 생성 정책 추가
  - NICE 제약을 고려해 20~50 byte 문자열
  - 예: `VH` + timestamp + random suffix
- `returnUrl`, `closeUrl`, `svcTypes`를 API request 또는 설정으로 받을 수 있게 설계

### VH-020에서 미루는 것이 좋은 부분

- NICE 실제 `/auth/token`, `/auth/url`, `/auth/result` HTTP 구현
- NICE AES/GCM 복호화 샘플 검증
- KG 암복호화 모듈 포팅
- KG Notiurl 실제 payload 파싱
- 회원 DB CI 저장/로그인 처리

이 항목들은 VH-023 이후 provider return/result 작업 또는 별도 provider 실제화 티켓으로 분리하는 편이 안전하다.

## TASKS.md 변경 제안

현재 티켓 구조는 큰 틀에서 유지 가능하지만, 아래 보강이 필요하다.

### VH-020 보강

- 인증 요청 생성 API 응답에 `ProviderAuthEntry` 추가
- provider request number 생성 정책 추가
- NICE/KG 공통 init request에서 개인정보 필드 제거 검토
- returnUrl/closeUrl/svcTypes 입력 또는 설정 정책 추가

### VH-023 보강

- NICE return_url 수신 후 `/auth/result` 호출
- NICE `ticket`, `iterators`, `transaction_id` 기반 key derivation
- `integrity_value` 검증
- AES/GCM 복호화
- `VerifiedIdentity` 매핑

### 신규 또는 VH-023 하위 작업 제안

- `ProviderTransactionSessionRepository` 구현
  - Redis TTL 10분
  - NICE/KG provider session 저장
- `NiceTokenManager` 구현
  - access token cache
  - 만료 전 갱신
- `KgNotiController` 또는 provider-specific inbound handler 구현
  - KG Notiurl 수신
  - 중복 Notiurl 처리
  - KG 요구 응답 문자열 처리

## 최종 판단

현재 개발 방향은 "S2S 오케스트레이터"라는 목표와 부합한다. 특히 provider 선택, 상태 머신, 이력, outbox, Resilience4j, provider 패키지 분리는 유지하는 것이 좋다.

수정이 필요한 핵심은 provider protocol 추상화의 깊이다. NICE와 KG는 인증 시작 이후 결과 회수 방식이 다르다. 따라서 `ProviderClientPort`는 유지하되, VH-020 API 응답과 provider init/result 모델은 `authUrl` 문자열 하나가 아니라 provider별 진입 방식과 correlation/session 정보를 표현할 수 있도록 확장해야 한다.

## 반영 내용

이번 변경에서 위 제안 중 VH-020 전에 필요한 모델 변경과 임시 provider API를 먼저 반영했다.

### Provider init model 변경

- `ProviderRequest`에서 `name`, `phoneNumber`, `birthDate`를 제거했다.
- `ProviderRequest`를 provider init 요청에 필요한 값 중심으로 변경했다.
  - `verificationId`
  - `requestId`
  - `providerRequestNo`
  - `returnUrl`
  - `closeUrl`
  - `purpose`
  - `svcTypes`

### Provider auth entry 추가

- `AuthEntryType`
  - `REDIRECT_URL`
  - `FORM_POST`
- `ProviderAuthEntry`
  - provider 진입 방식, URL, HTTP method, charset, form field를 표현한다.

`ProviderRequestResult`와 `ProviderVerificationResult`는 이제 `authUrl` 문자열 대신 `ProviderAuthEntry`를 가진다.

### NICE 임시 API

NICE 직접 호출이 불가능하므로 앱 내부 `mockprovider`에 NICE OpenAPI와 같은 path 형태의 임시 API를 추가했다.

- `POST /mock/providers/NICE/ido/intc/{version}/auth/token`
- `POST /mock/providers/NICE/ido/intc/{version}/auth/url`
- `POST /mock/providers/NICE/ido/intc/{version}/auth/result`

`NiceProviderClient`는 임시 API를 실제 NICE 흐름처럼 호출한다.

- token 요청
- auth URL 요청
- `ProviderAuthEntry(type=REDIRECT_URL)` 반환

### KG 임시 API

KG 직접 호출이 불가능하므로 앱 내부 `mockprovider`에 KG popup endpoint 형태의 임시 API를 추가했다.

- `POST /mock/providers/KG/goCashMain.mcash`
- `POST /mock/providers/KG/noti`

`KgProviderClient`는 `/goCashMain.mcash`를 호출하고 `ProviderAuthEntry(type=FORM_POST, charset=EUC-KR)`를 반환한다.

### 남은 후속 작업

- provider request number 생성 정책을 별도 component로 분리
- NICE token/session 정보를 Redis에 TTL로 저장
- NICE result의 `enc_data`, `integrity_value` 검증/복호화 구현
- KG Notiurl payload 복호화/검증 구현
- VH-020 인증 요청 생성 API에서 `ProviderAuthEntry`를 응답으로 노출
