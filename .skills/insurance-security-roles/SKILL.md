---
name: insurance-security-roles
description: Update this app's modular Jmix security roles, role compositions, view policies, menu policies, and role guardrail tests.
---

# Insurance Security Roles

Use this skill when adding or changing access for the insurance app.

## Workflow

1. Identify the surface that changes:
   - Core entity CRUD: `*-core/src/main/java/.../security/*Core*Role.java`
   - Flow UI views/menus: `*-ui/src/main/java/.../security/*Ui*Role.java`
   - App personas: `webapp/src/main/java/com/insurance/app/security/*Role.java`
2. Grant the narrowest domain role:
   - Manage roles use `EntityPolicyAction.ALL` and `EntityAttributePolicyAction.MODIFY`.
   - Read roles use `EntityPolicyAction.READ` and `EntityAttributePolicyAction.VIEW`.
   - Create-only workflows still need `MODIFY` attributes, but not update/delete entity actions.
3. For UI roles, list every real view id in `@ViewPolicy`, including detail/dialog views.
4. For menu policies, use concrete `menu.xml` item ids or view ids only. Do not grant parent menu groups.
5. Update app personas explicitly:
   - `InsuranceAgentRole` manages Partner and Quote, reads Policy and Account.
   - `InsuranceBackOfficeRole` manages Partner, Quote, Policy and Account.
   - Both include `UiMinimalRole`.
6. Add or update `RoleCompositionTest` when persona composition changes.
7. Run the role and architecture guardrails:

```shell
./gradlew :webapp:test --tests "com.insurance.app.security.*"
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

## Guardrails

- `JmixSecurityRoleRules` rejects `@ViewPolicy` and `@MenuPolicy` values that do not exist.
- `JmixSecurityRoleRules` rejects user-facing `@ViewController` ids with no concrete `@ViewPolicy`.
- `RoleCompositionTest` keeps app persona roles aligned with the intended business access matrix.

## Forbidden

- Broad `ALL` entity access in read-only roles.
- Menu policies for parent menu groups when a concrete view item is opened.
- Changing a view id without updating role policies and starter registration tests.
- Updating a domain role without considering `InsuranceAgentRole` and `InsuranceBackOfficeRole`.
