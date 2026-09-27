# ADR-0002: Apply Asymmetric API/Core/UI Layering

- **Status:** Accepted
- **Decision date:** 2026-05-30–2026-05-31 (earliest repository evidence)
- **Recorded:** 2026-08-08
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0001: Use a Modular Monolith Built from Jmix Add-ons](0001-use-a-modular-monolith-built-from-jmix-add-ons.md)

## Context

ADR-0001 establishes independently owned business domains inside one deployable Jmix application. A domain build boundary alone does not decide which parts of a domain another domain may use. Without an additional rule, Quote could depend on Policy Core and reference the Policy JPA entity directly. The application would have several Gradle projects but still one cross-domain entity graph and one logically monolithic database model.

The purpose of treating Partner, Quote, Policy, and Account as bounded contexts is to include their persistent models in the boundary. Policy must own the Policy entity and its schema. Quote may request creation of a policy or retain a local policy reference, but it must not extend its own JPA model with a relation to Policy's entity.

Jmix creates a competing pressure. Flow UI data containers, `DataContext`, fetch plans, generic filtering, entity and row-level security, and standard CRUD views work best with persistent entities. Requiring even a domain's own UI to use DTOs would give the strongest technical separation, but it would discard a substantial part of Jmix's entity-based development model and introduce mapping and service APIs for ordinary in-domain UI work.

The vertical layering therefore needs to protect bounded-context boundaries without treating a domain's own UI as an external consumer of its persistence model.

## Decision Drivers

- Keep each bounded context's JPA model and logical database model private.
- Prevent Gradle dependencies between foreign Core artifacts from turning the modular monolith back into a shared entity graph.
- Preserve a credible path to separating a domain's datastore during a later microservice extraction.
- Retain Jmix's entity-based UI, data loading, filtering, validation, and security features inside the owning domain.
- Avoid DTO mapping and service-level query abstractions where no bounded-context boundary is crossed.
- Give other domains and future delivery adapters one explicit contract for invoking a domain.
- Keep business logic independent of Flow UI.

## Meaning of API

An `*-api` artifact is the **external contract of a bounded context**. External means outside the owning domain, not necessarily outside the process and not necessarily HTTP.

The primary consumers are other modules in the modular monolith. A future REST API, messaging adapter, or separately deployed application must use the same domain contract rather than bypassing it through Core entities or direct database access. Swapping one implementation for another is not a decision driver; hiding the persistent model and preserving domain ownership are.

The matching `*-api-starter` artifacts follow the standard Jmix add-on packaging mechanism. Their existence is implementation plumbing for registering the API add-on, not a separate architectural decision and not evidence that the contract is a REST API.

## Considered Options

### 1. No hard API boundary

Domain modules could depend directly on one another's Core artifacts. Quote could use the Policy entity, create a JPA association to it, or query it directly from Java or a Jmix view descriptor.

This option retains the complete connected entity graph. Cross-domain screens, fetch plans, generic filters, and JPA queries are straightforward, and DTO mapping is rarely needed. The Gradle modules would provide source organization, but not independent domain models. A later database or service split would first have to find and replace foreign JPA relations, queries, fetch plans, security rules, and UI bindings.

### 2. Use API contracts for every access, including a domain's own UI

Every consumer of a domain, including its own UI artifact, could use services and DTOs from `*-api`. Core entities would remain inaccessible outside Core.

This option provides the strongest vertical isolation and makes UI another replaceable delivery adapter. It also requires DTOs, mapping, service operations, and explicit query models for normal list and detail views. Jmix UI components would have to operate on DTO entities or custom data loaders instead of the persistent entities they edit. Standard persistence-backed filtering, sorting, pagination, `DataContext` save behavior, fetch plans, and entity-aware security would either be unavailable at the UI boundary or need explicit replacement behind the service contract.

The additional separation inside one bounded context does not justify losing those Jmix features and carrying that mapping cost for every view.

### 3. Use an asymmetric boundary

A domain is split into API, Core, and UI artifacts. Its own UI may use its Core entities and services directly. Any access that crosses into another bounded context must use that context's API types.

This option keeps the hard boundary where domain ownership changes. It preserves the normal Jmix entity workflow inside the boundary while preventing a shared persistent model between domains.

## Decision

Use **asymmetric API/Core/UI layering** inside every business domain.

| Consumer | Allowed access |
|---|---|
| `<domain>-api` | Contract types and framework metadata required for those contracts; no Core or UI implementation |
| `<domain>-core` | Its own API and implementation model; foreign domains only through their API contracts |
| `<domain>-ui` | Its own API and Core entities/services; foreign domains only through API or explicit UI API contracts |
| REST, messaging, or another application | The owning domain's API contract |

API owns service interfaces, DTOs, events, and other types intentionally exposed outside the bounded context. It contains no persistent entities, repositories, Liquibase changelogs, or UI implementations.

Core owns the persistent model, business behavior, schema migrations, event listeners, and implementations of the public contracts. One Core artifact must not depend on another domain's Core artifact.

UI owns Flow UI views and interaction logic. It may bind directly to persistent entities from its own Core artifact and use local Core services. When it needs data or behavior from another domain, it uses that domain's service interfaces and DTOs. Business rules remain in Core even though the local entity model is available to UI.

One physical application database does not change this rule. The database is shared operationally, while the entity models and schema ownership remain logically separated by bounded context.

## Consequences

### Intended benefits

- A Gradle module boundary also protects the bounded context's JPA model instead of only organizing source files.
- Cross-domain dependencies reveal the contract being used; consumers do not gain accidental access to repositories, entity lifecycle, or persistence implementation details.
- Domain entities and tables can be separated later without first removing foreign JPA associations throughout the application.
- A domain's own Jmix UI retains direct entity binding, `DataContext`, fetch plans, persistence-backed filtering and sorting, and the existing entity security model.
- The same external application contract can be reused by other modules, REST endpoints, message adapters, or a future remote client.
- Core remains usable without Flow UI and keeps business behavior outside view controllers.

### Accepted costs and limitations

- Cross-domain use cases require DTO mapping and explicit service or event contracts.
- A consuming domain must keep its own representation of foreign references and of any copied values it needs locally.
- Copied values introduce decisions about snapshots, synchronization, and staleness.
- Generic filters, JPQL, fetch plans, and row-level security cannot transparently navigate from one domain into another.
- Cross-domain screens may require read services, local read models, custom filtering, or explicit UI composition.
- The asymmetric rule is more subtle than either unrestricted entity access or DTO-only access everywhere. It requires documentation and executable checks.
- API, Core, UI, and their Jmix starter artifacts add build configuration and navigation overhead.
- A domain's UI and Core are intentionally coupled at build time; they are not independently deployable parts of that domain.

## Guardrails and Agent Guidance

When implementing a change, first determine which bounded context owns the data or behavior.

- In the owning domain's UI, use the local persistent entity and normal Jmix data components when that is the simplest correct implementation.
- Across domains, depend on the owning domain's `*-api` artifact and use its service, DTO, or event contract.
- Do not add a foreign `*-core` dependency, import a foreign entity, create a cross-domain JPA association, or use a foreign Jmix entity name in JPQL or XML.
- If an existing API does not expose the required use case, extend the owning domain's contract rather than querying its tables from the consumer.
- Treat DTOs as boundary models, not as a second persistent model or a global canonical entity.
- Keep consumer-owned references and copied values inside the consuming domain; their exact persistence convention is governed by a separate ADR.
- A REST or messaging adapter must invoke the owning domain's public contract instead of accessing its Core persistence model directly.
- Keep validation and business state transitions in Core services even when the local UI binds directly to an entity.

The focused architecture suite must remain green after changes to layer or domain dependencies:

```shell
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

## Evidence in the Repository

- [`quote-ui.gradle`](../../quote/quote-ui/quote-ui.gradle) exposes Quote Core to Quote UI as a local dependency.
- [`QuoteDetailView`](../../quote/quote-ui/src/main/java/com/insurance/quote/ui/view/quote/QuoteDetailView.java) binds to the local `Quote` entity while loading foreign Partner data through `PartnerService` and `PartnerDto`.
- [`quote-detail-view.xml`](../../quote/quote-ui/src/main/resources/com/insurance/quote/ui/view/quote/quote-detail-view.xml) uses the persistent local Quote entity and the foreign `partner_api_PartnerDto` metadata type in the same view.
- [`quote-core.gradle`](../../quote/quote-core/quote-core.gradle) depends on Partner and Policy API starters rather than their Core artifacts.
- [`PartnerService`](../../partner/partner-api/src/main/java/com/insurance/partner/api/service/PartnerService.java) explicitly identifies itself as the public contract for foreign consumers.
- [`ModuleLayerDependencyRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/layer/ModuleLayerDependencyRules.java) enforces API/Core/UI direction and the distinction between local and foreign Core access.
- [`article-draft.md`](../article-draft.md#44-the-api--core--ui-contract) documents the two-dimensional structure and its deliberately asymmetric access rules.

## Follow-up Decisions

Separate ADRs should document:

- consumer-owned persistent references and copied cross-domain values;
- synchronous event delivery for Policy → Account;
- cross-domain UI contribution contracts;
- executable architecture rules for Java, Gradle, JPQL, and XML dependencies.
