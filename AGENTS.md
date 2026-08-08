# Dokja Project Instructions

## Project context

Dokja is a Vietnam-focused digital content publishing and distribution platform for licensed Anime,
Comic (Webtoon), and Novel products. Learning/demo project, not production-ready.

## Repository structure

Monorepo with two independent build roots (separate lockfiles, separate tooling — never mix
commands):

- `frontend/` — Vite+ / bun workspace (React 19, TanStack Start, TypeScript). All tooling goes
  through the `vp` CLI.
- `backend/` — Gradle build, single `:api` Spring Boot module (Kotlin, JDK 25). Run `./gradlew` from
  `backend/`.

Plus:

- `CONTRIBUTING.md` — authoritative guide for setup, conventions, branching, and PR process. Read it
  before first PR.
- `docs/` — project documentation; index in `docs/README.md`. Architecture docs go in
  `docs/architecture/`, ADRs in `docs/adr/`.
- `.github/workflows/build.yml` — CI: backend `./gradlew :api:build`, frontend `vp install` →
  `vp check` → `vp run -r build`.

## Agents

When working within a root, MUST READ that root's `AGENTS.md` to understand its context and
instructions:

- `backend/AGENTS.md`
- `frontend/AGENTS.md`

Architecture work: `.agents/agents/architect.md` defines the architect agent (design-only, no
implementation). For ADR workflows use the `adr-skill`.
