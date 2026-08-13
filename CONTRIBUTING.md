# Contributing to Dokja

Dokja is a Vietnam-focused digital content publishing and distribution platform for licensed Anime,
Comic (Webtoon), and Novel products.

The repository is a monorepo with two **independent build roots**:

| Root       | Stack                                  | Tooling              |
| ---------- | -------------------------------------- | -------------------- |
| `frontend` | Vite+ workspace (React 19, TypeScript) | `vp` (vite-plus CLI) |
| `backend`  | Spring Boot (Kotlin, JVM)              | Gradle (`./gradlew`) |

## Prerequisites

- Git
- Vite Plus, Node.js >= 24 and bun
- JDK 25
- Docker & Docker Compose (optional, for local dev infrastructure)

## Setup

Clone the repo, then set up each root:

```bash
git clone git@github.com:tozydev/dokja.git
cd dokja
```

### Frontend

Run all commands from `frontend/` using `vp` (never npm/yarn/pnpm/bun):

```bash
cd frontend
vp install   # install dependencies
vp dev       # start dev server
```

Start the local dev infrastructure (Postgres, Redis, RustFS, Keycloak) and the backend API first:

```bash
./infra/dokja-dev.sh up api   # from repo root, start dev infrastructure + API
```

### Backend

Run all commands from `backend/` using `./gradlew`:

```bash
cd backend
./gradlew :api:build    # compile + test
```

Start the local dev infrastructure (Postgres, Redis, RustFS, Keycloak), then run the app:

```bash
./infra/dokja-dev.sh up   # from repo root, start dev infrastructure
./gradlew :api:bootRun    # from backend/
```

### Git hooks

Install [lefthook](https://lefthook.dev/) hooks once per clone (config in `.lefthook.yml`):

```bash
vpx lefthook install
```

Pre-commit formats staged files (backend via ktfmt, frontend via `vp staged`, rest via oxfmt);
pre-push runs the backend check.

## Git Flow

### Branching

- `main` is protected — never push to it directly.
- Create a branch from `main` named after the issue or `<type>/<desc>`:

```bash
git checkout -b feat/my-change
```

Types: `feat/`, `fix/`, `chore/`, `refactor/`, `docs/`.

### Commits

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>[optional scope]: <description>
```

Common types: `feat`, `fix`, `chore`, `refactor`, `docs`, `test`, `build`, `perf`, `ci`. Use a scope
for changes confined to one area, e.g. `fix(frontend): ...`.

### Pull requests

1. Open a PR against `main`.
2. Describe what and why, and how to verify/test.
3. Keep PRs small and focused.
4. Address review feedback and re-run checks.
5. Merge requires CI passing and review approval.

## Code styles

- **General**: oxfmt (see `.oxfmtrc.json`)
- **Backend**: ktfmt (kotlinlang style); declare dependencies via the version catalog
  (`libs.versions.toml`), never hardcode versions
- **Frontend**: oxfmt via `vp fmt` (see `vite.config.ts`); strict TypeScript
