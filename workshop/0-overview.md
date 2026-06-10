# Agentic Engineering Live – Workshop Flow

## Workshop Goal

We will extend an existing Jmix insurance application with a new claims module.

The application is not an empty demo project. It is an existing multi-module Gradle system with:

- Quote module
- Policy module
- Accounting module
- Partner module
- Security module

The project also contains (initially deactivated) quality infrastructure that will be enabled during the workshop:

- architecture rules (ArchUnit)
- static code analysis (PMD, SpotBugs, Spotless)
- coverage thresholds (JaCoCo)
- CI pipeline
- AI code review
- agent skills

The workshop shows how AI-assisted development can evolve from simple code generation into professional agentic engineering.

---

## Introduction

### What happens?

- Briefly explain the difference between vibe coding and agentic engineering.
- Live demo: generate a claims reporting form with React and Vite from a single one-shot prompt (iteration 0).

### Focus

- Vibe coding can quickly produce visible results.
- That is impressive, but not enough for professional software development.
- Agentic engineering needs context, planning, requirements, tests, architecture rules, and reviews.

---

## Project Context

### What happens?

- Show the existing Jmix insurance application.
- Explain the functional context:
    - quotes
    - policies
    - accounting
- Explain the goal: add a new claims module.

### Functional slices

1. Claim creation
2. Reserve management
3. Payment management

### Focus

We are adding a new module inside an existing product, not building software in a vacuum.

---

## Maturity Curve

### Iteration 0: Vibe coding — claims form

- one-shot prompt: React + Vite claims form
- no plan, no tests, no architecture, no integration
- contrast to what follows

### Iteration 1: Plan and implement (claim creation)

- functional task as prompt
- plan mode
- implementation with Claude Code
- review the result

### Iteration 2: Persist requirements (reserve management)

- GitHub issue as persistent artifact
- iterate on the issue: add acceptance criteria and test specifications
- activate all agent skills
- implementation against the issue
- verify result against acceptance criteria

### Iteration 3: Govern implementation (payment management)

- Claude Design for UI planning (current state vs. target state)
- story decomposition into functional sub-tasks
- small diffs, one commit per sub-task
- activate all quality gates (ArchUnit, PMD, SpotBugs, Spotless, coverage)
- CI pipeline with AI code review
- human decisions on findings

---

## Core Message

Vibe coding produces output quickly.

Agentic engineering produces traceable, verifiable, and integrable software changes.

The difference is not whether AI writes code. The difference is whether the work is controlled through requirements, planning, context, tests, architecture rules, and reviews.