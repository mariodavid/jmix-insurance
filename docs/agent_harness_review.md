# Coding Agent Harness Review — jmix-insurance

> Historical snapshot: this review is kept as background material. The current architecture and
> agent-harness contract live in [docs/architecture.md](architecture.md), while the implementation
> backlog lives in [plans/codex-review.md](../plans/codex-review.md) and `tasks/`.

This document reviews the current architecture, modularization boundaries, and verification tools of the `jmix-insurance` project from the perspective of establishing a robust, safe, and highly efficient **Agent Development Harness**. 

A **Coding Agent Harness** is a structured combination of:
1. **Automated guardrails** (static analysis, modularity checks) to prevent regression and architectural violations.
2. **Rapid feedback loops** (targeted compilation and test execution) to minimize agent iteration time.
3. **Module-level documentation and specifications** to provide LLM-based agents with clear contextual maps of public boundaries and interfaces.
4. **Rich testing utilities** (fluent assertions, test data builders) to make adding/modifying tests reliable and descriptive.

---

## 1. Architectural & Modularization Overview

The `jmix-insurance` project is designed as a modular monolith built on **Jmix 2.8.1** (Spring Boot 3, Vaadin 24). It uses a **Gradle Composite Build** to manage independent domain components (add-ons) that are assembled into a single runnable application (`webapp`).

### The Domain Add-on Triad
Each functional domain (Partner, Quote, Policy, Account) is divided into three separate Gradle subprojects to enforce strict technical layering:
- **`*-api`**: Exposes public Java interfaces, Jmix DTOs, and Spring Application Events. It has no database persistence or Flow UI views.
- **`*-core`**: Contains the persistent JPA entities, database-backed service implementations, event listeners, and Liquibase migrations.
- **`*-ui`**: Contains the Jmix Flow UI view controllers, Vaadin views, and XML descriptors.

### Encapsulation via Value Types
To keep compilation loops circular-dependency-free and decouple JPA mappings across modules, inter-domain references are kept as **plain value types** (typically `String` or `UUID` values, like `partnerNo` in `Quote` or `policyId` in `Account`), rather than JPA `@ManyToOne` or `@OneToMany` relationships.

```
                  ┌───────────────────────┐
                  │        webapp         │
                  │  (Main Shell / Menu)  │
                  └───────────┬───────────┘
                              │ (aggregates UI starters)
            ┌─────────────────┼─────────────────┐
            ▼                 ▼                 ▼
     ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
     │ partner-ui  │   │  quote-ui   │   │  policy-ui  │
     └──────┬──────┘   └──────┬──────┘   └──────┬──────┘
            │                 │                 │
            ▼                 ▼                 ▼
     ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
     │partner-core │   │ quote-core  │   │ policy-core │
     └──────┬──────┘   └──────┬──────┘   └──────┬──────┘
            │                 │                 │
            │  ┌──────────────┴────────┐        │
            │  │ (depends only on API) │        │
            ▼  ▼                       ▼        ▼
     ┌─────────────┐             ┌─────────────┐
     │ partner-api │             │ policy-api  │
     └─────────────┘             └─────────────┘
```

---

## 2. Assessment of Existing Verification & Modularity Tools (The Harness)

The project currently has a strong baseline of verification utilities. Let's analyze how well they function as an agent harness:

### 2.1. Static Code Analysis (Checkstyle & SpotBugs)
- **Status**: **Configured** via a shared script `/gradle/static-analysis.gradle` and applied to all subprojects.
- **How it helps**: Checkstyle enforces standard spacing, bracket placements, and naming conventions. SpotBugs catches common runtime bugs (null pointer dereferences, resource leaks, infinite loops) at compile time.
- **Strengths**: High consistency. If an agent writes sub-standard Java code, the build fails immediately during `check` with a detailed report.
- **Gaps**: It does not catch Jmix-specific patterns (like Lombok annotations on JPA entities or instantiation of entities via `new`).

### 2.2. Architectural Guardrails (ArchUnit)
- **Status**: **Excellent configuration** in `webapp/src/test/java/com/insurance/app/arch/ArchitectureTest.java`.
- **How it helps**: A suite of junit-run architecture rules ensures that:
  - **API Layer Integrity**: API packages do not depend on core, ui, or Flow UI dependencies.
  - **Core Layer Separation**: Core modules do not depend on foreign core or UI implementations, nor do they declare Flow UI dependencies or include UI XML views.
  - **Jmix Guardrails**: Jmix persistent entities are never instantiated via constructor (which skips Jmix lifecycle/injection hooks; they must use `DataManager.create()`) and never use Lombok.
  - **UI Isolation**: UI modules only use their own core implementation, not foreign cores.
  - **Product Isolation**: Product classes do not depend on Partner classes.
- **Strengths**: This is a state-of-the-art mechanism for keeping code modular and clean. It blocks any code modifications that violate Jmix modular architecture.
- **Gaps**: These tests live solely in the `webapp` test suite. If an agent works in an isolated module (e.g. `partner`) and only runs tests inside `partner`, the ArchUnit tests are not executed.

### 2.3. Testing Utilities (`test-support` module)
- **Status**: **Fully operational**.
- **How it helps**: Provides a centralized test fixture set:
  - `EntityTestData` with pre-registered data builders (`*DataProvider`) for each entity type, ensuring valid required fields and database-level validation before persistence.
  - `AuthenticatedAsAdmin` JUnit extension to bypass security restrictions during integration tests.
  - `Assertions` with fluent domain-specific AssertJ matchers (e.g. checking generated policy numbers or accounting balances).
- **Strengths**: Extremely helpful for the coding agent when writing unit/integration tests, shielding the agent from the complexity of raw Jmix JPA bootstrapping.

---

## 3. Gaps & Gaps in the Coding Agent Harness

While the codebase is beautifully structured, the **Agent Harness** has a few key limitations that can cause coding agents to run slowly, fail unexpectedly, or make incorrect architectural assumptions:

### Gap 1: Slow Feedback Loop (Composite Build Execution Speed)
- **Problem**: Running a full verification via `./gradlew check` or `./gradlew test` at the root project triggers SpotBugs, Checkstyle, and Jacoco over all 12 modules. On standard developer environments, this full check takes **2-5 minutes**.
- **Impact on Agent**: Coding agents typically make small, iterative modifications. Waiting 5 minutes for compilation/analysis verification severely slows down agent operations and wastes tokens.
- **Required Fix**: Standardize and document targeted verification commands for coding agents. For example:
  - Check only the quote module: `./gradlew :quote:check` (much faster!).
  - Run only unit tests: `./gradlew test -x checkstyleMain -x spotbugsMain`

### Gap 2: ArchUnit Rules are Not Distributed
- **Problem**: ArchUnit tests reside in the `webapp` test source set. An agent running `./gradlew :partner:check` or `./gradlew :partner:test` will **not** execute the architecture validation checks.
- **Impact on Agent**: The agent might successfully compile and test its changes within the local module, but violate module boundaries (e.g., importing a Flow UI class in a Core package). The failure will only be detected when the root build is run, which is too late and causes backtracking.
- **Required Fix**: Make the ArchUnit test execution a global requirement of every subproject check, or copy/share the test class across modules via the `test-support` classpath, so that running any module test triggers it.

### Gap 3: Missing Module-Level API Map (README.md per module)
- **Problem**: There are no individual `README.md` files inside the domain module directories (`quote/`, `policy/`, `partner/`, etc.). 
- **Impact on Agent**: While Java classes have clean Javadoc, the agent has to search through packages to find what service interfaces, events, and DTOs a module provides. LLMs are much faster and more accurate when a single concise markdown file maps out:
  - What the module does.
  - The public services exposed in `-api`.
  - The events emitted by the module.
  - Typical integration code patterns.
- **Required Fix**: Create structured `README.md` files for each functional module mapping out its public boundaries.

### Gap 4: Absence of Automated Code Formatting (Spotless)
- **Problem**: While Checkstyle detects style violations, it is a *reporter*, not a *fixer*. It forces the build to fail, requiring the agent to manually fix indentation, imports, and spacing.
- **Impact on Agent**: The agent can get stuck in tedious "fix Checkstyle import order" loops.
- **Required Fix**: Introduce the Gradle **Spotless** plugin to automatically format code using standard configurations (e.g., Google Java Format) prior to compilation or during checks.

### Gap 5: Lack of Jmix-Specific Linting
- **Problem**: ArchUnit is wonderful, but only runs during test execution. A static linting tool that checks Jmix-specific patterns (like invalid XML components, missing message keys, or incorrect data binding types) at coding time is absent.
- **Required Fix**: Create an execution guidelines document (`AGENTS.md`) and pre-commit check scripts that verify Jmix message keys and XML schemas before concluding work.

---

## 4. Proposed Architecture of the Coding Agent Harness

To solve these gaps, we propose establishing a complete **Agent Harness System** consisting of:

```
                          ┌────────────────────────┐
                          │  CODING AGENT HARNESS  │
                          └───────────┬────────────┘
                                      │
       ┌──────────────────────────────┼──────────────────────────────┐
       ▼                              ▼                              ▼
┌──────────────┐              ┌──────────────┐              ┌──────────────┐
│  GUARDRAILS  │              │ FEEDBACK LOOP│              │ KNOWLEDGE MAP│
├──────────────┤              ├──────────────┤              ├──────────────┤
│ • ArchUnit   │              │ • Targeted   │              │ • Domain     │
│   modularity │              │   module     │              │   spec README│
│ • Spotless   │              │   commands   │              │   guides     │
│   formatting │              │ • Selective  │              │ • Enforced   │
│ • Checkstyle │              │   builds     │              │   naming     │
│   linting    │              │   (-x tasks) │              │   standards  │
└──────────────┘              └──────────────┘              └──────────────┘
```

### Proposed Verifications Matrix

| Command | Target | Scope | Execution Time | Use Case |
|---|---|---|---|---|
| `./gradlew spotlessApply` | All Java files | Format all source code | ~3-5 seconds | Before compiling |
| `./gradlew :<module>:<layer>:compileJava` | Specific module/layer | Fast compilation feedback | ~5-10 seconds | Incremental coding |
| `./gradlew :<module>:check` | Specific module | Full local verification (Checkstyle/Tests) | ~30-40 seconds | Module code review |
| `./gradlew check` | Entire project | Aggregated Checkstyle, SpotBugs, ArchUnit, & Tests | ~2-3 minutes | Final CI & validation |

---

## 5. Harness Execution & Action Plan

Here is the structured action plan to build the ultimate development harness for a Jmix coding agent.

### Phase 1: Establish Local Modularity Guardrails (ArchUnit Distribution)
1. Expose `ArchitectureTest.java` as a shared test fixture in the `test-support` module.
2. Enable every module's test suite to run the shared modularity checks during their local `./gradlew test` execution.

### Phase 2: Implement Automated Code Formatting (Spotless)
1. Add the Spotless Gradle plugin to the root build and subproject configurations.
2. Configure it to use the **Google Java Format** or standard Jmix styling patterns.
3. Hook `spotlessApply` into the compilation pipeline so the agent's code is automatically formatted before compiler checks.

### Phase 3: Domain Module Maps (Documentation)
1. Create a `README.md` inside each module's directory (`partner/`, `quote/`, `policy/`, `account/`, `product/`, `security/`) summarizing:
   - Functional description and tables owned.
   - Public API (`com.insurance.<module>.api.service`) with copy-pasteable usage examples.
   - Events emitted (`com.insurance.<module>.api.event`) and their transaction scope.
   - DTO models in the API.

### Phase 4: Fast Command Guidelines
1. Enhance the `AGENTS.md` and `CLAUDE.md` guidelines in the root folder with clear explanations of target-specific gradle tasks, helping the agent to quickly run micro-tests.
2. Provide a helper verification shell script (e.g. `verify-module.sh <module>`) that performs fast, targeted verification.

---

## 6. Review Summary Checklist

- [x] Analyze current composite build architecture.
- [x] Audit active modularity boundaries and value type reference style.
- [x] Inspect existing ArchUnit test suite (`ArchitectureTest.java`).
- [x] Evaluate SpotBugs and Checkstyle configurations.
- [x] Identify the core harness bottlenecks (slow compilation, lack of modular READMEs, no automatic code formatting).
- [x] Formulate the conceptual architecture of the Coding Agent Harness.
- [x] Outline a 4-phase concrete action plan to implement the harness.
