# ADR-0010: Enforce Jmix Metadata and Resource Conventions in Architecture Tests

- **Status:** Accepted
- **Decision period:** 2026-06-07–2026-06-09
- **Recorded:** 2026-07-03
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0004: Enforce Module Boundaries with Architecture Tests](0004-enforce-module-boundaries-with-architecture-tests.md), [ADR-0005: Store Cross-Domain References as Local Domain Representations](0005-store-cross-domain-references-as-local-domain-representations.md), [ADR-0007: Allow Provider-Owned UI Contributions Through Host-Defined SPIs](0007-allow-provider-owned-ui-contributions-through-host-spis.md)

## Context

ADR-0004 introduces architecture tests for Java package, layer, and module dependencies. Those
rules operate primarily on compiled classes. Jmix applications also connect important artifacts
through strings, annotations, XML descriptors, and database changelogs that do not create ordinary
Java dependencies.

A module can mention `policy_Policy` in a JPQL string or XML data loader without importing the
Policy class. A controller can reference a nonexistent component ID in `@Subscribe`. A security
role can grant a view ID that no longer exists. An entity can declare a required unique business
key while Liquibase lacks the corresponding database constraint. All of those changes can compile
successfully.

Jmix Studio and IntelliJ inspections detect some framework-specific inconsistencies during local
development. That feedback is not reliably available when a coding agent works autonomously in a
headless environment or when CI checks a clean repository without a running IDE. The project-local
Jmix skills provide agents with implementation guidance, but written guidance remains advisory. An
agent can misunderstand or omit a convention and still produce plausible code.

The project therefore needs a deterministic, headless feedback layer for selected Jmix conventions
that are important enough to fail a build.

## Decision Drivers

- Detect Jmix dependencies and identifier mismatches that Java compilation cannot see.
- Give autonomous coding agents actionable feedback without depending on IntelliJ or Jmix Studio.
- Turn stable conventions from Jmix skills into enforceable post-implementation checks.
- Run the same checks locally and in CI through a normal Gradle test command.
- Keep one architecture-test entry point instead of a collection of unrelated scripts.
- Make violations explain the intended repair, especially at domain boundaries.
- Separate reusable Jmix rules from Insurance-specific architecture rules.
- Check Java, XML, Liquibase, Gradle, and security metadata as one application composition.

## Considered Options

### 1. Rely on documentation, skills, and code review

`AGENTS.md`, Jmix skills, ADRs, and reviewers can describe the required conventions and catch
mistakes.

This guidance is necessary for explaining intent and choosing an implementation. It cannot provide
a deterministic failure when an agent overlooks a rule. Review then becomes the first hard
feedback point for errors that can be checked mechanically.

### 2. Rely on Jmix Studio and IntelliJ inspections

Developers can use the IDE's Jmix-aware inspections to find invalid descriptor references and other
framework problems.

This gives fast, rich local feedback. It is not guaranteed in headless agent environments or CI,
and its availability depends on an installed and running IDE. An architecture decision should not
be protected only on selected workstations.

### 3. Add separate scripts and lint tasks for every resource type

Custom commands could scan JPQL, XML, Liquibase, roles, and descriptors independently.

This can use a specialized implementation for each format. It creates several entry points,
reporting styles, and execution requirements. Developers and agents must know which script applies
to a change, and CI must assemble the results separately.

### 4. Extend the central architecture test suite

The existing JUnit and ArchUnit entry point can combine bytecode rules with custom conditions over
source and resource files. The complete `webapp` test runtime can see all assembled modules and
their metadata.

This option provides one command and one failure-reporting model while allowing checks beyond
ArchUnit's normal bytecode domain.

## Decision

Extend the central `ArchitectureTest` with **deterministic Jmix metadata and resource convention
rules**.

ArchUnit and JUnit provide the execution and reporting framework. Rules use ArchUnit's imported
class model where the relevant information exists in bytecode. When a Jmix dependency exists only
in a Java string, XML descriptor, Liquibase changelog, Gradle file, or security annotation value,
the rule may use a focused source or resource scanner and expose its findings as an `ArchRule`.

The architecture suite is intentionally broader than a pure ArchUnit bytecode suite. The important
property is that every selected convention produces the same local and CI feedback through:

```shell
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

## Relationship to Skills and IDE Feedback

Jmix skills remain the prescriptive layer. They explain how to implement a complete entity, view,
role, changelog, test, or cross-module reference and why the surrounding artifacts are required.
Architecture tests provide the corrective layer after a change: they reject selected deviations
with a concrete message.

Jmix Studio and IntelliJ inspections remain valuable for interactive development and may detect
more situations than the repository suite. The build rules do not attempt to reproduce the entire
IDE. They provide a portable minimum set of high-value checks for developers, headless coding
agents, and CI.

Guidance and enforcement therefore complement one another:

- skills reduce the probability of producing an invalid change;
- compiler and architecture tests provide deterministic feedback when it still happens;
- IDE inspections add richer local feedback when the IDE is available;
- review remains responsible for semantics and justified exceptions.

## Enforced Jmix Rule Groups

### Persistent entities

`PersistentEntityConventionRules` requires persistent entities and embeddables to follow the
package and naming conventions used to derive domain ownership. Persistent Jmix entities are
created through Jmix factories rather than constructors and remain free of Lombok-generated entity
semantics.

The group checks that:

- Jmix entities are instantiated through `Metadata`, `DataManager`, or `DataContext` rather than a
  constructor;
- persistent entities do not use Lombok annotations;
- Jmix entity names use the owning module prefix, such as `policy_Policy`;
- persistent entities and embeddables reside in the owning Core entity package.

### String-based persistent-model boundaries

`PersistentEntityNameBoundaryRules` scans production Java and XML for foreign Jmix entity names.
It rejects JPQL, XML loaders, and metadata lookups that bypass the API boundary without creating a
foreign Java import.

This rule directly protects the decision that each bounded context owns its persistent model. The
previous direct queries from Partner UI to `policy_Policy` and `account_Account` are representative
of the coupling it prevents.

### API DTO entities

`JmixDtoEntityConventionRules` keeps API DTO entities usable by Jmix consumers without turning them
into persistent entities. API DTO entities must:

- reside in the domain API DTO package;
- use a stable name in the form `<module>_api_<ClassName>`;
- declare exactly one `@JmixId`;
- place `@JmixGeneratedValue`, when present, on the identifier;
- contain no JPA entity, table, embeddable, or mapped-superclass annotations.

### Flow UI descriptors and packages

`JmixViewDescriptorIntegrityRules` verifies that a view or fragment controller references an
existing XML descriptor and that controller annotations refer to IDs present in that descriptor.
The scanner covers identifiers used by `@EditedEntityContainer`, `@LookupComponent`, `@Subscribe`,
and `@Install`.

`JmixUiPackageConventionRules` requires `@ViewController` and `@FragmentDescriptor` classes to live
in the module's UI view packages. This makes UI ownership and discovery predictable.

### Security policy consistency

`SecurityPolicyConsistencyRules` connects annotated roles to the assembled UI and menu resources.
It verifies that:

- view policies reference existing view controller IDs;
- menu policies reference existing menu items or view-backed menu entries;
- every user-facing view has a concrete view policy.

This prevents newly added views from becoming accidentally unreachable or relying only on broad
wildcard access and prevents stale role identifiers from compiling unnoticed.

### Entity and Liquibase alignment

`LiquibaseSchemaDriftRules` compares persistent entity metadata with module-owned changelogs. A
required column declared unique in the entity model must have a matching Liquibase unique
constraint, so a business key is enforced by the database rather than only described in Java.

The current rule intentionally covers a focused invariant. It is not a complete replacement for a
full schema diff.

### Jmix internal APIs

`JmixInternalApiRules` prevents production application code from depending on types or packages
marked with Jmix `@Internal` and on `io.jmix..impl..` packages, except for documented bootstrapping
locations. This reduces upgrade coupling to framework implementation details.

### Event listener safety

`JmixEventListenerSafetyRules` checks recurring Jmix listener failure modes:

- Core event listeners that use `DataManager` or services require an authenticated execution
  context;
- entity saving and loading listeners must not recursively call `DataManager.save()`;
- public API events must not expose Core entity classes.

These rules do not decide the business semantics of an event. They protect framework and boundary
conditions that can be checked statically.

## Scope and Ownership of Rules

Reusable, non-UI rules live in `test-support`. Rules with compile-time dependencies on Jmix Flow UI
annotations and descriptor behavior live in `test-support-ui`, allowing the generic support module
to remain independent of Flow UI.

Insurance-specific rules, such as the permitted horizontal dependency map, embedded local
references, and provider-owned UI section contracts, run through the same `ArchitectureTest` but
remain tied to their respective architectural decisions. Sharing an entry point does not make every
rule a generic Jmix convention.

The assembled `webapp` is the execution point because its test runtime sees all production classes,
roles, menus, descriptors, changelogs, and module resources. Scanners inspect production sources
and resources only; tests themselves do not become architecture input.

## Adding and Changing Rules

A new architecture rule must protect a documented decision or a recurring, high-value Jmix failure
mode. The suite is not a collection of arbitrary style preferences.

Every rule should:

- state the architectural or framework reason it protects;
- produce a message that identifies the offending artifact and expected boundary;
- have a scope narrow enough to avoid treating every unusual implementation as invalid;
- document necessary framework bootstrapping exceptions;
- be changed together with the ADR or convention when the architecture deliberately evolves.

A failing rule is not proof that the implementation must always change. It may reveal that the
decision itself has changed. In that case, the rule and its explanation are updated explicitly
rather than bypassed with an unrelated dependency or broad ignore pattern.

## Consequences

### Intended benefits

- Headless coding agents receive Jmix-aware feedback without a running IDE.
- CI checks the same selected conventions as local development.
- String-based JPQL and XML coupling can no longer silently bypass Java dependency rules.
- Cross-artifact inconsistencies in views, roles, and Liquibase fail close to the change.
- Skills, source examples, and executable checks reinforce one another.
- A single focused command covers both module architecture and selected Jmix conventions.
- Reusable Jmix rule groups can be applied independently of the Insurance-specific dependency map.

### Accepted costs and limitations

- Custom scanners and rule parts require maintenance as Jmix and project conventions evolve.
- Regex- or source-based inspection is less semantically complete than parsing every supported
  language and descriptor format.
- False positives are possible when a legitimate new pattern resembles a forbidden reference.
- Package names, entity-name prefixes, view IDs, and other identifiers become explicit contracts.
- The central architecture suite grows in runtime and diagnostic complexity.
- A green suite proves only the conventions that have been encoded; it does not replace Jmix
  Studio, semantic tests, code review, or architectural judgment.
- Some IDE inspections cannot be reproduced economically in a headless build.

## Guardrails and Agent Guidance

- Run the focused architecture suite after changing entities, DTOs, module boundaries, JPQL, XML
  views, fragments, roles, menus, listeners, or Liquibase.
- Treat foreign entity-name failures as boundary violations; extend the owning API or create a local
  representation instead of hiding the string.
- Fix descriptor IDs at their source rather than weakening descriptor-integrity rules.
- Update entity metadata and the owning Liquibase changelog together.
- Keep API DTO entities non-persistent and give them stable Jmix identity metadata.
- Use public Jmix APIs unless a narrow bootstrapping exception is documented.
- Do not add broad exclusions merely to make an agent-generated change pass.
- When a deliberate architecture change invalidates a rule, update the relevant ADR, rule, and
  examples together.

## Evidence in the Repository

- [`ArchitectureTest`](../../webapp/src/test/java/com/insurance/app/arch/ArchitectureTest.java) is the single entry point for all assembled architecture rule groups.
- [`PersistentEntityConventionRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/jmix/PersistentEntityConventionRules.java) enforces entity construction, Lombok, naming, and package conventions.
- [`PersistentEntityNameBoundaryRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/jmix/PersistentEntityNameBoundaryRules.java) scans Java and XML for foreign Jmix entity names.
- [`JmixDtoEntityConventionRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/jmix/JmixDtoEntityConventionRules.java) validates API DTO entity metadata.
- [`JmixViewDescriptorIntegrityRules`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/architecture/rules/jmix/JmixViewDescriptorIntegrityRules.java) connects controllers and fragments with their XML descriptors and IDs.
- [`JmixUiPackageConventionRules`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/architecture/rules/jmix/JmixUiPackageConventionRules.java) validates UI controller and fragment placement.
- [`SecurityPolicyConsistencyRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/jmix/SecurityPolicyConsistencyRules.java) checks role references against assembled views and menus.
- [`LiquibaseSchemaDriftRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/jmix/LiquibaseSchemaDriftRules.java) checks selected entity-to-schema invariants.
- [`JmixInternalApiRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/jmix/JmixInternalApiRules.java) blocks dependencies on internal framework implementation APIs.
- [`JmixEventListenerSafetyRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/jmix/JmixEventListenerSafetyRules.java) protects authentication, lifecycle listener, and API event boundaries.
- [`SourceBoundaryReferences`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/scan/SourceBoundaryReferences.java), [`SecurityPolicyIndex`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/scan/SecurityPolicyIndex.java), and [`JmixUiDescriptorScanner`](../../test-support-ui/src/main/java/com/insurance/common/test_support_ui/architecture/ui/JmixUiDescriptorScanner.java) provide file- and descriptor-based analysis where bytecode is insufficient.
- [`article-draft.md`](../article-draft.md#733-architecture-tests-with-archunit) describes the combined bytecode and file-based feedback layer.

## Follow-up Decisions

Separate ADRs may define:

- module ownership and assembly of Liquibase changelogs;
- composition of domain Core and UI security roles into application personas;
- the staged compiler, lint, test, coverage, and CI feedback pipeline.
