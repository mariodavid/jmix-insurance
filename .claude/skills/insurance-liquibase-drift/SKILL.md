---
name: insurance-liquibase-drift
description: Keep this app's Jmix entity metadata and Liquibase changelogs in sync, especially required unique business keys and schema constraints.
---

# Insurance Liquibase Drift

Use this skill when changing persistent Jmix entities, business keys, required fields, precision,
scale, lengths, indexes, or module changelogs.

## Workflow

1. Inspect the entity annotations and the owning module changelog together.
2. For every `@Column(nullable = false)`, make the column non-null in Liquibase.
3. For every `@Column(unique = true, nullable = false)`, add either:
   - inline `<constraints unique="true" .../>`, or
   - explicit `<addUniqueConstraint .../>`.
4. Add new changelog files under the owning core module and include them from that module's
   `liquibase/changelog.xml`.
5. Do not put DTO entities in Liquibase.

## Existing Business Keys

- `partner_Partner.partnerNo` -> `PARTNER_PARTNER.PARTNER_NO`
- `quote_Quote.quoteNo` -> `QUOTE_QUOTE.QUOTE_NO`
- `policy_Policy.policyNo` -> `POLICY_POLICY.POLICY_NO`

## Validation

```shell
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
./gradlew :<module>:<module>-core:compileJava
```

## Forbidden

- Entity unique business key without Liquibase unique constraint.
- New changelog file not included by the module changelog.
- Raw table joins in another module to compensate for missing API/read models.
- Schema changes only in tests.
