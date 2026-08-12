# Dokja Project Instructions

## Project context

Dokja is a Vietnam-focused digital content publishing and distribution platform for licensed Anime,
Comic (Webtoon), and Novel products. Learning/demo project, not production-ready.

## Repository structure

Monorepo with two independent build roots (separate lockfiles, separate tooling — never mix
commands):

- `frontend/` — Vite+ / bun workspace (TypeScript). All tooling goes through the `vp` CLI.
- `backend/` — Gradle build, single `:api` Spring Boot module (Kotlin, JDK 25). Run `./gradlew` from
  `backend/`.

Plus:

- `docs/` — project documentation; index in `docs/README.md`.
- `docs/decisions/` — architecture decision records (ADRs).
- `docs/frontend/` — frontend-specific documentation.
- `docs/backend/` — backend-specific documentation.

## Agents

When working within a root, MUST READ that root's `AGENTS.md` to understand its context and
instructions:

- `backend/AGENTS.md`
- `frontend/AGENTS.md`

## Architecture Decision Records

If the request has architecture-related context, use `adr-skill` to look up the existing ADRs. If
not, use `adr-skill` to propose a new ADR.
