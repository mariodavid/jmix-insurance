# Persistence and Security

This document describes the current physical database composition, schema ownership, Jmix entity
naming, and security-role composition.

## Database Runtime

The assembled application uses one file-based HSQLDB database:

```properties
main.datasource.url=jdbc:hsqldb:file:.jmix/hsqldb/app
main.liquibase.change-log=com/insurance/app/liquibase/changelog.xml
```

All installed domains share this database and the application's transaction manager. Module
ownership is expressed through entity packages, table naming, local references, and Liquibase
changelog location; it is not physical database isolation.

## Schema Ownership

Each Core artifact owns the master changelog and incremental change sets for its persistent model.
The application master changelog composes the installed modules:

```text
webapp/src/main/resources/com/insurance/app/liquibase/changelog.xml
├── Jmix Data
├── Jmix Flow UI Data
├── Jmix Security Data
├── Security Core
├── Partner Core
├── Quote Core
├── Policy Core
├── Account Core
├── Claim Core
└── application-owned change sets
```

Claim is included but its current master changelog is empty. Product has no persistent model and no
production changelog.

A module change set manages only database objects owned by that module. The webapp master defines
the installation and ordering; it does not contain domain table evolution.

## Current Tables

| Table | Owner | Model and relationships |
|---|---|---|
| `APP_USER` | Security Core | `User`; unique username index |
| `PARTNER_PARTNER` | Partner Core | `Partner`; unique Partner number |
| `QUOTE_QUOTE` | Quote Core | `Quote`; unique Quote number; local Partner and Policy-reference columns |
| `POLICY_POLICY` | Policy Core | `Policy`; unique Policy number; local Partner-reference columns |
| `ACCOUNT_ACCOUNT` | Account Core | `Account`; local Policy/Partner values and accounting period |
| `ACCOUNT_ACCOUNT_DOCUMENT` | Account Core | `AccountDocument`; in-domain foreign key to `ACCOUNT_ACCOUNT` |

Cross-domain reference columns such as `POLICY_NO`, `PARTNER_NO`, and `PARTNER_ID` have no foreign
key to another domain table. The corresponding local embedded types are documented in
[Domain model and runtime flows](domain-model-and-flows.md).

## Jmix Entity Names

Persistent entity names identify their owning domain:

| Java type | Jmix entity name |
|---|---|
| `Partner` | `partner_Partner` |
| `Quote` | `quote_Quote` |
| `Policy` | `policy_Policy` |
| `Account` | `account_Account` |
| `AccountDocument` | `account_AccountDocument` |
| `User` | `security_User` |

Non-persistent API DTO entities use the `<domain>_api_` prefix, for example
`partner_api_PartnerDto` and `policy_api_PolicyDto`.

The entity name is part of the boundary. JPQL strings, XML loaders, fetch plans, and metadata calls
inside one module may reference that module's entities. They must not refer to a foreign domain's
persistent entity name.

## Entity-to-Schema Synchronization

An entity change normally touches:

- the persistent class in `<domain>-core`;
- the owning Core changelog and a new immutable change set;
- entity messages and instance-name behavior where relevant;
- Core resource roles for new attributes;
- domain test fixtures and assertions;
- service, view, and integration tests affected by the field.

Current architecture rules compare required unique entity columns with selected Liquibase unique
constraints. They do not replace a complete migration review. Java metadata and the final database
shape must be checked together.

## Security Model

Jmix resource roles are split by ownership and layer.

### Core Roles

Core roles contain entity and entity-attribute policies. Domains with a persistent UI surface
currently expose read and manage capabilities, for example:

- `partner-core-read` and `partner-core-manage`;
- `quote-core-read` and `quote-core-manage`;
- `policy-core-read` and `policy-core-manage`;
- `account-core-read` and `account-core-manage`;
- `security-core-manage`.

Core roles do not contain view or menu policies.

### UI Roles

UI roles contain view and menu policies for their owning module, for example:

- `partner-ui-read` and `partner-ui-manage`;
- `quote-ui-read` and `quote-ui-manage`;
- `policy-ui-read` and `policy-ui-manage`;
- `account-ui-read` and `account-ui-manage`;
- `security-ui-manage`.

UI roles do not contain entity or attribute policies. A user-facing view ID and its menu ID must
exist before a role can reference them.

### Application Personas

The webapp composes domain capabilities into job-oriented roles:

| Persona | Partner | Quote | Policy | Account | Application shell |
|---|---|---|---|---|---|
| `insurance-agent` | Manage Core + UI | Manage Core + UI | Read Core + UI | Read Core + UI | `ui-minimal` |
| `insurance-backoffice` | Manage Core + UI | Manage Core + UI | Manage Core + UI | Manage Core + UI | `ui-minimal` |

`UiMinimalRole` supplies the Login and Main views and Jmix UI minimum policies. The application
personas extend module role interfaces; they do not repeat individual entity, attribute, view, or
menu annotations.

`FullAccessRole` (`system-full-access`) lives in Security API and is the explicit wildcard role. The
initial `admin` user is created by the Security Liquibase changelog and assigned this role. Current
development credentials are `admin` / `admin`.

## Web Security

`DatabaseUserRepository` loads `User` entities for authentication. Standard Jmix Flow UI security
protects the application views. `WebappSecurityConfiguration` additionally permits `/public/**`
through a custom high-priority filter chain; no current controller populates that path.

## Security Guardrails

The architecture suite currently checks:

- Core roles contain only Core policy annotations;
- UI roles contain only UI policy annotations;
- role codes carry the owning module and layer prefix;
- wildcard policies appear only in the designated full-access role;
- view and menu policy IDs resolve to existing artifacts;
- user-facing views have a concrete view policy;
- selected Liquibase constraints match entity metadata.

`RoleCompositionTest` separately protects the intended capabilities of the two application
personas.

## Modification Checklist

For a persistent model or security change:

1. Change the entity and its owning Core changelog together.
2. Do not add cross-domain foreign keys or alter another module's table.
3. Update Core entity/attribute policies for persistent attributes.
4. Update UI view/menu policies for user-facing UI resources.
5. Compose a domain capability into application personas only when the job role requires it.
6. Add or update role-composition tests when a persona changes.
7. Run the focused architecture suite and the affected integration/UI tests.

## Primary Source Files

- [`webapp` master changelog](../../webapp/src/main/resources/com/insurance/app/liquibase/changelog.xml)
- [`policy-core` changelog](../../policy/policy-core/src/main/resources/com/insurance/policy/core/liquibase/changelog.xml)
- [`account-core` changelog](../../account/account-core/src/main/resources/com/insurance/account/core/liquibase/changelog.xml)
- [`PolicyCoreReadRole`](../../policy/policy-core/src/main/java/com/insurance/policy/core/security/PolicyCoreReadRole.java)
- [`PolicyUiReadRole`](../../policy/policy-ui/src/main/java/com/insurance/policy/ui/security/PolicyUiReadRole.java)
- [`InsuranceAgentRole`](../../webapp/src/main/java/com/insurance/app/security/InsuranceAgentRole.java)
- [`InsuranceBackOfficeRole`](../../webapp/src/main/java/com/insurance/app/security/InsuranceBackOfficeRole.java)
- [`RoleCompositionTest`](../../webapp/src/test/java/com/insurance/app/security/RoleCompositionTest.java)
