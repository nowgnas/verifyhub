# VH-022 VerificationFlow Integration Test Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add an end-to-end integration test for verification create/query/history flow and fix idempotent replay so provider initialization is not repeated.

**Architecture:** Use `@SpringBootTest`, `MockMvc`, and Testcontainers MySQL. Keep real application/persistence/routing services, but mock provider execution at `ProviderClientResilienceDecorator` so the test stays deterministic and avoids network calls.

**Tech Stack:** Java 17, Spring Boot 2.7.18, MockMvc, JUnit 5, Mockito, AssertJ, Testcontainers MySQL, JdbcTemplate.

---

### Task 1: Integration Test

**Files:**
- Create: `src/test/java/com/verifyhub/verification/application/VerificationFlowIntegrationTest.java`

**Steps:**
1. Write a failing integration test that posts `POST /api/v1/verifications`, checks DB rows, calls query APIs, then replays the same idempotency request.
2. Run `./gradlew test --tests com.verifyhub.verification.application.VerificationFlowIntegrationTest --no-daemon` and confirm failure.

### Task 2: Idempotent Replay Fix

**Files:**
- Modify: `src/main/java/com/verifyhub/verification/application/VerificationCreateService.java`
- Test: `src/test/java/com/verifyhub/verification/application/VerificationCreateServiceTest.java`

**Steps:**
1. Add a unit test that existing non-`REQUESTED` verification is returned without calling provider flow.
2. Implement minimal replay behavior in `VerificationCreateService`.
3. Run focused service and integration tests.

### Task 3: Task Board

**Files:**
- Modify: `docs/TASKS.md`

**Steps:**
1. Mark `VH-022` done.
2. Set next ticket to `VH-023`.
3. Record verification command and result.
4. Run `./gradlew clean test --no-daemon`.
