---
name: insurance-quote-lifecycle
description: Implement or review the Quote accept/reject lifecycle, including PENDING-only transitions, premium and validity checks, rollback behavior, and Quote -> Policy -> Account tests.
---

# Insurance Quote Lifecycle

Use this skill when changing quote calculation, acceptance, rejection, policy creation, account
creation, or related UI actions.

## Service Invariants

`QuoteServiceCore` owns the state machine:

```text
PENDING -> ACCEPTED
PENDING -> REJECTED
```

Accept must require:

- status `PENDING`
- positive `calculatedPremium`
- `insuranceProduct`, `paymentFrequency`, and `effectiveDate`
- current date within `validFrom` and `validUntil`

Reject must require status `PENDING`.

## Required Tests

Cover illegal transitions in service or flow tests:

- accepted quote cannot be accepted again
- rejected quote cannot be accepted
- accepted quote cannot be rejected
- expired quote cannot be accepted
- quote without positive premium cannot be accepted
- downstream policy/account failure rolls back quote state

## Validation

```shell
./gradlew :webapp:test --tests "com.insurance.app.quote.*"
./gradlew :webapp:test --tests "com.insurance.app.policy.PolicyRollbackTest"
```

## Forbidden

- Relying only on disabled UI buttons for lifecycle safety.
- Creating a second policy/account from an already accepted quote.
- Catching downstream failures without rollback.
- Adding a new entry point that bypasses `QuoteServiceCore`.
