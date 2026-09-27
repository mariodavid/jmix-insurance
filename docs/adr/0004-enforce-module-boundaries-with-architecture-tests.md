# ADR-0004: Enforce Module Boundaries with Architecture Tests

- **Status:** Accepted
- **Decision date:** 2026-06-01 (earliest repository evidence)
- **Recorded:** 2026-08-08
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0001: Use a Modular Monolith Built from Jmix Add-ons](0001-use-a-modular-monolith-built-from-jmix-add-ons.md), [ADR-0002: Apply Asymmetric API/Core/UI Layering](0002-apply-asymmetric-api-core-ui-layering.md)

## Context

ADR-0001 defines the application as a deployment monolith composed from domain-owned Gradle builds. ADR-0002 defines the asymmetric API/Core/UI dependency rules inside and between those domains. The Gradle classpaths make these decisions effective during normal development: a module cannot import a type that is not available through a declared dependency.

Gradle alone does not preserve the intended architecture. When compilation fails because a foreign implementation type is unavailable, a developer or coding agent can add the missing Core dependency to the Gradle file. The code then compiles, but the change has replaced part of the intended architecture with a new dependency direction. The build describes what is technically available after the edit; it does not independently decide whether that availability is allowed.

Written decisions and code review explain the intended model, but violations are easy to miss as the number of domains, artifacts, and contributors grows. The project therefore needs an executable definition of its module boundaries that remains independent of individual Gradle dependency changes.

Without that second mechanism, the architecture documented in ADRs can drift away from the implementation while every module continues to compile.

## Decision Drivers

- Detect drift between the intended modular architecture and implemented Java dependencies.
- Prevent a compilation problem from being "fixed" by adding a forbidden Gradle dependency.
- Express the domain and layer rules from ADR-0001 and ADR-0002 as executable constraints.
- Check all domain classes together from the assembled application, where cross-build dependencies are visible.
- Return deterministic and focused feedback during local development and CI.
- Give coding agents a failure they can inspect and correct while the implementation context is still active.
- Make deliberate architecture changes modify the decision, dependency declarations, and executable rules together.

## Considered Enforcement Levels

### 1. Rely on Gradle dependencies

Separate Gradle artifacts provide useful compile-time isolation. As long as their dependencies remain correct, illegal implementation types are unavailable.

This mechanism cannot distinguish an intentional dependency from one added as a shortcut. Once `policy-core` is added to another domain's classpath, the compiler accepts imports from Policy's implementation and persistence model. Gradle then enforces the changed architecture rather than the intended one.

### 2. Rely on ADRs, conventions, and code review

Written decisions can explain domain ownership and the reasons behind API/Core/UI layering. Reviewers can compare each change with those decisions.

This approach preserves rationale but provides late and inconsistent enforcement. Reviewers must reconstruct dependency direction for every relevant change, and a coding agent does not receive a deterministic signal while it is implementing the task.

### 3. Add executable architecture tests

Architecture tests can import the assembled production classes and check their dependencies against rules defined independently of the Gradle files being changed.

This duplicates part of the module model deliberately. Gradle determines which types are available; the tests determine which dependencies are allowed. Adding a dependency may make compilation succeed, but using it across a forbidden boundary still makes the architecture suite fail.

## Decision

Use **ArchUnit-based architecture tests as a second enforcement layer for the modular structure and API/Core/UI dependency rules**.

The assembled `webapp` provides the central test entry point because its test runtime can see the production classes from all included domains. Reusable rules live in `test-support`; `ArchitectureTest` imports the complete `com.insurance` production namespace and executes those rules as part of the normal JUnit test suite.

The architecture tests covered by this ADR enforce:

- API packages do not depend on Core, UI, or Flow UI implementation packages.
- Core packages may use API contracts but do not depend on another domain's Core implementation.
- Core services and artifacts remain independent of Flow UI.
- UI packages may use their own Core model but do not use a foreign domain's Core implementation.
- Domain slices depend only on the explicitly allowed set of other business domains.
- Core artifacts do not restore UI coupling through a Gradle dependency or a view resource.

Package names are part of the executable architecture. Classes must reside in the API, Core, UI, and domain packages that let the rules determine ownership and layer membership.

Gradle and ArchUnit have complementary responsibilities:

| Mechanism | Responsibility |
|---|---|
| Gradle dependency declarations | Make only the intended artifacts available on a module's normal classpath |
| Architecture tests | Reject forbidden dependencies even if someone makes their types available through Gradle |
| ADRs | Explain why the dependency rules exist, which alternatives were rejected, and which costs are accepted |

## Architecture Change Process

An architecture test failure is not automatically a request to weaken the rule. It may show that the implementation took a shortcut, or it may reveal that the architecture genuinely needs to change.

For a deliberate architecture change, the same change set must:

1. explain the new context and trade-off by updating or superseding the relevant ADR;
2. update the permitted Gradle dependencies;
3. update the architecture rule and its rationale;
4. add or adjust tests that demonstrate the intended dependency direction.

A local exception that is not intended to become a general rule must be narrow, named, and justified. Broad package exclusions are not an acceptable way to make a feature compile.

## Consequences

### Intended benefits

- ADR-0001 and ADR-0002 become testable constraints rather than architectural aspirations.
- A foreign Core import remains a build failure after the developer adds the corresponding Gradle dependency.
- Architecture drift is detected in the same test workflow as behavioral regressions.
- The central suite presents one command for checking boundaries across the complete composite build.
- Rule names and failure messages give reviewers and coding agents a concrete explanation of the violated dependency direction.
- CI verifies the architecture independently of a developer's or agent's local choices.
- Intentional architectural evolution becomes visible because code and rules must change together.

### Accepted costs and limitations

- Part of the module model exists in both Gradle and architecture-test configuration and must be maintained consistently.
- Package naming becomes an enforceable contract; moving or introducing packages may require rule changes.
- The global suite depends on an assembled classpath and is broader than compiling a single domain artifact.
- Poorly designed rules can create false positives or encourage broad exceptions.
- ArchUnit can enforce only decisions translated into rules. It cannot determine whether a new business boundary is sensible.
- Java dependency tests do not see every dependency encoded in configuration files, strings, runtime bean discovery, or database artifacts.

## Feedback Loop for Coding Agents

The architecture suite is part of the agent's implementation feedback loop, not only a final CI gate.

After changing a module boundary or dependency, the agent runs:

```shell
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

A failing rule identifies the forbidden source and target packages and states the intended alternative, such as using a foreign `*-api` contract. The agent can then revise the dependency while the original task, changed files, and compiler errors are still in context.

This deterministic feedback complements the ADR. The test says that a dependency is forbidden; the ADR explains why it is forbidden and what trade-off the project accepted. Neither replaces the other.

## Out of Scope

This ADR covers the **coarse modular structure and Java dependency direction** from ADR-0001 and ADR-0002.

It does not decide how the project should verify Jmix-specific conventions and dependencies that may live outside ordinary Java type relationships, including:

- Jmix entity names referenced through JPQL, XML, or metadata strings;
- entity, DTO entity, and embeddable conventions;
- view-descriptor and message-key integrity;
- Liquibase alignment with entity metadata;
- security role, view, and menu consistency;
- Jmix-specific listener and internal-API safety rules.

Those checks require their own rationale, scope, and techniques and belong in a separate ADR. The current `ArchitectureTest` contains such additional rule groups, but they are not justified by this decision.

## Guardrails and Agent Guidance

- Do not add a dependency merely to make a foreign implementation import compile.
- When a cross-domain type is unavailable, look for or extend the owning domain's API contract.
- Run the focused architecture suite after changing Gradle dependencies, packages, or module interaction.
- Read the rule's rationale before changing the rule or adding an exception.
- Do not remove a rule, widen an allowlist, or exclude a package only to make the current task green.
- When the architecture itself changes, update the ADR, Gradle model, and executable rule in the same change.
- Keep module rules focused on dependency direction; do not mix unrelated code-style checks into this decision.

## Evidence in the Repository

- [`ArchitectureTest`](../../webapp/src/test/java/com/insurance/app/arch/ArchitectureTest.java) is the single entry point that imports all production classes and assembles the rule groups.
- [`ModuleLayerDependencyRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/layer/ModuleLayerDependencyRules.java) enforces the API/Core/UI dependency direction from ADR-0002.
- [`BusinessModuleDependencyRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/insurance/BusinessModuleDependencyRules.java) defines the permitted horizontal dependencies between business domains.
- [`CoreModuleIsolationRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/layer/CoreModuleIsolationRules.java) prevents Core artifacts from regaining Flow UI dependencies or view resources.
- [`webapp/build.gradle`](../../webapp/build.gradle) adds ArchUnit and the shared test-support libraries to the assembled test runtime.
- [`article-draft.md`](../article-draft.md#733-architecture-tests-with-archunit) describes the executable architecture as part of the feedback stack.

The first dedicated `ArchitectureTest` appears in commit `c1e8285` on 2026-06-01. Commit `61d1836` moved reusable architecture rules into `test-support`, leaving the assembled application as their central execution point.

## Follow-up Decision

A separate ADR should decide which Jmix-specific conventions deserve deterministic checks, why ordinary ArchUnit type analysis is insufficient for them, and when file or metadata inspection is justified.
