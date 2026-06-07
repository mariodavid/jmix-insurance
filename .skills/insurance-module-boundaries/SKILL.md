---
name: insurance-module-boundaries
description: Enforce this app's domain-module boundaries, especially API-only cross-domain access, Jmix entity-name string leaks in Java/XML, and webapp-only composition.
---

# Insurance Module Boundaries

Use this skill when moving data or UI behavior across `account`, `partner`, `policy`, `quote`,
`product`, or `security`.

## Workflow

1. Identify the owning domain for every persistent entity name.
2. Keep cross-domain access on API contracts:
   - Other modules may use `*-api` services, DTOs, and events.
   - Other modules must not use foreign `*-core` entities, repositories, services, JPQL entity
     names, or UI controllers.
3. Put foreign persistent queries in the owning core module and expose only DTO/read-model results.
4. Keep rich cross-domain UI composition in `webapp` or behind an explicit contribution SPI.
5. Run the architecture guardrail before finishing:

```shell
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

## Existing Examples

- `policy-api` exposes policy DTOs and policy services.
- `account-api` exposes account DTOs and account services.
- `partner-ui` displays policy/account overview data through API read services, not direct JPQL.

## Forbidden

- `policy_Policy`, `account_Account`, or other foreign persistent entity names in a domain module.
- `DataManager.loadValues()` joins across domain entity names outside the owning core module.
- `metadata.getClass("foreign_Entity")` in a domain UI module.
- Domain UI modules embedding foreign domain UI controllers directly.
