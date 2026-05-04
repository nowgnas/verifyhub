# VH-024 Late Duplicate Return Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Prevent terminal verification state from being changed by late or duplicate provider returns while recording audit history.

**Architecture:** Extend `ProviderReturnService` with a terminal-status branch. It still validates provider and retrieves provider result, but writes `LateCallbackHistoryService` instead of changing verification state or enqueueing outbox events.

**Tech Stack:** Java 17, Spring Boot 2.7.18, JUnit 5, Mockito.

---

### Task 1: Service Policy Tests

**Files:**
- Modify: `src/test/java/com/verifyhub/verification/application/ProviderReturnServiceTest.java`

**Steps:**
1. Write failing tests for late timeout success and duplicate success callback.
2. Run `./gradlew test --tests com.verifyhub.verification.application.ProviderReturnServiceTest --no-daemon` and confirm failure.

### Task 2: Service Implementation

**Files:**
- Modify: `src/main/java/com/verifyhub/verification/application/ProviderReturnService.java`

**Steps:**
1. Inject `LateCallbackHistoryService`.
2. Add terminal branch before normal state transition.
3. Record late callback history and return current status.
4. Run focused tests and confirm pass.

### Task 3: Task Board and Verification

**Files:**
- Modify: `docs/TASKS.md`

**Steps:**
1. Mark `VH-024` done.
2. Set next ticket to `VH-025`.
3. Run `./gradlew clean test --no-daemon`.
4. Commit and create PR.
