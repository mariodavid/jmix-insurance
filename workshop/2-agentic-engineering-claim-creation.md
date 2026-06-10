# Iteration 1: Claim Creation

## What will be built?

A backoffice user can create a claim for an existing policy.

Minimal scope:

- create a claim
- link it to a policy
- provide a simple backoffice UI
- integrate it into the existing application

## Agentic Engineering Aspects

- functional task
- plan mode
- implementation with Claude Code
- short functional and technical review
- initial tests are expected, but not yet deeply specified

## Example Prompt

```
Create a new claims module for this Jmix insurance application.

A backoffice user should be able to create a claim linked to an existing policy.
The claim needs:
- a generated claim number
- a reference to a policy
- a date of loss
- a short description
- a claim status (OPEN, IN_PROGRESS, CLOSED)

Provide a list view and a detail view in the backoffice UI.
```

## Flow

1. Formulate the functional task (see example prompt above).
2. Claude Code creates an implementation plan.
3. Review the plan briefly.
4. Start implementation.
5. Inspect the result in the application.
6. Review diff, structure, and tests at a high level.

## Focus

First maturity level: the agent does not work completely freely. It first provides a plan, and the human decides whether that plan is good enough.

## Learning

Agentic engineering starts with a reviewable plan, not with code.