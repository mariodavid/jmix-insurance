# Iteration 2: Reserve Management

## What will be built?

A claim receives a reserve that can be maintained by a claims handler.

Possible scope:

- reserve amount
- reserve type
- comment / reasoning
- display of the current reserve
- simple business rules

## Agentic Engineering Aspects

- requirements engineering
- GitHub issue as a persistent work artifact
- acceptance criteria
- test specifications
- Skills
- project context / `CLAUDE.md`
- more targeted implementation against requirements

## Example Prompt (Issue Creation)

```
Create a GitHub issue for adding reserve management to the claims module.

A claims handler should be able to set and update a reserve for a claim.
The reserve represents the estimated total cost of the claim.

Include in the issue:
- functional description
- acceptance criteria (as a checklist)
- definition of done

Business rules:
- a reserve must have an amount > 0
- a reserve must have a type (e.g. INITIAL, ADJUSTMENT)
- a reserve must have a comment explaining the reasoning
- the current reserve is always the latest entry
- the claim detail view shows the reserve history
```

## Flow

1. Use Claude to create the GitHub issue (see example prompt above).
2. Review the generated issue.
3. **Iterate on the issue**: add test specifications and acceptance criteria (see below).
4. Let Claude Code plan against the refined issue.
5. Start implementation.
6. Run tests.
7. Review the result against the issue and acceptance criteria.

## Acceptance Criteria and Test Cases (for iteration on the issue)

These are prepared so you can add them to the issue during the live session to demonstrate iterating on the requirements artifact:

### Acceptance Criteria

- A reserve can be created for an existing claim
- Reserve amount must be > 0
- Reserve must have a type (INITIAL, ADJUSTMENT)
- Reserve must have a comment
- The current reserve is the most recent entry
- The claim detail view shows reserve history

### Test Specifications

- Creating a reserve with valid data persists it successfully
- Creating a reserve with zero or negative amount is rejected
- The current reserve is always the most recently created entry
- Reserve history is ordered by creation date (newest first)
- The "Add Reserve" action opens a dialog from the claim detail view
- After saving a reserve, the claim detail view shows the updated amount

## Focus

Second maturity level: the agent no longer works only from a prompt. It works against a persistent, reviewable requirements artifact. You iterate on the issue — first the functional description, then you add concrete test expectations. The agent implements against these criteria.

## Learning

Good requirements and pre-specified test expectations make agent work verifiable. You define what "done" means before implementation starts, and you can check the result against concrete criteria instead of hoping the agent wrote meaningful tests on its own.