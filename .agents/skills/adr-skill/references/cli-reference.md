# adrs CLI Reference (v0.11.0)

Compatible with adr-tools repositories. Supports Nygard and MADR 4.0.0 formats. This repo's
`adrs.toml` sets MADR + NextGen mode, so most format flags are unnecessary.

Global options:

- `--ng` — NextGen mode (YAML frontmatter). Already enabled by this repo's config.
- `-C, --cwd <DIR>` — run from another directory.

## Core Commands

### init

Initialize a new ADR repository. Creates `.adr-dir`, the ADR dir, and ADR 0001.

```bash
adrs init                     # uses doc/adr by default
adrs init docs/decisions      # custom directory
```

Already done in this repo.

### new

Create a new ADR. Writes `NNNN-title-slug.md` directly when `no_edit = true` (this repo).

```bash
adrs new "Use PostgreSQL for persistence"
adrs new --status accepted "Already decided"
adrs new --supersedes 2 "Use MySQL instead"
adrs new -l "2:Amends:Amended by" "Title"        # link at creation time
adrs --ng new -t api,db "Title"                  # tags (comma-separated)
adrs new --deciders "a,b" --consulted "c" --informed "d" "Title"
adrs new --template /path/to/custom.tmpl "Title" # custom template overrides all
```

`-v/--variant`: full (default) | minimal | bare | bare-minimal.

### edit

Open an existing ADR in the editor (or with `--no-edit`, print/edit via other means). Use when the
user wants to amend typos, add detail, or adjust status.

### list

```bash
adrs list                        # plain listing
adrs list -l                     # detailed (status, date)
adrs list --status accepted      # filter by status
adrs list --since 2024-01-01     # filter by date
adrs --ng list --tag api         # filter by tag
```

### search

```bash
adrs search postgres             # full-text across content
adrs search -t database          # titles only
```

### link

```bash
adrs link 3 Amends 1             # bidirectional, reverse derived automatically
adrs link 3 "Relates to" 2       # symmetric relationship
adrs link 3 Supersedes 1
```

### status

```bash
adrs status 3 accepted
adrs status 1 superseded --by 4
adrs status 2 deprecated
adrs status 4 rejected
adrs status 3 "In Review"        # custom status
```

Standard statuses: proposed (default), accepted, deprecated, superseded, rejected. Use `--by N` with
`superseded` to link the replacing ADR.

### renumber

Repair duplicate or misassigned ADR numbers.

### doctor

Repository health checks. Configured via `[doctor]` in `adrs.toml` (`ignore`, `warnings_as_errors`).
Run before finishing any ADR batch.

## Other Commands

- `config` — show effective configuration.
- `generate` — documentation output:
  - `adrs generate toc` — table of contents (write to `docs/decisions/README.md`)
  - `adrs generate graph` — Graphviz relationship graph (`| dot -Tsvg > graph.svg`)
  - `adrs generate book` — mdbook site
- `export` / `import` — JSON-ADR format (federation): `adrs export json --pretty`,
  `adrs import json file.json --renumber`, `--dry-run`.
- `template list` / `template show <format>` — built-in MADR/Nygard templates and variants.
- `completions` — shell completions.
- `mcp serve` — MCP server for AI agent integration (enabled by default).
- `cheatsheet` — built-in quick reference.

## Config (adrs.toml)

Discovered by walking up from cwd; global fallback at `~/.config/adrs/config.toml`
(`%APPDATA%/adrs/config.toml` on Windows). Env overrides: `ADR_DIRECTORY`, `ADRS_CONFIG`.

Keys used in this repo: `adr_dir`, `mode` (ng), `no_edit`, `[templates]` (format/variant),
`[generate] toc_prefix`, `[export] base_url`, `[doctor] ignore/warnings_as_errors`.

## NextGen Frontmatter

YAML frontmatter fields managed by adrs (MADR/NextGen mode): `number`, `title`, `status`, `date`,
plus optional `tags`, `deciders`, `consulted`, `informed`. Number and date are tool-managed — don't
edit them by hand.
