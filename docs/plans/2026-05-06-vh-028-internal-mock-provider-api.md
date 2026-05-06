# VH-028 Internal Mock Provider API Plan

## Goal

provider client와 수동 테스트 콘솔에서 사용할 수 있는 internal mock provider API와 scenario 상태 관리를 구현한다.

## Steps

1. 기존 mock provider controller 테스트에 scenario API와 공통 verification/result endpoint 기대 동작을 추가한다.
2. `MockProviderScenario` enum을 추가한다.
3. provider별 scenario 상태를 관리하는 application service를 추가한다.
4. `POST /mock/providers/{provider}/scenario`를 구현한다.
5. `POST /mock/providers/{provider}/verifications`와 `POST /mock/providers/{provider}/results`를 구현한다.
6. `GET/POST /mock/providers/{provider}/returns`를 구현한다.
7. KG compatible endpoint가 scenario 상태를 반영하게 연결한다.
8. invalid enum request가 400 `INVALID_REQUEST`로 처리되도록 전역 handler를 보강한다.
9. focused test와 전체 테스트를 실행한다.

## Verification

- `./gradlew test --tests com.verifyhub.mockprovider.adapter.in.web.MockPhoneAuthProviderControllerTest --no-daemon`
- `./gradlew clean test --no-daemon`
