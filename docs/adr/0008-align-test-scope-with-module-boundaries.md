# ADR-0008: Align Test Scope with Module Boundaries

- **Status:** Accepted
- **Decision period:** 2026-05-31–2026-06-05
- **Recorded:** 2026-06-24
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0001: Use a Modular Monolith Built from Jmix Add-ons](0001-use-a-modular-monolith-built-from-jmix-add-ons.md), [ADR-0003: Create Policy and Accounting Records Atomically](0003-create-policy-and-accounting-records-atomically.md), [ADR-0004: Enforce Module Boundaries with Architecture Tests](0004-enforce-module-boundaries-with-architecture-tests.md)

## Context

The modular monolith needs two forms of confidence that pull in different directions. A bounded
context must be testable without loading every other domain implementation, otherwise its API
boundary and independent build have little practical value. The assembled application must also be
tested as a whole because important behavior emerges only when real modules collaborate.

Quote acceptance illustrates the difference. Quote Core can verify its own state transition and
the request sent to `PolicyService` without loading Policy or Account. It cannot prove that the real
Policy implementation publishes the expected event, that Account creates payment documents, or
that a listener failure rolls the complete operation back. Those properties belong to the assembled
application.

Jmix adds another distinction. Core integration tests need metadata, persistence, security, and a
database. Flow UI interaction tests need the Vaadin and Jmix UI test runtime. Loading the complete
`webapp` for every local behavior would provide broad fidelity at the price of slower feedback,
larger fixtures, and weaker evidence that a domain can operate behind its public contracts.

The test location and runtime must therefore follow the same boundaries as production code.

## Decision Drivers

- Prove that a domain can be built and exercised without foreign Core implementations.
- Keep feedback for local behavior focused and reasonably fast.
- Test public API collaboration rather than implementation coupling.
- Verify real cross-domain flows, event delivery, and transaction semantics where modules are
  assembled.
- Test Jmix Flow UI behavior without requiring a full browser for every interaction.
- Give developers and coding agents a predictable answer to where a new test belongs.
- Avoid both an all-mocked test suite and an all-`webapp` integration suite.

## Considered Options

### 1. Test only individual classes and modules

Every domain could test its services and views with mocks for all collaborators.

This option gives focused failures and fast feedback. It cannot prove starter assembly, real event
listeners, transaction propagation across modules, role composition, or the final Quote → Policy →
Account result. Mocks can agree with a caller while the real provider has evolved differently.

### 2. Test all behavior through the assembled `webapp`

Every test could start the complete application and use all real domain implementations.

This option exercises a realistic runtime but makes local behavior dependent on unrelated modules,
schema, security, and startup configuration. Failures have a larger diagnostic surface, and a
domain's tests no longer demonstrate that it can operate behind foreign APIs alone.

### 3. Duplicate every scenario at every level

The same use case could be repeated as a module test, application integration test, UI test, and
browser test.

This provides broad coverage but creates high maintenance cost and redundant feedback. Test scope
should be selected by the risk being proven rather than by mechanically repeating every scenario.

### 4. Use layered test scopes aligned with the architecture

Local behavior can be proven in the smallest module runtime that contains it. Collaboration and
deployment behavior can be proven at the application assembly boundary. UI-specific interaction
can be tested with the Jmix Flow UI test runtime.

This option preserves both focused feedback and system-level confidence.

## Decision

Place each test at the **smallest architectural scope that can prove the behavior**.

| Test scope | Responsibility |
|---|---|
| Plain unit test | Framework-independent calculation, value behavior, and small decision logic |
| API artifact test | DTO and enum contracts, Jmix DTO metadata, validation, and public contract shape |
| Core module integration test | One domain's persistence, services, listeners, and calls to mocked foreign APIs |
| UI module integration test | One domain's views, actions, data binding, and controller interaction using Jmix `@UiTest` |
| Assembled `webapp` integration test | Real cross-domain flows, events, transactions, rollback, role composition, and starter wiring |
| Browser end-to-end test | Behavior that specifically depends on the real browser, client/server boundary, routing, or rendering |

The scopes are complementary. A broad test does not eliminate the need for focused domain tests,
and a module test does not claim to prove integration with the real provider.

## Module Tests

A Core module integration test loads the Jmix infrastructure and persistence configuration needed
by that domain. A foreign collaborator is represented by its public API and replaced with a mock or
test double when the collaborator's implementation is not the subject of the test.

For example, Quote Core injects a mocked `PolicyService`. Its tests verify that Quote:

- accepts or rejects only valid Quote states;
- constructs the correct public Policy request;
- stores the `PolicyDto` result in its local representation;
- preserves its state when the Policy API reports a failure.

The test proves Quote's behavior and its use of the Policy contract. It deliberately does not prove
Policy or Account behavior.

API tests stay with the API artifact and verify boundary types without importing Core entities.
UI module tests use `@UiTest` and `FlowuiTestAssistConfiguration` to exercise the module's own views
and controllers. Foreign services remain behind their API contracts unless the integration itself
is the behavior under test.

## Assembled Application Tests

Tests belong in `webapp/src/test` when their assertion requires multiple real domain
implementations or application composition.

The assembled suite covers representative architectural seams:

- Quote acceptance creates the real Policy, Account, and payment documents.
- Policy events are handled by the real Account listener.
- Account failure propagates and rolls back the complete policy-conclusion transaction.
- application personas compose the expected domain Core and UI roles;
- UI starters and provider-owned sections are registered in the final runtime.

Normal application integration tests extend `BaseIntegrationTest`, which supplies the complete
Spring Boot context, test profile, and authenticated administrator. Individual tests do not repeat
that configuration.

## UI Test Boundary

Jmix `@UiTest` tests are the default for controller behavior, view navigation, data containers,
actions, component state, and persistence triggered through a view. They provide a wider UI signal
than a plain controller unit test without paying the cost of a full browser session.

UI tests must assert meaningful behavior, such as persisted changes, grid contents, enabled state,
validation, or notifications. Merely opening a view without an exception is not sufficient.

Shared helpers navigate by view IDs and interact through component IDs and actions. Stable IDs are
part of the test interface. Translated text is used only when a component has no stable ID.

Browser tests are reserved for behavior the server-side UI test runtime cannot prove. They are not
required for every CRUD path.

## Feedback Strategy

Run the narrowest relevant test first and broaden verification as confidence increases. A change in
Quote Core should first run Quote's focused test task. A change to the Quote → Policy → Account seam
should run the corresponding `webapp` flow tests. Architecture changes additionally run the focused
architecture suite before the complete project check.

This feedback order is useful for both developers and coding agents: a failure arrives close to the
changed scope, while the assembled and complete suites still protect integration before delivery.

## Consequences

### Intended benefits

- Module tests demonstrate that bounded contexts collaborate through APIs rather than foreign
  implementations.
- Local failures have a smaller diagnostic surface and can be corrected quickly.
- Cross-domain invariants and transaction behavior are tested with real participants.
- UI behavior receives focused integration coverage without requiring a browser for every case.
- The test tree mirrors the production architecture and provides examples for future changes.
- Coding agents can select a predictable test location and focused validation command.

### Accepted costs and limitations

- The project maintains multiple Spring Boot and Jmix test configurations.
- Mocks at module boundaries can diverge from real providers; assembled tests are still required for
  important contracts.
- The `webapp` suite is slower and needs coordinated cross-domain cleanup and fixtures.
- A behavior may need assertions at more than one scope when local responsibility and integration
  semantics are independently important.
- Jmix UI tests do not reproduce every browser and client-side rendering behavior.
- Deciding the smallest sufficient scope requires judgment; line coverage alone cannot make that
  decision.

## Guardrails and Agent Guidance

- Put a test beside the production artifact when it proves behavior owned entirely by that
  artifact.
- Mock foreign implementations through their API interfaces in module tests.
- Put tests in `webapp` when they require real cross-domain collaboration, transaction propagation,
  role composition, or starter assembly.
- Use `@UiTest` with `FlowuiTestAssistConfiguration` for Jmix view and controller behavior.
- Prefer component IDs and action IDs over translated labels.
- Name scenarios `given_X_when_Y_then_Z` and separate `given`, `when`, and `then` phases.
- Reload persisted entities after invoking a service or UI action before asserting stored state.
- Do not add a test-managed `@Transactional` annotation to Jmix integration test classes.
- Do not write navigation-only UI tests that lack a behavioral assertion.
- Start with the smallest relevant Gradle test command and run broader checks for boundary or
  application-level changes.

Some older tests still contain manual cleanup or text-based component selection. They are migration
debt, not examples of an alternative preferred test style. New and changed tests follow the shared
testing guidance and migrate nearby code when practical.

## Evidence in the Repository

- [`QuoteTestConfiguration`](../../quote/quote-core/src/test/java/com/insurance/quote/core/QuoteTestConfiguration.java) loads Quote Core and replaces `PolicyService` with a mock.
- [`QuoteTest`](../../quote/quote-core/src/test/java/com/insurance/quote/core/QuoteTest.java) verifies Quote behavior and the request sent through the Policy API.
- [`BaseIntegrationTest`](../../webapp/src/test/java/com/insurance/app/test_support/BaseIntegrationTest.java) defines the common assembled application test context.
- [`QuoteAcceptanceFlowTest`](../../webapp/src/test/java/com/insurance/app/quote/QuoteAcceptanceFlowTest.java) verifies the real Quote → Policy → Account result.
- [`PolicyRollbackTest`](../../webapp/src/test/java/com/insurance/app/policy/PolicyRollbackTest.java) verifies cross-module rollback behavior.
- [`AccountUiTest`](../../account/account-ui/src/test/java/com/insurance/account/ui/AccountUiTest.java) exercises Account views with Jmix `@UiTest`.
- [`ViewInteractions`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/ViewInteractions.java), [`DataGridInteractions`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/DataGridInteractions.java), and [`FormInteractions`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/FormInteractions.java) provide shared UI interaction vocabulary.
- [`insurance-testing`](../../.skills/insurance-testing/SKILL.md) records the current test placement, shape, and validation guidance for coding agents.
- [`article-draft.md`](../article-draft.md#732-behavior-tests-fixtures-and-coverage) contrasts module tests with assembled application flow tests.

## Follow-up Decision

ADR-0009 defines the shared test-data, cleanup, authentication, and assertion strategy used across
these test scopes.
