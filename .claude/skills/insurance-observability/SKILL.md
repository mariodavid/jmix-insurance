---
name: insurance-observability
description: Add or review business logging for this app's Quote -> Policy -> Account flows, including stable event names, MDC correlation, and non-PII diagnostic context.
---

# Insurance Observability

Use this skill when changing business-flow logging, Quote accept, Policy creation, Account creation,
or event listeners.

## Log Contract

Use stable event names that can be searched in demos and tests:

- `quote.accept.started`
- `quote.accept.policy-created`
- `quote.accept.completed`
- `quote.accept.failed`
- `policy.create.started`
- `policy.created`
- `policy.created.event-published`
- `policy.created.event-received`
- `account.create.started`
- `account.created`

Use MDC keys:

- `correlationId`
- `quoteNo`
- `policyNo`
- `partnerNo`

## Rules

1. Quote accept creates `correlationId` when absent and restores the previous MDC state.
2. Log business identifiers, not personal data or secrets.
3. Log failure paths with the same correlation/business ids as success paths.
4. Keep UI notifications separate from service logs.

## Validation

```shell
./gradlew :webapp:test --tests "com.insurance.app.quote.QuoteAcceptanceFlowTest"
./gradlew :webapp:test --tests "com.insurance.app.policy.PolicyRollbackTest"
```

## Forbidden

- Free-form one-off messages for core business events.
- Passwords, names, or other unnecessary PII in logs.
- Losing MDC context before downstream policy/account logs run.
