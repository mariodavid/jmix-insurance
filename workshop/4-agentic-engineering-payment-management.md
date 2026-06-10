# Iteration 3: Payment Management

## What will be built?

Payments can be recorded for a claim. Recording a payment triggers an accounting entry via the existing accounting module (similar to how policy creation triggers account documents).

Possible scope:

- create a payment for a claim
- record payment amount, date, and reference
- validate that total payments do not exceed the reserve
- display payment history and remaining reserve
- trigger an accounting document (debit) when a payment is recorded

## Agentic Engineering Aspects

- story decomposition into smaller sub-tasks
- UI planning with Claude Design (mockups for current and target state)
- technical planning
- module and architecture rules
- small commits / smaller diffs
- ArchUnit
- static code analysis
- coverage report
- CI pipeline
- AI code review

## Example Prompt (Story Decomposition)

We start with a large issue that already exists (created in advance). The first step is to ask Claude to decompose it into functional sub-tasks:

```
This issue is too large to implement in one go. Decompose it into smaller functional sub-tasks.

Each sub-task should be a self-contained functional slice that results in one commit.
Do not split by technical layer (entity, service, UI). Split by business capability.

Create GitHub sub-tasks with acceptance criteria for each.
```

## Expected Sub-Tasks (prepared)

These are the functional slices we expect Claude to identify:

1. **Create a payment for a claim** — a claims handler can record a payment with amount, date, and reference
2. **Accounting integration** — recording a payment triggers a debit document in the accounting module
3. **Reserve validation** — total payments must not exceed the current reserve; reject payments that would exceed it
4. **Payment history and remaining reserve display** — the claim detail view shows all payments and the remaining reserve amount

Each sub-task becomes one commit. Each has its own acceptance criteria and tests.

## Claude Design (UI Planning)

Before or in parallel to the story decomposition, use Claude Design to plan the UI:

- How does the claim detail view look today (after iteration 1 and 2)?
- How should it look with payments integrated?
- Where does the payment history go?
- Where does the remaining reserve display go?
- How does the "Add Payment" dialog look?

The resulting mockups become part of the sub-task descriptions, so the coding agent has a visual reference for each UI change.

## Flow

1. Start with the large payment management issue.
2. Use Claude Design to plan the target UI (current state vs. target state mockups).
3. Use Claude to decompose the story into functional sub-tasks (see prompt above).
4. Attach UI mockups to the relevant sub-tasks.
5. Review the sub-tasks — adjust if the split is too technical or too large.
6. Implement one sub-task at a time.
7. Run tests, build, and analysis after each commit.
8. Check ArchUnit and static analysis results.
9. Inspect the coverage report.
10. Include AI code review / CI feedback.
11. Make human decisions on the findings.

## Focus

Third maturity level: the agent works within clear guardrails. Quality comes from small work packages, architecture rules, automated checks, and review.

## Learning

The more implementation work is delegated to agents, the more important automated quality boundaries and human review decisions become.