# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Modular insurance management application built with Jmix 2 (Spring Boot 3, Vaadin 24, EclipseLink JPA). Structured as a Gradle composite build of independently publishable domain add-ons assembled into a single runnable app.

**Stack:** Java 21, Jmix 2.8.1, HSQLDB (embedded), Liquibase, Gradle composite build.

## Build Commands

```shell
# Run the application (http://localhost:8080, admin/admin)
./gradlew :webapp:bootRun

# Auto-format code (always run before compile/check)
./gradlew spotlessApply

# Compile a single layer (~5s)
./gradlew :<module>:<layer>:compileJava    # e.g. :partner:partner-core:compileJava

# Test/check a single module (~30s)
./gradlew :<module>:check                  # e.g. :partner:check

# Architecture guardrails (module boundaries, naming, security policies)
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"

# Full project verification
./gradlew check

# Lint only (spotless + PMD + SpotBugs, no tests)
./gradlew lint

# Run a single test class
./gradlew :webapp:test --tests "com.insurance.app.quote.QuoteAcceptTest"
```

## Module Structure

Root `settings.gradle` uses `includeBuild` for each domain. Each domain add-on follows a six-artifact pattern:

| Artifact | Purpose |
|---|---|
| `<domain>-api` | Service interfaces, DTOs, events (no JPA entities) |
| `<domain>-api-starter` | Spring Boot auto-config for API |
| `<domain>-core` | JPA entities, service implementations, listeners |
| `<domain>-core-starter` | Spring Boot auto-config for core |
| `<domain>-ui` | Jmix Flow UI views (Java controllers + XML descriptors) |
| `<domain>-ui-starter` | Spring Boot auto-config for UI |

Domain modules: `partner`, `policy`, `quote`, `account`, `claim`, `product`, `security`.
Shared: `theme`, `ui-sections`, `test-support`, `test-support-ui`.
App assembly: `webapp`.

Each domain build has its own `settings.gradle` and applies `gradle/jmix-domain-conventions.gradle`. The `jmixDomainProjectId` in each domain's `build.gradle` controls entity name prefixes (e.g. `partner_Partner`).

## Architecture Rules

- **API/Core boundary**: Inter-domain communication goes through `*-api` interfaces + DTOs only. Core modules never import from another domain's core.
- **Cross-domain references**: Stored as string values (partner numbers, policy IDs), not JPA associations.
- **Business logic**: In services or Spring event listeners, never in view controllers.
- **Entity instantiation**: Use `DataManager.create()` / `Metadata.create()` / `DataContext.create()`, never constructors.
- **No Lombok on entities**.
- **Message keys**: All user-visible text must use `msg://` keys, never hardcoded strings.
- **Decentralized Liquibase**: Each module owns its changelogs; `webapp/liquibase/changelog.xml` assembles them in dependency order.
- **Cross-module UI**: Uses `ViewSection<C>` contract from `ui-sections`. Host views define typed section interfaces in `*-ui-api`; contributors implement them as Jmix fragments.

## Key Business Flows

**Quote acceptance** (main cross-domain flow):
`QuoteService.accept()` → `PolicyService.createPolicy()` → publishes `PolicyCreatedEvent` → `AccountService.createAccount()` (event-driven, keeps policy-core free of account dependency).

## Testing

- **Base class**: `BaseIntegrationTest` (authenticated as admin, Spring Boot context).
- **Cleanup**: `DatabaseCleanup` truncates all domain tables in `@BeforeEach`.
- **Fixtures**: `EntityTestData` generic factory + `*DataProvider` per module (in `testFixtures` source set).
- **Assertions**: `InsuranceAssertions` entry point with domain-specific fluent assertions.
- **UI tests**: `@UiTest` annotation with `FlowuiTestAssistConfiguration`.
- All integration tests run in `webapp/src/test`.

## Skill Routing

Use the most specific skill for the task:

- Entity: `jmix-create-entity`
- Enum: `jmix-create-enum`
- List view: `jmix-create-list-view`
- Detail view: `jmix-create-detail-view`
- Composition UI: `jmix-create-composition-detail-view`
- Service logic: `jmix-create-service`
- Dialog flow: `jmix-add-dialog-detail-flow`
- Entity events: `jmix-add-entity-event-listener`
- Liquibase: `jmix-create-liquibase-changelog`
- Roles: `jmix-create-resource-role`
- I18n: `jmix-add-i18n-keys`
- Fetch plans: `jmix-configure-fetch-plan`
- DTO entities: `jmix-create-dto-entity`
- Fragments: `jmix-create-fragment`
- Tests: `jmix-create-test` or `insurance-testing`
- Module boundaries: `insurance-module-boundaries`
- Security roles: `insurance-security-roles`

## Validation Checklist

Before reporting completion:
1. Run `./gradlew spotlessApply` then compile the affected layer.
2. Check for unresolved `msg://` keys, hardcoded labels, mismatched form component types.
3. Verify role policies reference real view/menu IDs.
4. Run architecture test if module boundaries were touched.
5. Run `./gradlew :<module>:check` for the affected module.
