# Dokja Backend Instructions

## Stack

- Language: Kotlin JVM 2.4+ (not Java, not Kotlin 1.x)
- Framework: Spring Framework 7, Spring Boot 4, Spring Modulith
- Security: Spring Security 7, OAuth2 Resource Server
- Database: PostgreSQL 18, Spring Data JPA, Flyway
- Unit Testing: JUnit 5, Spring Test, MockK
- Integration Testing: Spring Boot Test, WebTestClient (reactive), Testcontainers
- Build Tool: Gradle 9, Kotlin DSL, Gradle Version Catalog
- Caching: Redis 8, Spring Cache
- Storage: S3-compatible, AWS Kotlin S3 SDK

## Architecture

- Base package: `vn.id.tozydev.dokja.backend`
- Spring Modulith: keep code in cohesive modules.
- Security: Spring Security 7 Kotlin DSL. OAuth2 resource server validates Keycloak JWTs.

## Dependencies

- Centralized dependency management with `gradle/libs.versions.toml`.
- Do not hardcode library and version coordinates in `build.gradle.kts`.
- Use BOM catalog-generated dependency declarations from `settings.gradle.kts`, e.g.
  `springLibs.spring.boot.starter.webmvc`

## Commands

- Use bash shell.
- Always use `./gradlew -q --console=plain` to avoid Gradle's verbose output.
- Use `./gradlew -q --console=plain :api:compileKotlin` for quick syntax checks.
- Use `./gradlew -q --console=plain :api:build` to build, check and test.

## Dev Infrastructure

- Use `../infra/compose.be-dev.yaml` to run all services in development mode.
- Use `../infra/compose.obs-dev.yaml` for observability (OpenTelemetry Collector + OpenObserve).
- Ports:
  - Keycloak: 9000
  - Postgres: 9010
  - Redis: 9020
  - RustFS (Object Storage): 9030
  - RustFS (Web UI): 9031
  - OTLP (gRPC): 9040
  - OTLP (HTTP): 9041
  - OpenObserve (UI/API): 9050

## Code Conventions

- Follow `ktfmt` (Kotlinlang style) for code formatting.
