---
name: insurance-create-cross-module-reference
description: Create or change a consumer-owned embedded reference from one Insurance core module to an entity owned by another Insurance module. Use when an Insurance domain entity needs a fachliche reference to a foreign module concept without a direct JPA association, shared reference class, or live cross-domain lookup, including entity/embeddable mapping, events/API snapshot data, Liquibase, UI bindings, tests, and ArchUnit guardrails.
---

# Insurance Create Cross Module Reference

Use this skill when an Insurance module needs to remember or filter by a concept owned by another module, for example `account-core -> policy`, `policy-core -> partner`, or `quote-core -> partner`.

## Core Rule

Model the reference as a consumer-owned embedded value object in the consuming module.

Do:

- Put the reference class in `com.insurance.<consumer>.core.entity`.
- Annotate it with `@JmixEntity` and `@Embeddable`.
- Embed it in the owning entity with `@EmbeddedParameters(nullAllowed = false)`, `@Embedded`, and `@NotNull` when required.
- Store only fields the consuming module owns semantically: identity, display, filter, decision, or audit snapshot data.
- Let the consuming module name the class from its perspective, for example `AccountPolicyReference`, not shared `PolicyReference`.

Do not:

- Add `@ManyToOne`/`@OneToOne` to a foreign module entity.
- Persist a DTO, entity, or reference class from another module.
- Create shared reference classes in `*-api` or `common` for multiple core modules to persist.
- Import foreign `core.entity`, foreign DTOs, or foreign services inside persistent entities or embeddables.
- Use Liquibase SQL that joins another module's table from a module-local changelog.

Foreign API enums are allowed when they are stable value/catalog contracts already accepted by the architecture rules.

## Field Selection

For every candidate field, write down why the consuming module needs it:

- `Identity`: stable technical id or business key.
- `Display`: local UI should show it without a live API call.
- `Filter`: GenericFilter/list queries should search it locally.
- `Decision`: local domain logic needs it.
- `Audit`: value captures a historical snapshot.

Leave it out when it is only "maybe later", derivable, or live/current foreign master data. Current names such as `firstName`/`lastName` usually belong in a read/search model, not in the write-model reference, unless the business explicitly wants a historical snapshot.

## Implementation Workflow

1. Inspect the existing owner entity, service flow, event/API contracts, XML views, message bundles, tests, and Liquibase changelog.
2. Create the embedded reference class in the consuming core module:

```java
@JmixEntity
@Embeddable
public class AccountPolicyReference {

  @Column(name = "POLICY_ID", nullable = false)
  @NotNull
  private UUID policyId;

  @Column(name = "POLICY_NO", nullable = false)
  @NotNull
  private String policyNo;

  @Column(name = "PARTNER_NO")
  private String partnerNo;

  @InstanceName
  @DependsOnProperties("policyNo")
  public String instanceName() {
    return policyNo;
  }
}
```

3. Embed it in the consuming entity:

```java
@EmbeddedParameters(nullAllowed = false)
@Embedded
@NotNull
private AccountPolicyReference policy;
```

4. Add consumer-owned fields outside the reference for local decisions. Example: `accountingPeriodStart` and `accountingPeriodEnd` belong to Account, not to the Policy reference.
5. Extend the source module's API event or command with the minimal snapshot values the consumer needs. Do not pass foreign entities.
6. Populate the reference in the consuming service/listener from the event/command. Do not call a foreign service from UI rendering just to display the reference.
7. Update Flow UI XML to use embedded paths such as `policy.policyNo` and `policy.partnerNo`. Use typed components, for example `datePicker` for `LocalDate`.
8. Add message keys for the owning entity attribute and the embeddable's fields.
9. Add a module-local Liquibase migration for the new columns. Keep it reachable from the module changelog. For old data, backfill only from local columns or safe constants unless an app-level migration explicitly owns a cross-module join.
10. Update tests and fixtures to create the reference explicitly or through the service/event path.
11. Run the smallest relevant checks:

```shell
./gradlew spotlessApply
./gradlew :<consumer>:<consumer>-core:test
./gradlew :<consumer>:<consumer>-ui:test
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

## Guardrail Check

Before finishing, verify the architecture rules cover the new shape:

- Persistent entities and embeddables reside below `com.insurance.<module>.core.entity`.
- Persistent entities and embeddables do not depend on foreign `com.insurance.<module>.core.entity` packages.
- Foreign API enums stay allowed because they are outside `core.entity` packages.
- A temporary foreign entity field, for example `com.insurance.policy.core.entity.Policy`, in a consuming embeddable makes `ArchitectureTest` fail, then remove the probe and rerun it green.

If these rules are missing, update `PersistentEntityDependencyRules`, `PersistentEntityConventionRules`, and their tests before relying on the new pattern.
