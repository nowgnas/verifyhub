# VH-020 Verification Create API Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build `POST /api/v1/verifications` so clients can create a verification request and receive provider auth entry information.

**Architecture:** Add a thin web adapter and a `VerificationCreateService` application boundary. The service creates or reuses `Verification` through `IdempotencyService`, then delegates provider initialization to `ProviderVerificationFlowService`.

**Tech Stack:** Java 17, Spring Boot 2.7.18, Spring MVC validation, JUnit 5, Mockito, MockMvc.

---

### Task 1: Application Service

**Files:**
- Create: `src/main/java/com/verifyhub/verification/application/VerificationCreateCommand.java`
- Create: `src/main/java/com/verifyhub/verification/application/VerificationCreateService.java`
- Test: `src/test/java/com/verifyhub/verification/application/VerificationCreateServiceTest.java`

**Steps:**
1. Write a failing test for a new verification request creating a requested aggregate and calling provider flow.
2. Run `./gradlew test --tests com.verifyhub.verification.application.VerificationCreateServiceTest --no-daemon` and confirm failure.
3. Implement the command record and service with minimal logic.
4. Run the focused test and confirm pass.
5. Add a failing test for idempotent reuse.
6. Implement any missing minimal behavior and confirm pass.

### Task 2: Web API

**Files:**
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/VerificationCreateController.java`
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/VerificationCreateRequest.java`
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/VerificationCreateResponse.java`
- Test: `src/test/java/com/verifyhub/verification/adapter/in/web/VerificationCreateControllerTest.java`

**Steps:**
1. Write failing MockMvc tests for success, missing `Idempotency-Key`, and invalid request body.
2. Run `./gradlew test --tests com.verifyhub.verification.adapter.in.web.VerificationCreateControllerTest --no-daemon` and confirm failure.
3. Implement request/response DTOs and controller.
4. Run the focused web test and confirm pass.

### Task 3: Documentation

**Files:**
- Modify: `docs/TASKS.md`

**Steps:**
1. Mark `VH-020` done.
2. Keep `VH-022` as the next ticket.
3. Record verification command and result.
4. Run `./gradlew clean test --no-daemon`.
