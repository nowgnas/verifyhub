# VH-021 Verification Query API Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add read APIs for verification status and verification histories.

**Architecture:** Keep the web adapter thin. Add a query application service that reads `Verification` and `VerificationHistory` through output ports, then expose DTOs from a controller under `/api/v1/verifications`.

**Tech Stack:** Java 17, Spring Boot 2.7.18, Spring MVC, Spring Data JPA, JUnit 5, Mockito, MockMvc.

---

### Task 1: Query Service

**Files:**
- Create: `src/main/java/com/verifyhub/verification/application/VerificationQueryService.java`
- Modify: `src/main/java/com/verifyhub/verification/port/out/VerificationHistoryRepositoryPort.java`
- Test: `src/test/java/com/verifyhub/verification/application/VerificationQueryServiceTest.java`

**Steps:**
1. Write failing tests for fetching one verification and ordered histories.
2. Run `./gradlew test --tests com.verifyhub.verification.application.VerificationQueryServiceTest --no-daemon` and confirm failure.
3. Implement `VerificationQueryService`.
4. Add `findByVerificationIdOrderByCreatedAtAsc` to the history port.
5. Run the focused test and confirm pass.

### Task 2: Persistence Query

**Files:**
- Modify: `src/main/java/com/verifyhub/verification/adapter/out/persistence/repository/VerificationHistoryJpaRepository.java`
- Modify: `src/main/java/com/verifyhub/verification/adapter/out/persistence/VerificationHistoryPersistenceAdapter.java`
- Test: `src/test/java/com/verifyhub/verification/adapter/out/persistence/VerificationHistoryPersistenceAdapterIT.java`

**Steps:**
1. Write a failing integration test that saves histories out of order and reads them in `createdAt ASC` order.
2. Run the focused integration test and confirm failure.
3. Implement the JPA repository query and adapter mapping.
4. Run the focused integration test and confirm pass.

### Task 3: Web API

**Files:**
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/VerificationQueryController.java`
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/VerificationQueryResponse.java`
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/VerificationHistoryListResponse.java`
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/VerificationHistoryResponse.java`
- Test: `src/test/java/com/verifyhub/verification/adapter/in/web/VerificationQueryControllerTest.java`

**Steps:**
1. Write failing MockMvc tests for status lookup, histories lookup, and 404 lookup.
2. Run the focused web test and confirm failure.
3. Implement DTO records and controller methods.
4. Run the focused web test and confirm pass.

### Task 4: Documentation and Board

**Files:**
- Modify: `docs/TASKS.md`

**Steps:**
1. Mark `VH-021` done.
2. Set next ticket to `VH-022`.
3. Record verification command and result after full test execution.
4. Run `./gradlew clean test --no-daemon`.
