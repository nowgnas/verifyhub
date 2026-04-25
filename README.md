# verifyhub

Server-to-Server 본인인증 오케스트레이터.

이 저장소는 회사 적용 전 설계와 구현을 검증하기 위한 Spring Boot 기반 백엔드 프로젝트다. MVP 기준 런타임은 Java 17 + Spring Boot 2.7.18이며, 실제 KG/NICE 연동 키 없이 Mock Provider로 인증 요청, 장애, 지연, timeout, 중복 callback, late callback을 검증한다.

## Current Scope

- Java 17
- Spring Boot 2.7.18
- Gradle
- Hexagonal Architecture / Port & Adapter 패키지 구조
- Lombok 미사용

상세 설계는 `ARCHITECTURE.md`를 기준으로 한다.
