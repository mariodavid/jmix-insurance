# Testing Strategy

This document describes the test scopes, placement, shared infrastructure, data strategy, and
validation commands currently used by `jmix-insurance`.

## Test Objectives

The test suite covers four different questions:

1. Does one artifact start and expose valid Jmix metadata?
2. Does one domain's service or view behave correctly with its declared dependencies?
3. Do installed domains collaborate correctly in the assembled application and transaction?
4. Does the implementation still conform to the documented architecture and Jmix conventions?

Tests are placed at the narrowest scope that can answer the question without hiding a real module
boundary.

## Test Scopes

| Scope | Location | Runtime | Typical subject |
|---|---|---|---|
| API module | `<domain>-api/src/test` | Small Spring/Jmix test configuration | DTO metadata, IDs, instance names, API isolation |
| Core module | `<domain>-core/src/test` | Domain Core plus declared API dependencies | Service rules, listeners, persistence, local rollback |
| UI module | `<domain>-ui/src/test` | Domain UI with test configuration and selected mocks | View behavior, fragments, actions, data binding |
| Assembled application | `webapp/src/test` | Complete `WebappApplication` | Cross-domain flows, transactions, personas, installed UI |
| Architecture | `webapp/.../ArchitectureTest` | All production classes and repository resources | Module boundaries and Jmix conventions |

The presence of module tests does not remove the need for webapp tests. Module tests prove local
behavior against declared contracts; webapp tests prove that the real installed implementations
collaborate correctly.

## Test Selection

Use these placement rules for new tests:

| Change | Primary test scope |
|---|---|
| API DTO, enum, instance name, serialization-facing contract | API module test |
| One service, listener, entity rule, repository query | Core module test |
| One view or fragment with mocked foreign API | UI module `@UiTest` |
| Quote → Policy → Account behavior | Webapp integration test |
| Shared transaction or rollback across modules | Webapp integration test |
| Application role composition | Webapp unit test |
| Package, resource, descriptor, role, or persistence boundary | Central `ArchitectureTest` |

When a change affects both a local service and an assembled flow, add or update both scopes rather
than moving all assertions into the broad test.

## Shared Integration-Test Infrastructure

### BaseIntegrationTest

Webapp service integration tests extend `BaseIntegrationTest`, which provides:

- `@SpringBootTest` for the assembled application;
- the `test` Spring profile;
- authentication as administrator through `AuthenticatedAsAdmin`.

Tests do not use test-level `@Transactional`. Production transaction boundaries must execute as
they do at runtime, and persisted state or rollback is asserted through a fresh load.

### DatabaseCleanup

`DatabaseCleanup` removes business data before each assembled integration test. It deletes child
tables before parents and derives table names from Jmix metadata. Current cleanup covers
`AccountDocument`, `Account`, `Policy`, `Quote`, and `Partner`.

Tests call cleanup explicitly from `@BeforeEach`. If a new persistent aggregate participates in
webapp tests, add it in referentially safe order.

Some existing module-level tests still contain local JDBC cleanup. New assembled tests use the
shared component; nearby manual cleanup can be migrated when the affected test is changed.

## Test Data Strategy

### Domain-Owned Providers

Every persistent domain publishes defaults from its Core `testFixtures` source set:

- `PartnerDataProvider`;
- `QuoteDataProvider`;
- `PolicyDataProvider`;
- `AccountDataProvider`;
- `ProductDataProvider` for Product values;
- `UserDataProvider` for Security tests.

A provider implements `TestDataProvider<Entity>` and knows the minimal valid defaults for its own
model. Tests override only the values relevant to the scenario.

### EntityTestData

The shared `EntityTestData` component:

- creates entities through `DataManager.create()`;
- applies a domain provider and optional per-test customization;
- validates entities before saving;
- saves, loads all, and reloads by Jmix `Id`.

Typical usage:

```java
Quote quote =
    entityTestData.saveWithDefaults(
        new QuoteDataProvider(),
        q -> {
          q.setPaymentFrequency(PaymentFrequency.MONTHLY);
          q.setCalculatedPremium(new BigDecimal("120.00"));
        });
```

Tests do not construct Jmix entities directly. This keeps generated IDs, metadata behavior,
listeners, validation, and future defaults visible in test setup.

### Assertions

Domain Core and API `testFixtures` publish fluent AssertJ assertions next to their models. Module
tests import the local `Assertions` entry point. The assembled application uses
`InsuranceAssertions`, which aggregates Partner, Quote, Policy, Account, and AccountDocument
assertions.

Prefer domain assertions for stable business facts and standard AssertJ for collections, failures,
and incidental values.

## Test Shape

Behavior tests use explicit Given/When/Then phases in that order:

```java
// given
Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());

// when
quoteService.accept(Id.of(quote));

// then
Quote reloaded = entityTestData.reload(Id.of(quote));
assertThat(reloaded).hasStatus(QuoteStatus.ACCEPTED);
```

Reload persisted entities before asserting saved state. A managed object held by the test can make
an assertion pass without proving what was committed.

Cover at least:

- the successful state transition;
- rejected invalid input or invalid prior state;
- side effects in the owning domain;
- cross-domain side effects at webapp scope;
- rollback when transactional collaboration fails.

## UI Integration Tests

UI integration tests use:

```java
@UiTest
@SpringBootTest(classes = {WebappApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
```

Module UI tests can use a module-specific test configuration instead of the complete webapp and
mock foreign API interfaces. Shared helpers in `test-support-ui` include:

- `ViewInteractions` for opening and finding views;
- `FormInteractions` for fields and form values;
- `DataGridInteractions` for grid content and selection;
- `UiTestSupport` for common component interaction.

Stable component IDs are the preferred test contract. Some current tests still locate controls by
English labels or button text; these are existing migration points, not a pattern to extend. New or
changed UI tests should address components by stable IDs and use localized messages only for
asserting text that is itself the behavior.

A view test should assert observable behavior: saved state, service invocation, enabled state,
navigation, grid reload, section content, or notification. Merely opening a view is a smoke test,
not sufficient coverage of custom behavior.

## Cross-Domain and Transaction Tests

The main assembled examples are:

- `QuoteAcceptanceFlowTest`: Policy and Account are created, payment frequency controls document
  count, and repeated acceptance is rejected;
- `PolicyCreatedEventTest`: Policy event handling creates Account state;
- `PolicyRollbackTest`: an Account creation failure propagates and prevents Policy/Account commit;
- `AccountBalanceWithPolicyTest`: Account uses its local Policy snapshot for date validation;
- `RoleCompositionTest`: application personas contain the intended module capabilities;
- `PolicyUiTest` and `PartnerUiTest`: installed host views render cross-module contributions.

Cross-domain tests may load several domains' Core entities because `webapp` is the composition
scope. Production domain code must still use API contracts.

## Architecture Tests

`ArchitectureTest` is the assembled entry point for executable architecture. It is a test of
structure and metadata, not business behavior. Add a behavior test for business semantics even
when an architecture rule checks placement. Rule groups and failure handling are documented in
[Automated guardrails](automated-guardrails.md).

## Coverage

The root build aggregates JaCoCo data from all included builds. Generated auto-configuration and
persistent entity classes are excluded from the aggregate class directories. The dedicated
verification task requires:

- line coverage of at least 75%;
- branch coverage of at least 60%.

CI runs tests, creates the aggregate report, and verifies both thresholds after compilation and
static analysis succeed.

## Focused Commands

```shell
# One test class
./gradlew :webapp:test --tests "com.insurance.app.quote.QuoteAcceptanceFlowTest"

# One domain build
./gradlew :quote:check

# Architecture only
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"

# All tests across the composite build
./gradlew test

# CI-equivalent coverage verification
./gradlew test jacocoRootReport jacocoRootCoverageVerification
```

Run `./gradlew spotlessApply` before compile/check commands when Java files changed.

## Modification Checklist

1. Select the narrowest test scope that executes the real behavior under test.
2. Reuse a domain `*DataProvider`; add missing defaults in the owning fixture.
3. Use `EntityTestData` and Jmix factories instead of constructors.
4. Clean persisted data before each integration test and avoid test-level transactions.
5. Reload before asserting persisted state.
6. Use domain-specific fluent assertions for stable domain facts.
7. Add an assembled webapp test for cross-domain success and failure behavior.
8. Add stable IDs and shared interactions for UI tests.
9. Run the focused test, the owning module check, and the architecture suite.

## Primary Source Files

- [`BaseIntegrationTest`](../../webapp/src/test/java/com/insurance/app/test_support/BaseIntegrationTest.java)
- [`DatabaseCleanup`](../../webapp/src/test/java/com/insurance/app/test_support/DatabaseCleanup.java)
- [`EntityTestData`](../../test-support/src/main/java/com/insurance/common/test_support/EntityTestData.java)
- [`TestDataProvider`](../../test-support/src/main/java/com/insurance/common/test_support/TestDataProvider.java)
- [`QuoteDataProvider`](../../quote/quote-core/src/testFixtures/java/com/insurance/quote/core/test_support/QuoteDataProvider.java)
- [`InsuranceAssertions`](../../webapp/src/test/java/com/insurance/app/test_support/assertion/InsuranceAssertions.java)
- [`ViewInteractions`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/ViewInteractions.java)
- [`QuoteAcceptanceFlowTest`](../../webapp/src/test/java/com/insurance/app/quote/QuoteAcceptanceFlowTest.java)
- [`PolicyRollbackTest`](../../webapp/src/test/java/com/insurance/app/policy/PolicyRollbackTest.java)
