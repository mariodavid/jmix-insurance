# ADR-0005: Store Cross-Domain References as Local Domain Representations

- **Status:** Accepted
- **Decision date:** 2026-06-07 (earliest repository evidence)
- **Recorded:** 2026-06-12
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0001: Use a Modular Monolith Built from Jmix Add-ons](0001-use-a-modular-monolith-built-from-jmix-add-ons.md), [ADR-0002: Apply Asymmetric API/Core/UI Layering](0002-apply-asymmetric-api-core-ui-layering.md), [ADR-0003: Create Policy and Accounting Records Atomically](0003-create-policy-and-accounting-records-atomically.md)

## Context

ADR-0002 prevents one bounded context from using another context's persistent entities. That rule
still leaves open how a consumer should retain a durable reference to a foreign concept. Account
needs to associate financial records with a Policy, Policy needs to identify its policyholder, and
Quote needs to retain both its selected Partner and the Policy created during acceptance.

A direct JPA association would connect the persistent models again. Even if the Java sources were
organized into separate Gradle modules, a relation such as `Account.policy -> Policy` would create
one cross-domain entity graph. JPQL queries, fetch plans, database foreign keys, joins, entity
lifecycle assumptions, and security rules could then grow across the intended boundary. Extracting
one domain into a service with its own database would first require finding and dismantling those
connections.

Storing only a foreign UUID avoids the JPA association, but treats the relationship as a technical
pointer. A consuming bounded context may need its own interpretation of the referenced concept and
selected values for local search, display, decisions, or audit. A Policy from Accounting's point of
view is not the complete Policy aggregate from Policy Core. Accounting needs only the part that is
meaningful to its own model.

The application should therefore model domain boundaries as if the domains already had separate
databases, while retaining the operational simplicity of one physical database for the initial
deployment.

## Decision Drivers

- Keep the persistent model of each bounded context private, including at the database-model level.
- Prevent apparently separate Java modules from becoming one model through JPA associations,
  foreign keys, or operational SQL joins.
- Let the consuming domain define its own interpretation of a foreign concept.
- Store the values needed for local search and processing without repeated service or REST calls.
- Avoid copying the complete foreign entity when the consumer needs only selected attributes.
- Make a later move to a separate service and datastore start at an existing data boundary.
- Preserve a path from in-process events to durable messaging and eventual consistency when a
  domain is extracted.

## Considered Options

### 1. Reference the foreign JPA entity directly

Account could declare a JPA relationship to `Policy`, and Quote and Policy could reference the
Partner entity in the same way.

This option preserves the connected Jmix entity graph. It enables direct navigation, fetch plans,
filters, security rules, database foreign keys, and cross-domain joins. It also makes the foreign
entity lifecycle and schema part of the consumer's implementation. The Gradle modules would remain
separate in name while the persistence model and database stayed monolithic.

This option is rejected because the database-model boundary is part of the bounded-context
boundary.

### 2. Store only foreign identifiers as ungrouped fields

A consumer could store fields such as `policyId` or `partnerId` directly on its entity without a
JPA association.

This option removes the entity dependency and is sufficient when the consumer needs only an opaque
pointer. It does not express a local domain concept and scales poorly when the consumer needs a
business key or selected searchable attributes. Related fields become an incidental collection of
columns rather than a representation owned by the consuming model.

### 3. Use a shared persistent reference type

The domains could share a common `PolicyReference` or `PartnerReference` class.

This option avoids repeating the reference structure but establishes a canonical model outside the
bounded contexts. Consumers could no longer evolve their interpretations independently. Accounting,
Quote, and Policy do not necessarily need the same Partner or Policy attributes, semantics, or
update behavior.

### 4. Let each consumer own an embedded local representation

Each bounded context can define a small `@Embeddable` Jmix type representing the foreign concept
from its own point of view. The representation contains only the values required by that consumer
and is stored in the consumer's table.

This option duplicates selected values deliberately but keeps persistence, querying, and domain
meaning local.

## Decision

Store durable cross-domain references as **consumer-owned local domain representations**.

The local representations are Jmix `@Embeddable` types located in the consuming Core module:

- Account owns `AccountPolicyReference`.
- Quote owns `QuotePartnerReference` and `QuotePolicyReference`.
- Policy owns `PolicyPartnerReference`.

A reference type is not a partial copy of the foreign entity and not a shared integration model. It
expresses what the foreign concept means to the consumer. Each consumer selects its own fields for
identity, business keys, local search, display, decisions, or audit. It must not copy fields merely
because they exist on the source entity.

Values enter the consumer through the owning domain's public contract. Depending on the use case,
that contract may be a synchronous command or query returning a DTO, or an integration event
carrying the values required by downstream consumers. The consumer persists the required values
locally and does not perform an on-demand service or REST call whenever it needs to search or
process its own data.

Cross-domain references do not create:

- a JPA association to the foreign entity;
- a database foreign key to the foreign domain's table;
- a shared persistent reference class;
- an operational query or join over the foreign domain's tables.

The domains currently share one physical database as a deployment convenience. That physical
proximity does not make foreign tables part of a domain's operational model.

## Update Semantics

The current local references contain identifiers and business keys that cannot be changed through
the implemented business processes. No synchronization mechanism is required for those values
today.

If a consumer later copies mutable attributes for search, display, decisions, or audit, the
semantics of every copied value must be explicit:

- A historical snapshot remains unchanged intentionally.
- A current projection is updated from an event published by the owning domain.
- A stable identifier or business key changes only if the owning domain explicitly supports that
  change and defines its propagation.

Current projections must be maintained through event-based propagation rather than repeated
on-demand reads. In the modular monolith, the event can be delivered as a Spring application event.
When the domains are deployed separately, the intended migration path is durable messaging such as
Kafka combined with a Transactional Outbox.

Durable asynchronous propagation introduces eventual consistency. Consumers must then tolerate a
delay between the source change and their local update and must handle at-least-once delivery,
idempotency, retries, failure visibility, and reconciliation. Those mechanisms are required when
mutable copied values are introduced across a deployment boundary; they are not implemented
preemptively for the current immutable references.

## Reporting Exception

Operational domain code must not query or join foreign domain tables. Otherwise, queries and read
paths would recreate the database coupling avoided by the local representations.

A central reporting or analytics component may read across the shared physical database while the
application is deployed as a monolith. Such queries belong outside the operational bounded
contexts, must not implement domain invariants or transactional business behavior, and must be
treated as dependent on the current deployment topology.

If a domain is extracted to a separate datastore, cross-domain reporting must move to an explicit
reporting read model, replicated analytical store, data warehouse, or another integration
mechanism. The reporting exception is not permission for a domain module to depend on foreign
tables.

## Consequences

### Intended benefits

- Each bounded context owns its complete operational model, including references to foreign
  concepts.
- A local reference can evolve according to the consumer's needs without exposing or copying the
  complete source entity.
- Consumers can search and process required attributes locally without runtime calls to the owning
  domain.
- Gradle separation is reinforced by a matching JPA and logical database separation.
- Foreign entity lifecycle, fetch plans, schema changes, and persistence implementation remain
  outside the consumer.
- A later service extraction does not first require removing cross-domain JPA associations,
  foreign keys, or operational joins.
- Events and API DTOs form an explicit place for data transfer and later transport changes.

### Accepted costs and limitations

- Selected data is duplicated across bounded contexts.
- A copied value can become stale unless its snapshot or update semantics are defined.
- Mutable projections require events, synchronization logic, monitoring, and eventually
  reconciliation.
- There is no database-enforced referential integrity between domains.
- Jmix fetch plans, GenericFilter, JPQL, and entity or row-level security cannot transparently
  navigate into the foreign entity graph.
- Cross-domain operational queries require an API, a local read model, or copied searchable data
  instead of a convenient SQL join.
- Separate local reference types repeat some structure and require their own mapping, Liquibase,
  messages, fixtures, and tests.
- Central reporting over the shared database is coupled to the monolithic deployment and must be
  redesigned when datastores are separated.

## Guardrails and Agent Guidance

- When a persistent entity needs to refer to a foreign domain concept, create a representation in
  the consuming domain's Core entity package.
- Use a consumer-specific `@Embeddable` rather than a foreign entity, shared reference entity, or
  unstructured copy of the complete source DTO.
- Select fields from the consumer's use cases: identity, business key, local search, display,
  decisions, and audit.
- Document whether every copied mutable value is a historical snapshot or a current projection.
- Populate local references only through public API DTOs, commands, or events.
- Do not add a foreign Core dependency, JPA association, database foreign key, foreign entity name,
  or operational cross-domain SQL join.
- If current copied values need updates, add an explicit event and test the consumer's idempotent
  update behavior.
- Keep central reporting queries outside domain modules and do not use them to implement business
  invariants.
- Update the consuming module's Liquibase changelog, UI bindings, messages, fixtures, and tests when
  its local representation changes.

The focused architecture suite must remain green after changes to persistent references or domain
queries:

```shell
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

## Evidence in the Repository

- [`AccountPolicyReference`](../../account/account-core/src/main/java/com/insurance/account/core/entity/AccountPolicyReference.java) represents Policy from Account's point of view.
- [`QuotePartnerReference`](../../quote/quote-core/src/main/java/com/insurance/quote/core/entity/QuotePartnerReference.java) and [`QuotePolicyReference`](../../quote/quote-core/src/main/java/com/insurance/quote/core/entity/QuotePolicyReference.java) represent Quote's foreign references.
- [`PolicyPartnerReference`](../../policy/policy-core/src/main/java/com/insurance/policy/core/entity/PolicyPartnerReference.java) represents the policyholder inside Policy.
- [`PolicyCreatedEvent`](../../policy/policy-api/src/main/java/com/insurance/policy/api/event/PolicyCreatedEvent.java) transfers the values Account needs without exposing the Policy entity.
- [`AccountServiceCore`](../../account/account-core/src/main/java/com/insurance/account/core/service/AccountServiceCore.java) creates Account's local Policy representation from the event data.
- [`EmbeddedReferenceConventionRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/entity/EmbeddedReferenceConventionRules.java) enforces the local embedded-reference convention.
- [`PersistentEntityNameBoundaryRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/jmix/PersistentEntityNameBoundaryRules.java) rejects foreign entity-name references in Java and XML resources.
- [`article-draft.md`](../article-draft.md#451-local-models-at-domain-boundaries) describes local models and the Jmix entity-graph trade-off.

## Follow-up Decisions

Separate ADRs should document:

- cross-domain UI composition through consumer-owned views and provider-owned contributions;
- the event schema and compatibility policy when mutable projections are introduced;
- the concrete outbox, broker, retry, and reconciliation design when a domain is extracted;
- the reporting architecture when cross-domain SQL access is no longer physically possible.
