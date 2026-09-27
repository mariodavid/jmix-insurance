# ADR-0009: Use Domain-Owned Test Data and a Shared Test DSL

- **Status:** Accepted
- **Decision period:** 2026-05-31–2026-06-02
- **Recorded:** 2026-06-27
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0008: Align Test Scope with Module Boundaries](0008-align-test-scope-with-module-boundaries.md)

## Context

Jmix persistence tests need valid entity instances, authentication, database cleanup, and assertions
over stored state. Repeating that setup inline makes tests long and encourages incompatible local
factory patterns. Centralizing every fixture in `webapp`, however, would make domain test data depend
on the assembled application and would prevent module tests from reusing the same vocabulary.

Entity creation also has framework-specific requirements. Jmix entities should be created through
`DataManager` or metadata factories rather than constructors. Required attributes must be populated,
and persistence tests should observe committed and reloaded state rather than only the in-memory
object they modified.

The test harness needs a stable division of ownership similar to production code: generic mechanics
can be shared, while knowledge of what constitutes a valid Quote, Policy, Account, Partner, or User
belongs to the respective domain.

## Decision Drivers

- Create Jmix entities through framework factories and validate them before persistence.
- Give each domain one reusable definition of minimally valid default test data.
- Keep scenario-relevant deviations visible in the test case.
- Make domain fixtures available to module and assembled application tests without production
  classpath pollution.
- Express assertions in domain vocabulary rather than repeated field-by-field boilerplate.
- Provide consistent authentication and database cleanup for integration tests.
- Prevent coding agents from reintroducing obsolete factory and data-record patterns.
- Keep tests readable as Given/When/Then business examples.

## Considered Options

### 1. Build every entity inline in each test

Each test could create an entity with `DataManager.create()` and set every required property.

This is explicit but highly repetitive. A new mandatory attribute breaks many unrelated tests, and
different tests invent inconsistent defaults. The business condition under test becomes difficult
to distinguish from setup noise.

### 2. Use constructors, builders, or production factories

Tests could instantiate entities with `new`, use generated builders, or introduce production-facing
factory APIs for test convenience.

This conflicts with Jmix entity creation and risks bypassing metadata and enhancement behavior.
Production APIs should not exist solely to support test setup.

### 3. Keep all test factories in `webapp`

The assembled application could own factories for every domain entity.

This provides one central location but removes fixture ownership from the domains. Module tests
cannot depend on the application, and the central fixture package becomes a second domain model
that must understand every entity.

### 4. Combine generic test mechanics with domain-owned test fixtures

A neutral test-support library can create, validate, save, and reload entities. Each domain can
publish its own provider of valid defaults and its own fluent assertions through Gradle test
fixtures. Tests customize only the values relevant to their scenario.

This option shares mechanics without centralizing domain knowledge.

## Decision

Use a **shared generic test harness with domain-owned test data providers and assertions**.

The ownership split is:

| Artifact | Responsibility |
|---|---|
| `test-support` | Generic Jmix entity creation, validation, persistence helpers, and authentication support |
| Domain `testFixtures` | Valid defaults, domain-specific fluent assertions, and fixture types for that domain |
| `test-support-ui` | Reusable navigation, form, grid, and component interactions |
| `webapp` test support | Complete application context, cross-domain cleanup, and aggregate assertion entry point |
| Individual test | Scenario-specific values, action, and expected behavior |

## Entity Test Data

`EntityTestData` is the common entry point for persistent fixture creation. It creates entities
through `DataManager`, applies a domain `TestDataProvider`, allows an optional per-test customizer,
validates the result, and saves or reloads it as requested.

A typical scenario has this shape:

```java
Quote quote =
    entityTestData.saveWithDefaults(
        new QuoteDataProvider(),
        q -> q.setCalculatedPremium(new BigDecimal("300.00")));
```

The provider supplies a minimally valid baseline. The test overrides values that explain its
scenario. A provider must not hide the business condition being tested behind an opaque graph of
defaults.

Jmix entities are never instantiated with `new Entity()`. DTO entities needed in a test are also
created through Jmix metadata or `DataManager` when their metadata behavior matters.

## Domain-Owned Providers

Every provider lives in the owning domain's `testFixtures` source set. Examples include
`PartnerDataProvider`, `QuoteDataProvider`, `PolicyDataProvider`, `AccountDataProvider`, and
`UserDataProvider`.

This placement has three consequences:

- The domain owns the definition of valid defaults for its entities.
- Module tests can use the fixture without depending on `webapp`.
- The assembled application can consume published domain test fixtures without copying them.

Older `PartnerFactory`, `QuoteFactory`, `PolicyFactory`, and companion `*Data` record patterns are
not part of the current strategy and must not be reintroduced.

Default values may provide unique technical or business identifiers so tests can coexist. Values
that determine the expected outcome, such as premium, coverage date, payment frequency, or status,
remain explicit in the test.

## Assertion Vocabulary

Fluent assertion classes live with the domain test fixtures they describe. They expose domain
language such as `hasStatus`, `hasPolicyNo`, `hasBalance`, or `hasDocumentCount` and keep repetitive
property assertions out of scenarios.

Module tests import their local assertion entry point. The assembled application exposes
`InsuranceAssertions`, which aggregates the domain assertion types behind one static `assertThat`
import. The aggregator contains no new business semantics; those remain in the owning domain's
assertion classes.

Assertions verify externally meaningful state. After a service or UI action, tests reload entities
through `DataManager` before asserting persistence so that an unchanged in-memory instance cannot
mask a missing save or transaction problem.

## Authentication and Cleanup

Normal assembled integration tests extend `BaseIntegrationTest`, which supplies the test profile,
Spring Boot context, and authenticated administrator. Module integration tests use the shared
`AuthenticatedAsAdmin` extension where Jmix security requires a current authentication.

Business data in assembled tests is cleaned through `DatabaseCleanup` in dependency order. The
cleanup resolves physical table names through Jmix metadata instead of scattering hardcoded table
names through tests. Security user tests remove only the test users they created so that built-in
users and role assignments remain intact.

Tests do not rely on a test-managed `@Transactional` rollback. Production services and event
listeners define their own transaction behavior, and cross-module rollback is itself an observable
property. Explicit cleanup and reloading make the persisted outcome visible across transaction
boundaries.

## Test Shape

Test names follow `given_X_when_Y_then_Z`, with `given`, `when`, and `then` phases separated in the
body. Shared providers and assertions reduce infrastructure noise, but the scenario still states
the business-relevant input and result.

The same vocabulary principle applies to UI tests. `ViewInteractions`, `FormInteractions`, and
`DataGridInteractions` express user-level operations through stable view, component, and action
IDs. Selectors based on translated text are a fallback only when no stable ID exists.

## Consequences

### Intended benefits

- Tests share Jmix-correct creation, validation, authentication, and cleanup behavior.
- Valid defaults are maintained by the domain that owns the entity.
- Test fixtures can be reused by module and assembled application tests without entering production
  artifacts.
- Scenario code emphasizes the behavior under test instead of mandatory setup fields.
- Fluent assertions create a consistent domain-specific verification language.
- Schema and validation changes fail close to the owning provider rather than producing unrelated
  persistence errors throughout the suite.
- Coding agents have one canonical pattern for creating data and expressing expected state.

### Accepted costs and limitations

- Each domain must maintain its providers and assertion classes alongside model changes.
- Defaults can obscure important inputs if providers become too broad or magical.
- Shared helpers create conventions that contributors must learn before writing tests.
- `DatabaseCleanup` must be extended when new business tables participate in assembled tests.
- Random unique identifiers make exact fixture values unsuitable for assertions unless the test
  overrides or captures them.
- The application-level assertion aggregator requires a dependency on each domain's test fixtures.
- Explicit cleanup is more code than automatic transaction rollback but better reflects production
  transaction boundaries.

## Guardrails and Agent Guidance

- Use `EntityTestData` with the owning domain's `TestDataProvider`.
- Override only scenario-relevant values in the test.
- Add or update a provider in the owning domain's `testFixtures` when the entity model changes.
- Do not construct Jmix entities with `new`.
- Do not introduce replacement `*Factory` or `*Data` fixture hierarchies.
- Use local domain assertions in module tests and `InsuranceAssertions` in assembled tests.
- Reload persisted entities before asserting changes caused by a service or UI action.
- Use `DatabaseCleanup` for assembled business-flow tests instead of local hardcoded table lists.
- Clean security test users selectively.
- Do not add `@Transactional` to integration test classes to obtain automatic cleanup.
- Prefer stable IDs and actions in UI helpers; translated text is a fallback.

Some older tests predate this consolidated harness and still contain manual table cleanup or
text-based lookup. Those instances are migration debt and do not establish another approved
strategy.

## Evidence in the Repository

- [`EntityTestData`](../../test-support/src/main/java/com/insurance/common/test_support/EntityTestData.java) implements generic Jmix entity creation, validation, saving, and reloading.
- [`TestDataProvider`](../../test-support/src/main/java/com/insurance/common/test_support/TestDataProvider.java) defines the domain-provider contract.
- [`QuoteDataProvider`](../../quote/quote-core/src/testFixtures/java/com/insurance/quote/core/test_support/QuoteDataProvider.java) supplies valid Quote defaults from Quote's test fixtures.
- [`PartnerDataProvider`](../../partner/partner-core/src/testFixtures/java/com/insurance/partner/core/test_support/PartnerDataProvider.java), [`PolicyDataProvider`](../../policy/policy-core/src/testFixtures/java/com/insurance/policy/core/test_support/PolicyDataProvider.java), and [`AccountDataProvider`](../../account/account-core/src/testFixtures/java/com/insurance/account/core/test_support/AccountDataProvider.java) provide the equivalent domain-owned fixtures.
- [`DatabaseCleanup`](../../webapp/src/test/java/com/insurance/app/test_support/DatabaseCleanup.java) clears assembled business data in dependency order using Jmix table metadata.
- [`BaseIntegrationTest`](../../webapp/src/test/java/com/insurance/app/test_support/BaseIntegrationTest.java) supplies the shared application test context and authentication.
- [`InsuranceAssertions`](../../webapp/src/test/java/com/insurance/app/test_support/assertion/InsuranceAssertions.java) aggregates domain-owned fluent assertions for application tests.
- [`ViewInteractions`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/ViewInteractions.java), [`FormInteractions`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/FormInteractions.java), and [`DataGridInteractions`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/DataGridInteractions.java) provide the shared UI test DSL.
- [`insurance-testing`](../../.skills/insurance-testing/SKILL.md) gives coding agents the canonical examples, forbidden patterns, and focused validation commands.

## Follow-up Decisions

Separate ADRs may define generated fixture strategies for large data volumes, contract-test data for
remote services, and data anonymization if production-derived test data is introduced later.
