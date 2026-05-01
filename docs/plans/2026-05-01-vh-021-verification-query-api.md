# VH-021 인증 조회 API 구현 계획

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**목표:** 인증 상태 조회 API와 인증 이력 조회 API를 추가한다.

**아키텍처:** Web adapter는 얇게 유지한다. 조회 전용 application service가 output port를 통해 `Verification`과 `VerificationHistory`를 읽고, `/api/v1/verifications` 하위 controller가 응답 DTO로 변환해 노출한다.

**기술 스택:** Java 17, Spring Boot 2.7.18, Spring MVC, Spring Data JPA, JUnit 5, Mockito, MockMvc.

---

### 작업 1: 조회 서비스

**파일:**
- 생성: `src/main/java/com/verifyhub/verification/application/VerificationQueryService.java`
- 수정: `src/main/java/com/verifyhub/verification/port/out/VerificationHistoryRepositoryPort.java`
- 테스트: `src/test/java/com/verifyhub/verification/application/VerificationQueryServiceTest.java`

**단계:**
1. 단건 인증 조회와 시간순 이력 조회 실패 테스트를 작성한다.
2. `./gradlew test --tests com.verifyhub.verification.application.VerificationQueryServiceTest --no-daemon`를 실행해 실패를 확인한다.
3. `VerificationQueryService`를 구현한다.
4. 이력 port에 `findByVerificationIdOrderByCreatedAtAsc`를 추가한다.
5. 집중 테스트를 다시 실행해 통과를 확인한다.

### 작업 2: Persistence 조회

**파일:**
- 수정: `src/main/java/com/verifyhub/verification/adapter/out/persistence/repository/VerificationHistoryJpaRepository.java`
- 수정: `src/main/java/com/verifyhub/verification/adapter/out/persistence/VerificationHistoryPersistenceAdapter.java`
- 테스트: `src/test/java/com/verifyhub/verification/adapter/out/persistence/VerificationHistoryPersistenceAdapterIT.java`

**단계:**
1. 이력을 순서와 다르게 저장한 뒤 `createdAt ASC` 순서로 읽는 실패 통합 테스트를 작성한다.
2. 집중 통합 테스트를 실행해 실패를 확인한다.
3. JPA repository 조회 메서드와 adapter 매핑을 구현한다.
4. 집중 통합 테스트를 다시 실행해 통과를 확인한다.

### 작업 3: Web API

**파일:**
- 생성: `src/main/java/com/verifyhub/verification/adapter/in/web/VerificationQueryController.java`
- 생성: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/VerificationQueryResponse.java`
- 생성: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/VerificationHistoryListResponse.java`
- 생성: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/VerificationHistoryResponse.java`
- 테스트: `src/test/java/com/verifyhub/verification/adapter/in/web/VerificationQueryControllerTest.java`

**단계:**
1. 상태 조회, 이력 조회, 404 응답에 대한 실패 MockMvc 테스트를 작성한다.
2. 집중 Web 테스트를 실행해 실패를 확인한다.
3. DTO record와 controller method를 구현한다.
4. 집중 Web 테스트를 다시 실행해 통과를 확인한다.

### 작업 4: 문서와 작업 보드

**파일:**
- 수정: `docs/TASKS.md`

**단계:**
1. `VH-021`을 완료 상태로 표시한다.
2. 다음 티켓을 `VH-022`로 설정한다.
3. 전체 테스트 실행 후 검증 명령과 결과를 기록한다.
4. `./gradlew clean test --no-daemon`를 실행한다.
