# ADR-0011: Keep Migrations and Security Policies Domain-Owned

- **Status:** Accepted
- **Decision period:** 2026-06-02–2026-06-09
- **Recorded:** 2026-07-08
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0001: Use a Modular Monolith Built from Jmix Add-ons](0001-use-a-modular-monolith-built-from-jmix-add-ons.md), [ADR-0002: Apply Asymmetric API/Core/UI Layering](0002-apply-asymmetric-api-core-ui-layering.md), [ADR-0010: Enforce Jmix Metadata and Resource Conventions in Architecture Tests](0010-enforce-jmix-metadata-and-resource-conventions-in-architecture-tests.md)

## Context

Modularizing Java packages and Gradle dependencies is insufficient if the web application still
owns one central set of database migrations and security policies for all domains. A change to the
Policy model would then require Policy code in one module, a migration in the application module,
and permissions in another central file. The source tree would show a domain boundary while the
surrounding Jmix artifacts remained centrally owned.

Liquibase changelogs and Jmix resource roles are part of a domain's implementation. A module that
introduces an entity knows how its table evolves and which entity attributes may be read or
modified. A module that introduces views and menu entries knows which UI resources must be
protected. Those details change together with the module.

The final application still has responsibilities that no individual domain can own. It selects
which modules are deployed, establishes the order of the changelogs in the shared schema, and
defines job-oriented personas that combine permissions from several domains. For example, an
insurance agent manages Partners and Quotes but only reads Policies and Accounts.

The ownership boundary must therefore distinguish domain-owned definitions from application-level
composition.

## Decision Drivers

- Keep an entity change, its schema migration, and its entity permissions in the same bounded
  context.
- Keep a view and its view and menu policies in the same UI artifact.
- Allow domain teams to evolve their modules without editing central lists of individual tables,
  entities, attributes, views, or menu entries.
- Make domain modules closer to independently publishable Jmix add-ons.
- Preserve a practical path for extracting a domain with its schema history and local security
  definitions.
- Let the deployable application decide which modules are installed and how their capabilities form
  business personas.
- Continue using one process, one transaction manager, and one physical database for the initial
  modular monolith.
- Make ownership visible to coding agents through file location and mechanically checkable
  conventions.

## Considered Options

### 1. Centralize migrations and all security policies in the web application

The `webapp` could contain every Liquibase change set and every entity, view, and menu policy.

This resembles a conventional single Jmix application and makes the complete schema and permission
set visible in one place. It also turns the web application into an owner of domain implementation
details. Almost every domain change crosses module boundaries, and extracting a module requires
reconstructing its migration history and security model from central files.

### 2. Let every domain own everything, including final user personas and runtime activation

Each module could provide complete roles for end users and independently activate its migrations.

This maximizes local autonomy but assigns application-specific knowledge to reusable domain
add-ons. A Policy module cannot decide whether an "Insurance Agent" may manage or only read a
Policy; that decision depends on the application in which Policy is installed. Independently
activated migrations also obscure the deliberate ordering of changes in the single shared schema.

### 3. Keep definitions in domains and compose them in the web application

Core modules own schema changes and entity policies. UI modules own view and menu policies. The
`webapp` includes module changelogs and combines module roles into application personas.

This keeps detailed knowledge close to the artifacts it protects while retaining an explicit
composition root for deployment-wide decisions.

## Decision

We choose **option 3: domain-owned definitions with application-level composition**.

This is one ownership decision applied consistently to two kinds of Jmix artifacts: database
migrations and resource roles.

### Schema migrations

Each domain Core artifact owns:

- the Liquibase master changelog for that Core artifact;
- the ordered change sets that create and evolve its tables, columns, indexes, and constraints;
- the alignment between its persistent entity metadata and its physical schema.

The `webapp` owns only the composition of the installed schema. Its master changelog includes Jmix
framework changelogs followed by the domain master changelogs and finally genuine
application-owned changes.

The include order is explicit because all modules initially share one database and may need a
deterministic bootstrap order. Inclusion does not transfer ownership: a change to the Account
schema belongs in `account-core`, not beside the application's master changelog.

In accordance with [ADR-0002](0002-apply-asymmetric-api-core-ui-layering.md) and
[ADR-0005](0005-store-cross-domain-references-as-local-domain-representations.md), a module-owned
changelog must not recreate a domain dependency through the database. Cross-domain references are
stored as local values rather than foreign JPA associations. A module must not modify another
domain's tables or introduce cross-domain joins as part of its own operational model.

### Security policies

Security definitions are split along the same Core/UI boundary as the implementation:

- a domain Core artifact defines fine-grained resource roles for its entities and attributes;
- a domain UI artifact defines resource roles for its views and menu entries;
- reusable roles distinguish capabilities such as read-only and manage access;
- role codes identify both their owning domain and layer.

The `webapp` composes these capabilities into business personas. Application roles such as
`InsuranceAgentRole` and `InsuranceBackOfficeRole` extend the required Core and UI roles from the
installed domains. They decide which capabilities a person receives, but they do not duplicate the
individual entity, attribute, view, or menu policies.

For example, the application can compose Partner and Quote manage roles with Policy and Account
read roles. The Policy module remains the owner of what "Policy read" technically permits; the
application remains the owner of whether an insurance agent receives that capability.

### Composition is explicit application architecture

The `webapp` is not a passive launcher. It is the composition root that makes deployment-wide
decisions:

- which Jmix add-ons and domain starters are installed;
- which module changelogs participate in the shared schema and in which order;
- which module-level security capabilities form each application persona.

Consequently, adding a domain requires an explicit composition change in the application. This is
intentional: publishing a module and installing it in a particular application are different
decisions.

## Consequences

### Positive

- Schema and permission changes normally stay inside the domain and layer that own the affected
  artifact.
- A coding agent changing an entity has a clear place for its migration and entity policies.
- A coding agent changing a view has a clear place for view and menu policies.
- Domain add-ons can be published with their own schema history and reusable security capabilities.
- The application can create different personas without copying or reaching into individual policy
  declarations.
- Extraction has a better starting point because a domain's tables, migration history, and local
  permissions can be identified by module.
- Reviews can distinguish domain behavior from application assembly.

### Negative

- Schema evolution is decentralized across several master changelogs, so understanding the entire
  physical database requires following the application include list.
- The shared database still requires a deterministic global migration order.
- Installing a new domain requires both a module dependency and an update to the webapp composition.
- A new business persona may require selecting many small Core and UI roles.
- Read and manage variants create more role interfaces than one central broad role would.
- Removing or extracting a module requires changing application-level role compositions as well as
  changelog assembly.
- A module can be present technically but remain unusable if its changelog or security capabilities
  are not composed correctly.

### Deliberately accepted limitations

- Modular changelog ownership does not provide database isolation. The modular monolith continues
  to use one physical schema and one transaction manager.
- Module-owned security roles are reusable capabilities, not complete authorization for every
  application. Final personas remain application-specific.
- This structure makes later extraction easier but does not make it automatic. A separate service
  still needs database provisioning, identity propagation, externally enforceable authorization,
  and a migration plan for existing data.

## Guardrails

- Put production schema changes in the Core module that owns the affected entity.
- Keep each domain master changelog responsible only for its own database objects.
- Keep entity and attribute policies in `core.security` and view and menu policies in
  `ui.security`.
- Prefix domain role codes with the owning domain and layer.
- Restrict broad wildcard permissions to explicitly designated full-access roles.
- Compose business personas in the application instead of duplicating domain policies there.
- Test important persona compositions so accidental read/manage changes become visible.
- Use the Jmix-aware architecture rules from ADR-0010 to validate role layering, referenced view and
  menu IDs, and selected entity-to-Liquibase invariants.
- Treat a missing application include or role composition as an installation error, not as a reason
  to move the underlying definition into `webapp`.

## Evidence in the Codebase

- [`webapp` Liquibase master changelog](../../webapp/src/main/resources/com/insurance/app/liquibase/changelog.xml) composes Jmix, domain, and application changelogs.
- [`policy-core` Liquibase master changelog](../../policy/policy-core/src/main/resources/com/insurance/policy/core/liquibase/changelog.xml) owns Policy schema evolution.
- [`account-core` Liquibase master changelog](../../account/account-core/src/main/resources/com/insurance/account/core/liquibase/changelog.xml) owns Account schema evolution.
- [`PolicyCoreReadRole`](../../policy/policy-core/src/main/java/com/insurance/policy/core/security/PolicyCoreReadRole.java) protects the persistent Policy model.
- [`PolicyUiReadRole`](../../policy/policy-ui/src/main/java/com/insurance/policy/ui/security/PolicyUiReadRole.java) protects Policy views and menu entries.
- [`InsuranceAgentRole`](../../webapp/src/main/java/com/insurance/app/security/InsuranceAgentRole.java) composes manage and read capabilities from several domains.
- [`InsuranceBackOfficeRole`](../../webapp/src/main/java/com/insurance/app/security/InsuranceBackOfficeRole.java) defines a different application-wide composition.
- [`RoleCompositionTest`](../../webapp/src/test/java/com/insurance/app/security/RoleCompositionTest.java) protects the intended persona composition.
- [`SecurityRoleLayerRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/security/SecurityRoleLayerRules.java) enforce the Core/UI policy split, role-code convention, and wildcard restriction.

The same ownership model is described in the article draft under
[`Schema and Security Ownership`](../article-draft.md#454-schema-and-security-ownership).

## Follow-up Decisions

- Define how an extracted service publishes and applies its migrations independently of the
  monolith's master changelog.
- Define how application personas map to service-level scopes or claims after extraction.
- Consider an executable completeness check that every installed domain changelog is included and
  every user-facing domain capability is intentionally composed or explicitly excluded.
