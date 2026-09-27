# ADR-0003: Create Policy and Accounting Records Atomically

- **Status:** Accepted
- **Decision date:** Before 2026-05-30 (earliest repository evidence)
- **Recorded:** 2026-08-08
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0001: Use a Modular Monolith Built from Jmix Add-ons](0001-use-a-modular-monolith-built-from-jmix-add-ons.md), [ADR-0002: Apply Asymmetric API/Core/UI Layering](0002-apply-asymmetric-api-core-ui-layering.md)

## Context

Accepting a quote creates an insurance policy and establishes the premium that must be collected for its coverage period. In the current business model, maintaining an active Policy without a corresponding Account and its payment documents is not a useful valid outcome. The accounting representation is required to collect the premium created by policy conclusion.

ADR-0001 deliberately keeps the initial application in one process and one physical database. That deployment model can enforce the Policy and Account consistency requirement with one database transaction. Introducing asynchronous messaging inside this deployment would give up that advantage before a separate runtime or datastore makes eventual consistency necessary.

ADR-0002 also requires Policy and Account to remain separate bounded contexts. Policy must not create Account entities or depend on Account's implementation. The integration mechanism therefore needs to preserve domain dependency direction while still producing one atomic business outcome.

## Decision Drivers

- Do not commit a newly concluded Policy without the accounting representation needed to collect its premium.
- Use the single-process, single-database deployment model to provide one transaction across the participating modules.
- Keep Policy Core independent of Account API, Core, and persistence types.
- Keep Account responsible for translating policy data into its own Account and payment-document model.
- Avoid Kafka, an outbox, retries, idempotency infrastructure, and an eventual-consistency state model until Account is extracted into another service.
- Preserve an explicit integration event that can later become a durable message boundary.

## Considered Options

### 1. Call Account directly from Policy

`PolicyService` could call `AccountService` after saving the Policy. With both services in the same Spring application and transaction, Account failure could still roll back Policy creation.

The control flow would be explicit, but Policy would need a compile-time dependency on Account's API. Policy would know which downstream capability must react to policy creation, and each additional reaction would add another dependency to Policy's orchestration logic.

### 2. Publish a synchronous in-process event in the current transaction

Policy can publish `PolicyCreatedEvent` after saving the new Policy. Account owns an event listener and creates its accounting representation on the publisher's thread. The listener and Account service join the existing transaction, and any failure propagates to the caller.

This option keeps Policy free of an Account dependency while preserving atomic consistency in the deployment monolith.

### 3. Process the event after commit or asynchronously

An `AFTER_COMMIT` listener, `@Async` handler, or similar in-process mechanism would allow Policy creation to commit before Account is created.

This option shortens the Policy transaction and reduces synchronous runtime coupling. It also creates a valid intermediate state with a Policy but no Account. Reliable recovery would require durable delivery, retries, idempotent account creation, failure visibility, and a defined business state for incomplete provisioning. An ordinary asynchronous Spring event would not provide those guarantees.

### 4. Publish through Kafka using a Transactional Outbox

Policy could write a message to a Transactional Outbox (TXO) in the same transaction as the Policy. A relay would publish it to Kafka, and Account would consume it in its own transaction.

This is the intended direction when Policy and Account no longer share a process and database. It provides durable cross-service delivery but changes the consistency model from one atomic database transaction to eventual consistency. It also requires outbox processing, at-least-once delivery handling, idempotent consumers, retries, monitoring, and an explicit incomplete-provisioning state.

Those costs are not justified for the initial deployment monolith.

## Decision

Use a **synchronous Spring application event inside the current transaction** to create the Account and payment documents for a new Policy.

`PolicyServiceCore.createPolicy()` saves the Policy and publishes `PolicyCreatedEvent`. The event is part of `policy-api` and carries the identifiers and commercial values Account needs. It does not expose the Policy entity.

`PolicyCreatedEventListener` in Account handles the event through a normal synchronous `@EventListener`. It invokes Account's creation service on the publishing thread. With Spring's default transaction propagation, Account creation joins the transaction that created the Policy. When policy creation is initiated through quote acceptance, the outer Quote transaction participates in the same outcome.

Account creation failure must propagate. The transaction must not commit the new Policy, its Account, partial payment documents, or the accepted Quote state. Saving the Policy before publishing the event does not make it durable independently; the save remains uncommitted until the complete transaction succeeds.

The event is an in-process integration boundary, not an asynchronous message. It separates compile-time dependency direction while deliberately retaining synchronous runtime and transactional coupling.

## Business Invariant

At the end of a successful policy-conclusion transaction:

- the Quote is accepted when the operation started from quote acceptance;
- the Policy exists;
- an Account referencing that Policy exists;
- the Account contains the payment documents required by the selected payment frequency.

If the accounting representation cannot be created, policy conclusion fails as a whole.

## Consequences

### Intended benefits

- The initial solution enforces the business invariant with one local transaction and no distributed infrastructure.
- Callers receive immediate success or failure for the complete policy-conclusion outcome.
- Policy Core does not depend on Account; Account opts into the public Policy event.
- Account owns the transformation from policy premium and payment frequency to accounting records.
- Integration and rollback behavior can be tested deterministically in the assembled application.
- `PolicyCreatedEvent` provides a clear seam for a later move to durable messaging.

### Accepted costs and limitations

- Policy creation is synchronously dependent on successful Account processing even though there is no compile-time dependency from Policy to Account.
- Account work increases the duration and scope of the policy-conclusion transaction.
- An Account defect or temporary failure prevents Policy creation.
- In-process Spring events are not durable messages and cannot cross a process boundary.
- Event-based control flow is less visible than a direct method call and requires focused integration tests and logging.
- Additional synchronous listeners would extend the same transaction and need explicit review; publishing an event is not permission to add unlimited work to policy conclusion.
- The event payload is a public boundary model and must evolve without exposing Policy Core entities.

## Migration When Account Is Extracted

Moving Account into a separate service requires a new consistency decision and must supersede the synchronous part of this ADR.

The intended migration path is:

1. Write a durable policy-created record to a Transactional Outbox in the same local transaction as the Policy.
2. Publish the outbox record to Kafka outside the Policy transaction.
3. Let Account consume the event and create its local representation in its own transaction.
4. Make Account consumption idempotent because delivery may occur more than once.
5. Add retries, dead-letter or failure handling, observability, and reconciliation.
6. Model and expose the state in which a Policy exists while accounting provisioning is still pending or has failed.

Kafka and the outbox preserve reliable handover; they do not preserve the current cross-domain database transaction. The business must explicitly accept and represent eventual consistency before that migration.

## Guardrails and Agent Guidance

- Keep `PolicyCreatedEvent` in `policy-api` and free of Policy Core entities.
- Keep the current listener synchronous. Do not add `@Async` or move it to `AFTER_COMMIT` as a local optimization.
- Propagate Account creation failures; do not log and swallow them.
- Do not add a direct Policy Core dependency on Account to make the flow easier to follow.
- Do not introduce remote calls into the current transaction.
- Preserve or extend the rollback test whenever policy or accounting creation changes.
- When adding another listener, decide explicitly whether its work belongs to the atomic policy-conclusion invariant.
- Do not replace the event with Kafka alone. A distributed implementation requires the complete outbox, idempotency, retry, observability, and business-state design described above.

## Evidence in the Repository

- [`QuoteServiceCore.accept()`](../../quote/quote-core/src/main/java/com/insurance/quote/core/service/QuoteServiceCore.java) opens the transaction for the complete quote-acceptance flow.
- [`PolicyServiceCore.createPolicy()`](../../policy/policy-core/src/main/java/com/insurance/policy/core/service/PolicyServiceCore.java) saves the Policy and publishes the event inside a transaction.
- [`PolicyCreatedEvent`](../../policy/policy-api/src/main/java/com/insurance/policy/api/event/PolicyCreatedEvent.java) carries the data needed by Account without exposing a Policy entity.
- [`PolicyCreatedEventListener`](../../account/account-core/src/main/java/com/insurance/account/core/listener/PolicyCreatedEventListener.java) handles the event synchronously and propagates failures.
- [`AccountServiceCore.createAccount()`](../../account/account-core/src/main/java/com/insurance/account/core/service/AccountServiceCore.java) creates the Account and payment documents transactionally.
- [`PolicyRollbackTest`](../../webapp/src/test/java/com/insurance/app/policy/PolicyRollbackTest.java) proves that Account failure rolls back Policy creation.
- [`QuoteAcceptanceFlowTest`](../../webapp/src/test/java/com/insurance/app/quote/QuoteAcceptanceFlowTest.java) verifies the assembled Quote → Policy → Account result.
- [`article-draft.md`](../article-draft.md#42-boundaries-in-action-from-quote-to-policy-to-account) documents the synchronous flow and the later asynchronous alternative.

## Follow-up Decisions

Separate ADRs should document:

- the Account-owned persistent representation of Policy data;
- the detailed event schema and compatibility policy if the event becomes a Kafka message;
- the eventual-consistency state model when Policy and Account are deployed separately.
