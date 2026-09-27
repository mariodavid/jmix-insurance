# ADR-0001: Use a Modular Monolith Built from Jmix Add-ons

- **Status:** Accepted
- **Decision date:** Before 2026-05-30 (earliest repository evidence)
- **Recorded:** 2026-08-08
- **Type:** Retrospectively documented decision

## Context

The application contains business capabilities with different ownership: Partner manages policyholder data, Quote owns offers, Policy owns insurance contracts, and Account owns the corresponding financial records. These capabilities collaborate in the Quote → Policy → Account flow, but they should not become one shared implementation model.

The module model also needs to support two different time horizons. Initially, the complete business flow should remain easy to run and change in one application. Later, an individual business capability may need to move into a separately deployed service. The initial architecture should make that extraction possible without imposing distributed-system concerns on every interaction from the beginning.

Jmix provides many productive features through a connected entity graph and a shared application runtime. Splitting that graph and its runtime across boundaries makes filtering, data loading, security, UI data binding, transaction management, and integration testing more involved. The architecture therefore needs a deliberate balance between domain separation and the advantages of a single Jmix application.

## Decision Drivers

- Give teams explicit ownership of individual business capabilities.
- Make dependencies between those capabilities visible in the build rather than relying only on package conventions.
- Keep the option of extracting selected capabilities into microservices without first untangling one shared implementation and persistence model.
- Start with one Spring Boot process and one database so business operations can use a single transaction where required.
- Avoid the operational cost, remote failure modes, and distributed consistency model of microservices until independent deployment is justified.
- Continue using standard Jmix add-on and Gradle mechanisms.

## Considered Options

### 1. One regular Jmix application

All business capabilities could live in one Jmix application and Gradle project, separated mainly through Java packages and development conventions.

This option has the lowest structural overhead and provides unrestricted use of Jmix features across one connected entity graph. It does not, by itself, create build-level ownership boundaries. Code in one business area can directly use the entities, services, views, and persistence details of another. Extracting a capability later would first require discovering and removing those dependencies.

### 2. One deployable application composed from Jmix add-ons

Each business capability can be implemented as an independently buildable and publishable Jmix add-on. A Gradle composite build keeps the add-ons in one repository, while a separate web application assembles them into one runtime.

This option creates compile-time boundaries and team-owned build units without introducing network communication or multiple runtime environments. The application can still use one database, one Spring application context, synchronous calls, and shared transactions.

### 3. Independently deployed microservices

Each business capability could own a separate application, deployment, and datastore and communicate through remote APIs or messages.

This option provides the strongest runtime isolation and independent deployment. It also requires remote failure handling, authentication between services, observability across processes, durable messaging where needed, retries, idempotency, and an explicit consistency model. It would remove the simple single-transaction model before the application has a demonstrated need for independent deployment.

## Decision

Structure `jmix-insurance` as a **modular monolith composed from independently buildable Jmix add-ons in a Gradle composite monorepo**.

Each business capability owns a domain build. The root composite includes those builds, and `webapp` acts as the composition root that selects their published-style coordinates and starts the complete application. The application is deployed as one Spring Boot process and initially uses one physical database.

The module boundaries must be strong enough that a future extraction starts at an existing contract and local model boundary. Extraction is an option, not an automatic outcome: replacing in-process collaboration with remote communication, assigning a separate datastore, and changing the consistency model remain explicit future work.

The internal API, Core, UI, and starter artifact structure is governed by follow-up decisions. This ADR decides the runtime and top-level module model, not every dependency rule inside a domain build.

## Consequences

### Intended benefits

- A team can own and evolve one business capability without treating the complete application as its implementation surface.
- Gradle classpaths expose or hide module artifacts deliberately, making accidental dependencies easier to detect.
- Each domain can be compiled, tested, and published independently while remaining immediately available inside the composite build.
- The complete business flow remains simple to start and test in one application context.
- Operations that require atomic consistency can initially participate in one transaction.
- A later microservice extraction does not begin by separating a shared JPA graph and moving unrelated domain code out of one application project.

### Accepted costs and limitations

- The repository contains more Gradle builds, artifacts, starters, configuration classes, and dependency declarations than a regular Jmix application.
- Decoupling increases implementation effort. Cross-domain use cases require explicit contracts, integration code, and tests.
- Jmix features based on navigating one entity graph become harder across domain boundaries. Generic filters, fetch plans, UI data binding, and row-level security cannot transparently traverse into another domain's model.
- Cross-domain views and queries may require API calls, local read models, copied reference data, or custom UI components.
- A single runtime still has shared resource and failure characteristics. Build modularity does not provide runtime isolation.
- A future extraction still requires remote communication, a separate operational model, and revised transaction and consistency semantics.

## Guardrails and Agent Guidance

The number of modules is not the purpose of this decision. The boundary exists to preserve domain ownership and a credible extraction path.

When changing the application:

- Start in the domain that owns the business capability.
- Do not add a dependency on another domain's implementation merely because all modules run in one process.
- Treat cross-domain entity navigation as a design decision, not as a convenience available through the shared database.
- Preserve the single-runtime transaction semantics where the business flow currently depends on them; do not introduce asynchronous or remote semantics implicitly.
- Run the focused compile and tests for the affected domain before verifying the assembled application.

Detailed rules for public API artifacts, persistent model ownership, UI composition, transaction boundaries, and executable architecture belong in separate ADRs.

## Evidence in the Repository

- The root [`settings.gradle`](../../settings.gradle) includes the domain builds through `includeBuild`.
- Domain settings such as [`partner/settings.gradle`](../../partner/settings.gradle) declare the artifacts owned by one business capability.
- [`webapp/build.gradle`](../../webapp/build.gradle) assembles the domain add-ons through Maven-style coordinates.
- [`article-draft.md`](../article-draft.md#3-choosing-the-runtime-and-module-model) documents the runtime alternatives and the module model.
- [`PolicyRollbackTest`](../../webapp/src/test/java/com/insurance/app/policy/PolicyRollbackTest.java) demonstrates that the shared runtime can provide one transaction across module collaboration.

## Follow-up Decisions

Separate ADRs should document:

- the API/Core/UI artifact split inside each domain;
- API-only communication between domains;
- consumer-owned persistent references instead of cross-domain JPA associations;
- synchronous event delivery for Policy → Account;
- cross-domain UI contribution contracts;
- executable architecture rules.
