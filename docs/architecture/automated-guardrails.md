# Automated Guardrails

This document describes the deterministic feedback layers that validate the current architecture
and Jmix conventions locally and in CI.

## Feedback Layers

The project uses progressively broader checks:

| Layer | Detects | Typical command |
|---|---|---|
| Formatter | Mechanical Java formatting and unused imports | `./gradlew spotlessApply` |
| Compiler | Missing types, invalid signatures, declared classpath | `./gradlew :quote:quote-core:compileJava` |
| Architecture suite | Forbidden dependencies and cross-artifact Jmix inconsistencies | focused `ArchitectureTest` |
| Focused behavior tests | Incorrect state, output, side effects, rollback, UI behavior | one test class or module `check` |
| Static analysis | PMD and SpotBugs findings | `./gradlew lint` |
| Full verification | Tests and quality checks across every included build | `./gradlew check` |
| CI coverage | Aggregate line and branch thresholds | JaCoCo root verification |
| Focused review | Semantic and task-specific risks not encoded as rules | pull-request review jobs |

Compilation validates the dependency graph after a change. The architecture suite independently
validates whether that graph is allowed. A developer or agent cannot legitimize a foreign Core
import merely by adding the corresponding Gradle dependency.

## Central Architecture Suite

`webapp/src/test/java/com/insurance/app/arch/ArchitectureTest.java` is the single entry point. The
assembled webapp test runtime can see production classes and resources from all installed modules.
Reusable non-UI rules and scanners live in `test-support`; Flow UI-specific rules and descriptor
scanners live in `test-support-ui`.

Run it with:

```shell
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

The suite combines ArchUnit bytecode analysis with source, XML, Gradle, Liquibase, menu, and view
descriptor scans. The latter are necessary for Jmix relationships expressed through strings and
resource identifiers rather than Java imports.

## Enforced Rule Groups

### Module and Layer Boundaries

- API, Core, and UI packages follow the allowed vertical dependency direction.
- API code contains no Core, UI, or Flow UI dependencies.
- Core modules do not depend on other domains' Core implementations.
- Core code and Core resources contain no Flow UI artifacts.
- UI modules do not import another domain's Core or UI implementation.
- Horizontal dependencies for Product, Partner, Security, Account, Policy, and Quote stay within
  their declared domain graph. Claim currently has only the generic layer/package checks; it does
  not yet have a dedicated horizontal rule in `BusinessModuleDependencyRules`.
- Domain builds apply the shared Gradle convention and required project IDs.
- production packages correspond to the owning artifact and layer.
- test-support libraries do not acquire production-domain implementation dependencies.

Primary groups: `ModuleLayerDependencyRules`, `CoreModuleIsolationRules`,
`BusinessModuleDependencyRules`, `DomainBuildConventionRules`, `ModulePackageConventionRules`, and
`TestSupportBoundaryRules`.

### Persistent Models and Cross-Domain References

- Persistent entities do not depend on foreign persistent entities.
- Cross-domain Jmix entity names are rejected in Java source, JPQL strings, XML loaders, and
  metadata lookups.
- Local reference types remain Jmix `@Embeddable` objects in the consuming Core module.
- Local embedded references have an instance name and no foreign persistence dependency.
- Persistent entity names use the owning domain prefix.
- Persistent entities stay in the expected package, do not use Lombok, and are not created with
  constructors in production code.

Primary groups: `PersistentEntityDependencyRules`, `PersistentEntityNameBoundaryRules`,
`EmbeddedReferenceConventionRules`, and `PersistentEntityConventionRules`.

### Jmix API and Event Safety

- API DTO entities are non-persistent, use the API prefix, and define exactly one valid Jmix ID.
- Production code does not use Jmix classes annotated `@Internal`, internal packages, or
  implementation packages outside explicit bootstrap exceptions.
- Core event listeners that use persistence or services execute with an authenticated context.
- entity saving/loading listeners do not call `DataManager.save()` recursively.
- API events do not carry Core entity types.

Primary groups: `JmixDtoEntityConventionRules`, `JmixInternalApiRules`, and
`JmixEventListenerSafetyRules`.

### UI and Contribution Integrity

- View and fragment controllers reside in their expected UI packages.
- `@ViewDescriptor` files exist.
- `@Subscribe`, `@ViewComponent`, and related annotation targets resolve to descriptor components.
- Cross-module UI imports use provider APIs or typed UI APIs, not provider implementations.
- `ViewSection` contracts, contexts, provider beans, ordering, message keys, and fragment creation
  follow the contribution convention.

Primary groups: `JmixUiPackageConventionRules`, `JmixViewDescriptorIntegrityRules`,
`UiCompositionBoundaryRules`, and `UiSectionContributionRules`.

### Schema and Security Consistency

- selected required/unique entity metadata has a matching Liquibase constraint.
- Core roles contain entity/attribute policies only.
- UI roles contain view/menu policies only.
- role codes use domain/layer prefixes.
- wildcard policies are restricted to the explicit full-access role.
- referenced view and menu IDs exist.
- every user-facing view has a concrete view policy.

Primary groups: `LiquibaseSchemaDriftRules`, `SecurityRoleLayerRules`, and
`SecurityPolicyConsistencyRules`.

### General Code Rules

`GeneralCodingRules` contains project-wide class-level checks that do not belong to one Jmix or
module category. They execute through the same central suite so structural failures have one entry
point and report format.

## When to Add a Rule

Add an executable rule when all of these are true:

- the convention is stable and applies repeatedly;
- a violation can be detected deterministically from classes or repository resources;
- the failure can explain both the invalid artifact and the expected repair;
- false positives can be kept low enough that developers do not routinely suppress the rule.

Keep semantic judgment in behavior tests or review. Architecture tests do not decide whether a new
bounded context is sensible, whether a premium is correct, or whether an exception to the current
architecture should be accepted.

## Handling a Failure

An architecture-test failure is evidence of a mismatch, not an instruction to weaken the test.

1. Read the complete violation and identify the owning module and artifact.
2. Compare the implementation with the relevant current-state document.
3. Move or remap the implementation when it accidentally crossed a boundary.
4. If the architecture itself intentionally changed, update source dependencies, current-state
   documentation, the corresponding ADR trail, and the executable rule together.
5. Re-run the focused suite before broad verification.

Do not solve a missing foreign type by adding a Core dependency, solve a missing view policy by
removing the view from scanning, or solve a descriptor mismatch by weakening the scanner unless
the architecture has actually changed.

## Local Validation Sequence

For a normal implementation task:

```shell
./gradlew spotlessApply
./gradlew :<domain>:<artifact>:compileJava
./gradlew :<domain>:check
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

Use `./gradlew check` before handoff when the change crosses domains or affects shared
infrastructure. For documentation-only changes, validate Markdown links and factual source
references; Java compilation is not required unless the documentation work reveals or changes
code.

## CI Pipeline

`.github/workflows/ci.yml` executes three stages:

1. **Compile & Static Analysis** runs `compileAll` and `lint` on JDK 21.
2. **Tests & Coverage** runs all composite-build tests, aggregates JaCoCo, and verifies 75% line and
   60% branch coverage.
3. **Focused review** runs only for successful pull requests and separates domain/boundary,
   correctness/testing/observability, and security/role/i18n perspectives.

The deterministic stages precede the AI-assisted review. Review prompts exclude issues already
reported by ArchitectureTest, PMD, SpotBugs, or Spotless so the final stage can focus on semantics
and context-dependent risks.

## Coding-Agent Feedback

Coding-agent guidance and executable checks have distinct roles:

- `AGENTS.md` provides repository routing and high-level constraints.
- architecture documents describe the current system.
- ADRs preserve decision rationale and accepted consequences.
- Jmix and `insurance-*` skills describe how to perform recurring changes.
- the compiler, tests, architecture suite, static analysis, and CI independently check the result.

An agent should use the documentation and skill before implementing, then use deterministic checks
as external feedback rather than relying on self-review.

## Primary Source Files

- [`ArchitectureTest`](../../webapp/src/test/java/com/insurance/app/arch/ArchitectureTest.java)
- [`test-support` architecture rules](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules)
- [`test-support-ui` architecture rules](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/architecture)
- [`static-analysis.gradle`](../../gradle/static-analysis.gradle)
- [Root build](../../build.gradle)
- [CI workflow](../../.github/workflows/ci.yml)
