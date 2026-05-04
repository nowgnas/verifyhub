# VH-023 Provider Return Result Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement provider return endpoints that retrieve provider results and complete verification status.

**Architecture:** Add a web adapter for `/api/v1/providers/{provider}/returns` and an application service that coordinates verification lookup, provider result retrieval, state transition, and outbox event enqueueing.

**Tech Stack:** Java 17, Spring Boot 2.7.18, Spring MVC validation, JUnit 5, Mockito, MockMvc.

---

### Task 1: Application Service

**Files:**
- Create: `src/main/java/com/verifyhub/verification/application/ProviderReturnCommand.java`
- Create: `src/main/java/com/verifyhub/verification/application/ProviderReturnResult.java`
- Create: `src/main/java/com/verifyhub/verification/application/ProviderReturnService.java`
- Modify: `src/main/java/com/verifyhub/verification/application/VerificationStateService.java`
- Test: `src/test/java/com/verifyhub/verification/application/ProviderReturnServiceTest.java`

**Steps:**
1. Write failing tests for success result, provider mismatch, and integrity failure.
2. Run focused tests and confirm failure.
3. Implement command/result records and service.
4. Add `VerificationStateService.recordProviderReturn(...)`.
5. Run focused tests and confirm pass.

### Task 2: Web API

**Files:**
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/ProviderReturnController.java`
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/ProviderReturnRequest.java`
- Create: `src/main/java/com/verifyhub/verification/adapter/in/web/dto/ProviderReturnResponse.java`
- Test: `src/test/java/com/verifyhub/verification/adapter/in/web/ProviderReturnControllerTest.java`

**Steps:**
1. Write failing MockMvc tests for GET, POST, and invalid request.
2. Run focused tests and confirm failure.
3. Implement DTOs and controller.
4. Run focused tests and confirm pass.

### Task 3: Task Board and Verification

**Files:**
- Modify: `docs/TASKS.md`

**Steps:**
1. Mark `VH-023` done.
2. Set next ticket to `VH-024`.
3. Run `./gradlew clean test --no-daemon`.
4. Commit and create PR.
