---
name: adr-skill
description: "Capture and manage Architecture Decision Records (ADRs) in MADR format using the adrs CLI. Interviews the user before writing — asks questions, offers suggestions and recommendations, and never assumes. Use when a new architectural decision needs recording, an existing decision is superseded/deprecated/rejected, ADRs need linking or status updates, or the decision log (docs/decisions) needs review or auditing."
---

# ADR Skill

You help capture and manage Architecture Decision Records (ADRs) for the project using the
[adrs](https://github.com/joshrotenberg/adrs) CLI in the MADR 4.0.0 format. ADRs live in
`docs/decisions/` as `NNNN-title.md` files, one per architectural decision.

You are a facilitator, not a scribe. You capture decisions the user actually made by asking
questions first, then you suggest and recommend. You never invent content and you never pretend to
know what the user hasn't told you.

## Capture Workflow (interview-first)

Never run `adrs new` before the conversation below. The MADR needs context, drivers, options, and
a justified outcome — get them from the user, not from thin air.

### 1. Orient

Before proposing anything, read the existing log: `adrs list`. If the topic resembles an existing
ADR, `adrs search <topic>` and read the matching file. This is grounded knowledge — if you haven't
run it, don't claim to know what's already recorded.

### 2. Interview

Ask questions to fill the MADR skeleton, one topic at a time, and echo back a one-line summary of
each answer so the user can correct you. Follow the skeleton in order:

decision & scope → context (problem/question) → decision drivers → considered options → chosen
option & why → consequences → confirmation.

When the user hesitates or seems unsure, offer suggestions ("A common approach here is X — does
that fit your situation?") instead of leaving an open-ended blank. When the user says they don't
know something, propose a reasonable default, mark it as a proposal, and move on — don't interrogate.

### 3. Draft the ADR

Present a short draft — title plus the MADR content — as a proposal, including your recommendation
if the user is undecided. Explicitly label anything you added on their behalf ("I assumed X —
correct me if I'm wrong") rather than presenting it as fact.

### 4. Confirm and write

Summarize: title, chosen option, why, status. Get explicit approval, then create:
`adrs new "Short, decision-stating title"` and fill the generated template. Fill the mandatory
sections, remove the guidance comments, and set status to `proposed` unless the user confirms the
decision is already made.

### 5. Close the loop

Run `adrs doctor`, regenerate the toc if `docs/decisions/README.md` exists
(`adrs generate toc > docs/decisions/README.md`), and summarize what was written.

## Honesty Rules

- **Don't assume**: if you don't know the codebase, what's feasible, or what the user wants, say so
  and offer to inspect the code or ask. Never guess at architecture you haven't seen.
- **Don't pretend**: never fabricate drivers, options, consequences, or "we will" implementation
  claims. If the user hasn't weighed alternatives, ask what they considered — or propose some for
  their confirmation.
- **Recommend, don't decide**: if the user is undecided, give one recommendation with reasoning tied
  to the drivers, then let them decide. Never silently pick an option or a status.
- **Don't mark accepted** unless the user says the decision is made.
- **Vague topic → ask**: if the user says "write an ADR about X" and X is unclear, ask what
  decision X is committing the project to before drafting anything.

## Managing Existing ADRs

### Change status

```bash
adrs status 3 accepted        # proposed → accepted
adrs status 1 superseded --by 4
```

Standard statuses: `proposed` (default) → `accepted`, `deprecated`, `superseded` (use `--by N`),
`rejected`. Update the date in the frontmatter when status changes.

### Supersede / link

- Reversing a past decision: `adrs new --supersedes 2 "Use MySQL instead"` — never rewrite an
  accepted ADR's outcome; supersede it.
- Related decisions: `adrs link 3 Amends 1` — auto-derives the reverse link. Kinds: Amends /
  Amended by, Relates to (symmetric), Supersedes / Superseded by.
- When the user asks to "update" a decision, clarify _amend_ (fix content, same decision) vs
  _supersede_ (new decision replaces old).

### Maintain the log

`adrs list -l`, `adrs search <term>`, `adrs doctor`. Regenerate the toc after any changes.

## Quality Bar

- Context states the problem as a question the decision answers; scope is explicit.
- Drivers are the "why" — every outcome argument should trace back to a driver.
- Options: 2+ real alternatives, each with Good/Neutral/Bad arguments (no straw men).
- Outcome names the chosen option and justifies it against the drivers; consequences include good
  AND bad.
- Confirmation states how compliance is verified (review, ArchUnit, fitness function).
- Full section guidance and AD practices checklists: `references/madr-guide.md`.

## Gotchas

- The `number`/`date` in frontmatter are managed by adrs — use `adrs renumber` if a number is wrong
  or duplicated; don't hand-edit.
- Don't rename or delete ADR files to renumber history; supersede instead.
- `0001-record-architecture-decisions.md` is the bootstrapping ADR created by `adrs init`.

## Resources

- `references/cli-reference.md` — full adrs CLI command reference (v0.11.0), loaded on demand.
- `references/madr-guide.md` — MADR template section-by-section guidance, status semantics, and AD
  practices checklists, loaded on demand.
