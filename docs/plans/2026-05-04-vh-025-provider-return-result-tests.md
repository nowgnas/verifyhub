# VH-025 Provider Return/Result Test Plan

## Goal

Provider return/result 흐름의 핵심 정책을 통합 테스트로 고정한다. 특히 성공, 실패, duplicate terminal callback이 API와 DB 저장 계층을 지나도 의도대로 동작하는지 확인한다.

## Steps

1. 기존 `VerificationFlowIntegrationTest`의 Testcontainers, MockMvc, provider decorator mock 패턴을 재사용한다.
2. `ProviderReturnIntegrationTest`를 추가한다.
3. 인증 생성 helper와 provider result stub helper를 작성한다.
4. success, integrity failure, duplicate terminal return 테스트를 작성한다.
5. focused integration test를 실행한다.
6. 전체 테스트를 실행한다.
7. `docs/TASKS.md`에서 VH-025 완료와 다음 티켓을 갱신한다.

## Verification

- `./gradlew test --tests com.verifyhub.verification.application.ProviderReturnIntegrationTest --no-daemon`
- `./gradlew clean test --no-daemon`
