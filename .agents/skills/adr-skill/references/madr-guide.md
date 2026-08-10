# MADR 4.0.0 Template Guide

The MADR template ([adr.github.io/madr](https://adr.github.io/madr/)) is designed to make the
reasoning behind a decision auditable. The adrs-generated template marks optional sections with
`<!-- This is an optional element. Feel free to remove. -->` — remove sections you don't use so
readers know the record is complete.

## Sections

### Frontmatter (managed by adrs)

`number`, `title`, `status`, `date` (plus `tags`/`deciders` in NextGen mode). Don't hand-edit
number or date. Bump the date when the record is materially updated.

### Context and Problem Statement (mandatory)

Describe the problem in 2–3 sentences, or as an illustrative story. Frame it as a question the
decision answers. Make the decision's scope explicit (components, connectors, boundaries). Link
issues/boards if relevant. This section is why the record exists — a reader should understand the
problem without prior context.

### Decision Drivers (optional but recommended)

Bulleted list of forces that push the decision: quality attributes (performance, scalability,
security), constraints (licensing, team skills, budget), and concerns. These become the criteria
the options are judged against. Normalise drivers to the same level of abstraction.

### Considered Options (mandatory)

2+ realistic alternatives, one line each. Name them concretely ("PostgreSQL 16", not "SQL
database"). Every option must get a fair treatment in "Pros and Cons"; no straw men.

### Decision Outcome (mandatory)

- `Chosen option: "X", because <justification>` — justify against drivers: "only option meeting
  k.o. criterion Y", "resolves force Z", or "came out best in weighted comparison". Avoid
  justification by authority ("team prefers it") without a driver-based reason.
- `### Consequences` — bullet good AND bad consequences of the choice.
- `### Confirmation` — how compliance/implementation is verified: design review, ArchUnit test,
  fitness function. Include it; it turns the ADR into an enforceable contract.

### Pros and Cons of the Options (optional)

One subsection per considered option. Each bullet is `Good/Neutral/Bad, because <argument>`.
Arguments must be concrete and traceable to drivers. An option with only good or only bad
arguments is a sign of a loaded comparison — fix it.

### More Information (optional)

Additional evidence, team agreement, when the decision should be realised, and when it should be
re-visited. Links to related ADRs or resources go here.

## Status Semantics

| Status     | Meaning                             | Notes                                            |
| ---------- | ----------------------------------- | ------------------------------------------------ |
| proposed   | Initial, not yet decided            | Default for new ADRs                             |
| accepted   | Approved, actionable                |                                                  |
| deprecated | No longer recommended, not replaced |                                                  |
| superseded | Replaced by another ADR             | Use `--by N` to link the replacement             |
| rejected   | Considered but not approved         | Keep the record: rejected decisions are valuable |

Custom statuses are allowed. Never rewrite an accepted ADR to change its outcome — supersede it.

## AD Practices Checkpoints

From [adr.github.io/ad-practices](https://adr.github.io/ad-practices/) — apply these before
declaring an ADR done:

### Definition of Ready (START)

An ADR is worth starting when it is:

- **S**pecific — one decision, clearly scoped
- **T**raceable — relates to an issue, requirement, or driver
- **A**ctionable — deciding now unblocks real work
- **R**elevant — architecturally significant (hard to reverse, costly to change, cross-cutting)
- **T**imely — the decision is being made now, not retrofitting

### Definition of Done

A decision is done when you have: evidence, criteria and considered alternatives, documented
agreement, a record of the decision, and a realization/review plan.

### Writing Guidance

- Good justification: driver-based and comparative ("Option A meets the k.o. criterion for 5-nines
  availability, Option B does not").
- Bad justification: restating the decision, appeal to authority, or "industry best practice"
  without tying it to this project's drivers.
- When the same conclusion has many possible routes, weight criteria down to one level of
  abstraction before comparing.

## Further Reading

- MADR project & templates: https://github.com/adr/madr
- MADR template explained: https://www.ozimmer.ch/practices/2022/11/22/MADRTemplatePrimer.html
- How to create ADRs (and how not to): https://www.ozimmer.ch/practices/2023/04/03/ADRCreation.html
- How to review ADRs: https://www.ozimmer.ch/practices/2023/04/05/ADRReview.html
- Architectural Significance Test: https://www.ozimmer.ch/practices/2020/09/24/ASRTestECSADecisions.html
