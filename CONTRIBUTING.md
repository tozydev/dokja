# Contributing to Dokja

This guide covers the workflow, conventions, and tooling used across the repository.

Dokja is a Vietnam-focused digital content publishing and distribution platform for licensed Anime,
Comic (Webtoon), and Novel products.

## Table of contents

- [Repository overview](#repository-overview)
- [Prerequisites](#prerequisites)
- [Getting started](#getting-started)
- [Development workflow](#development-workflow)
- [Code conventions](#code-conventions)
- [Continuous integration](#continuous-integration)
- [Pull request process](#pull-request-process)
- [Code of conduct](#code-of-conduct)

## Repository overview

The repository is a monorepo split into two **independent build roots**:

| Root       | Stack                                  | Tooling              |
| ---------- | -------------------------------------- | -------------------- |
| `frontend` | Vite+ workspace (React 19, TypeScript) | `vp` (vite-plus CLI) |
| `backend`  | Spring Boot 4 (Kotlin, JVM)            | Gradle (`./gradlew`) |

## Prerequisites

### Common

- [Git](https://git-scm.com/)
- Access to the repo: `git@github.com:tozydev/dokja.git`

### Frontend

- **Node.js >= 24**
- **bun** ([bun.sh](https://bun.sh))

### Backend

- **JDK 25**
- Docker (optional but recommended for local services via Spring Boot Docker Compose integration)

## Getting started

### 1. Clone and bootstrap

```bash
git clone git@github.com:tozydev/dokja.git
cd dokja
```

### 2. Frontend setup

All `vp` commands are run from the `frontend/` root:

```bash
cd frontend
vp install            # install dependencies (use vp, not npm/yarn)
vp dev                # start dev server
```

`vp` is the vite-plus CLI. Use it for install, check, build, and test — the CI relies on it, so
local commands should match. `vp run`/`vpx` works for scripts defined in workspace `package.json`
files.

### 3. Backend setup

```bash
cd backend
./gradlew :api:build   # compile + test the :api module
```

The `:api` module is a Spring Boot application. The dev profile auto-starts local infrastructure
(Postgres, Redis, RustFS, Keycloak) via `docker-compose.yaml` and Spring Boot's Docker Compose
integration:

```bash
./gradlew :api:bootRun --args='--spring.profiles.active=dev'
```

### 4. Git hooks (recommended)

This repository uses [lefthook](https://lefthook.dev/) pre-commit hooks to auto-format changed files
before every commit (config in `lefthook.yml`):

- **Backend** — format Kotlin with `ktfmt` (staged files only, via the `ktfmtPrecommit` task)
- **Frontend** — format with oxfmt via `vp fmt`
- **Rest of the repo** — format with oxfmt via `vpx oxfmt`

Install the hooks once per clone:

```bash
vpx lefthook install
```

Hooks then run automatically on `git commit`. If hooks are ever missing (e.g. after switching
machines or cloning again), re-run the install command above.

## Development workflow

### Branching

- `main` is the default and protected branch — do not push to it directly.
- Create a feature branch from `main`:
  ```bash
  git checkout -b feat/my-change
  ```
- Branch naming: `feat/`, `fix/`, `chore/`, `refactor/`, `docs/` prefixes matching your work.

### Conventional commits

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>[optional scope]: <description>
```

Examples from this repo:

```
refactor: split frontend and backend into separate roots
chore: init project
```

Common types: `feat`, `fix`, `chore`, `refactor`, `docs`, `test`, `build`, `perf`, `ci`. Use a scope
when the change is confined to one area, e.g. `fix(api): ...` or `feat(web): ...`.

## Code conventions

### General

- Follow `.editorconfig` — UTF-8, LF line endings, final newline, trimmed trailing whitespace. Most
  editors pick this up automatically.
- Do not commit secrets, `.env` files, or generated build artifacts (see `.gitignore`).

### Frontend

- TypeScript with strict type checking (`vp check` runs lint + type-check).
- Style is enforced by `vite-plus` formatter (see `vite.config.ts`):
  - No semicolons, double quotes, sorted imports, sorted Tailwind classes.
- Run formatter via `vp fmt`.
- Reuse `@dokja/ui` components instead of duplicating styling.

### Backend

- Kotlin, official code style (format via `ktfmt`).
- Package namespace: `vn.id.tozydev.dokja.server`.
- Dependencies are declared through the generated version catalog (`libs.versions.toml`) — do not
  hardcode versions in `build.gradle.kts`.
- Spring Modulith for modular architecture — keep modules cohesive.

## Continuous integration

The `Build` workflow (`.github/workflows/build.yml`) runs on every push/PR to `main`:

- **Backend**: `./gradlew :api:build`
- **Frontend**: `vp install` → `vp check` → `vp run -r build`

Your PR must pass all checks before it can merge. Run the same commands locally first to avoid
wasting CI cycles.

## Pull request process

1. Branch from `main`, implement, and commit with a conventional message.
2. Run all local checks (frontend `vp check` + `vp run -r build`, backend `./gradlew :api:build`) to
   confirm the CI will pass.
3. Open a PR against `main` with a clear description:
   - What and why
   - How to verify / test
   - Any screenshots for UI changes
4. Keep PRs small and focused. Split large changes into multiple PRs.
5. Address review feedback; re-run checks after changes.
6. PRs must pass CI and receive review approval before merging.

## Code of conduct

Be respectful and constructive. This is a team project — assume good intent, give clear feedback,
and help reviewers understand your changes.
