# Architecture Documentation — jmix-insurance

## Overview

`jmix-insurance` is a modular insurance management demo application built on the **Jmix 2** framework (Spring Boot 3, Vaadin 24, EclipseLink JPA). It demonstrates how to structure a Jmix project as a set of independently publishable domain add-ons assembled into a single runnable application.

The application covers four core insurance domains — **Partners**, **Quotes**, **Policies**, and **Accounts** — plus shared infrastructure for security, product configuration, and standalone test support utilities.

---

## Module Structure

The project is a Gradle composite build with 13 sub-builds declared in the root `settings.gradle`.

```
jmix-insurance/
├── webapp/                 ← Runnable Spring Boot application
├── test-support/           ← Standalone test support library (EntityTestData, Assertions, etc.)
├── security/               ← Security API, core, UI, and starters
├── partner/                ← Partner API, core, UI, and starters
├── policy/                 ← Policy API, core, UI, and starters
├── quote/                  ← Quote API, core, UI, and starters
├── account/                ← Account API, core, UI, and starters
├── product/                ← Product API, core, UI, and starters
├── theme/                  ← Shared Jmix theme add-on
├── ui-sections/            ← Shared Flow UI section contract library
└── test-support-ui/        ← Flow UI test helper library
```

### Domain Add-on Layout

Each domain module follows a consistent six-artifact pattern:

| Artifact | Contents |
|---|---|
| `<domain>-api` | Service interface, DTOs, events — no JPA entities or Liquibase |
| `<domain>-api-starter` | Spring Boot auto-configuration for the api artifact |
| `<domain>-core` | JPA entities, service implementations, event listeners |
| `<domain>-core-starter` | Spring Boot auto-configuration for the core artifact |
| `<domain>-ui` | Jmix Flow UI views (controllers + XML descriptors) |
| `<domain>-ui-starter` | Spring Boot auto-configuration for the ui artifact |

- The `product-*` modules are exceptions: `product-api` contains only enum DTOs (no persistence needed), and `product-core` contains only `ProductConfiguration` with no entities.
- Host UI modules may add a small `<domain>-ui-api` artifact when other modules need to contribute UI into a host-owned view. `partner-ui-api` and `policy-ui-api` expose typed `ViewSection` contracts; they are UI-specific and intentionally separate from the domain `*-api` modules.
- The `test-support` module is a standalone standard Java library module rather than a full Jmix add-on. It is added to the test classpath dependencies of domain modules.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Jmix 2.8.1 (Spring Boot 3, Vaadin 24) |
| Persistence | EclipseLink JPA |
| Database | HSQLDB (file-based, embedded) |
| Schema migrations | Liquibase |
| Build | Gradle (composite build), Jmix Gradle Plugin 2.8.1 |
| Security | Jmix Security (Spring Security) |
| UI | Jmix Flow UI (Vaadin Flow + Jmix view framework) |
| Test | JUnit 5, Spring Boot Test, AssertJ, Jmix UI Test Assist |

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                            webapp                               │
│  Spring Boot application, assembles all domain add-ons          │
│  LoginView / MainView / app menu / Liquibase master changelog   │
└────────────┬──────┬─────────┬──────┬─────────┬──────┬───────────┘
             │      │         │      │         │      │
   ┌─────────▼──┐   │   ┌─────▼────┐ │   ┌─────▼────┐ │   ┌──────────┐
   │  security  │   │   │ partner  │ │   │  quote   │ │   │  product │
   │  User      │   │   │ Partner  │ │   │ Quote    │ │   │  enums   │
   │  roles     │   │   │ Service  │ │   │ Service  │ │   │  config  │
   └────────────┘   │   └─────┬────┘ │   └────┬─────┘ │   └──────────┘
                    │         │      │        │       │
                    │         │      │        │       │
                    │   ┌─────▼──┐   │   ┌────▼─────┐ │
                    └──►│ policy │◄──┼───│ account  │─┘
                        │ Policy │   │   │ Account  │
                        │ Service│◄──┘   │ Service  │
                        └────────┘       └──────────┘
```

Domain dependencies (read as "depends on"):

```
quote-core    → policy-api, partner-api, product-api
policy-core   → product-api
account-core  → policy-api, product-api
partner-core  → partner-api
partner-ui-api → ui-sections
policy-ui-api → ui-sections
policy-ui → partner-ui-api, policy-ui-api
partner-ui/account-ui → partner-ui-api, policy-ui-api
webapp → all *-starter artifacts
```

All inter-domain communication goes through **API interfaces** (`*Service` + DTOs). Core modules never import from another core module directly. All generic test fixtures (e.g. `EntityTestData`, JUnit extensions) are inherited at test-scope from the standalone `test-support` library.

---

## Domain Model

### Entity Hierarchy

All persistent entities are self-contained and do not inherit from a shared mapped superclass:

```
├── Partner        (PARTNER_PARTNER)
├── Policy         (POLICY_POLICY)
├── Quote          (QUOTE_QUOTE)
├── Account        (ACCOUNT_ACCOUNT)
└── AccountDocument (ACCOUNT_ACCOUNT_DOCUMENT)
```

Each persistent entity directly declares its own Jmix standard auditing and soft-delete fields:
- `Integer version` (@Version)
- `String createdBy`, `OffsetDateTime createdDate` (@CreatedBy, @CreatedDate)
- `String lastModifiedBy`, `OffsetDateTime lastModifiedDate` (@LastModifiedBy, @LastModifiedDate)
- `String deletedBy`, `OffsetDateTime deletedDate` (soft-delete fields)

`User` has Jmix entity name `security_User`, maps to table `APP_USER`, and implements
`JmixUserDetails` directly. The table name remains historical; the Jmix entity name follows the
security module prefix contract.

### Entity Details

#### Partner
Table `PARTNER_PARTNER`. Key fields: `partnerNo` (unique, generated `PT-NNNNN`), `firstName`, `lastName`.

#### Policy
Table `POLICY_POLICY`. Key fields: `policyNo` (unique, generated `HC-YYYY-NNNNNN`), `partnerNo` (FK by value, not JPA relation), `insuranceProduct`, `coverageStart`, `coverageEnd` (auto-set to `coverageStart + 1 year`), `premium`, `paymentFrequency`.

#### Quote
Table `QUOTE_QUOTE`. Key fields: `quoteNo` (unique, `QT-NNNNN`), `partnerNo`, `status` (`QuoteStatus` enum), `productType`, `productVariant`, `paymentFrequency`, `insuranceProduct`, `effectiveDate`, `squareMeters`, `calculatedPremium`, `validFrom`/`validUntil`. After acceptance: `createdPolicyNo`, `createdPolicyId`.

#### Account
Table `ACCOUNT_ACCOUNT`. Key fields: `policyId` (UUID as String, references `Policy`), `accountNo`, `accountBalance`. Has a composition of `AccountDocument` records.

#### AccountDocument
Table `ACCOUNT_ACCOUNT_DOCUMENT`. Belongs to `Account`. Key fields: `type` (`DocumentType` enum: `CREDIT`/`DEBIT`), `documentDate`, `amount`, `description`.

### Cross-Domain References

Domain boundaries are maintained by storing references as **value types** (strings), not JPA foreign keys:

| Field | References | How |
|---|---|---|
| `Policy.partnerNo` | Partner | String value |
| `Quote.partnerNo` | Partner | String value |
| `Account.policyId` | Policy | UUID as String |
| `Quote.createdPolicyNo/Id` | Policy | String values written on acceptance |

This keeps domains independently deployable without shared JPA entity classes.

---

## Business Logic & Services

### Service Layer

Each domain exposes its operations through a **service interface** in the `*-api` module and provides a Spring bean implementation in `*-core`.

| Interface | Implementation Bean | Key Operations |
|---|---|---|
| `PartnerService` | `partner_PartnerService` | find (wildcard search), getByNo, save (with no generation) |
| `PolicyService` | `policy_PolicyService` | createPolicy, findPolicyById |
| `QuoteService` | `quote_QuoteService` | accept, reject |
| `AccountService` | `account_AccountService` | createAccount, getAccountBalance |

### Quote Acceptance Flow (main cross-domain operation)

```
QuoteService.accept(quoteId)
  → loads Quote
  → builds CreatePolicyRequestDto
  → calls PolicyService.createPolicy(request)
      → generates policyNo (HC-YYYY-NNNNNN)
      → sets coverageEnd = effectiveDate + 1 year
      → saves Policy
      → publishes PolicyCreatedEvent
          → PolicyCreatedEventListener
              → calls AccountService.createAccount(policyDto)
                  → creates Account
                  → generates AccountDocument per payment interval
  → marks Quote ACCEPTED
  → stores createdPolicyNo + createdPolicyId on Quote
```

### Premium Calculation

Premium is calculated client-side in `QuoteDetailView` via `InsuranceProduct.calculatePremium(squareMeters)`. The calculation is a product-domain concern encapsulated in the `product-api` enum — no service call required.

### Account Balance

`AccountService.getAccountBalance(policyNo, effectiveDate)`:
1. Loads the Account for the given `policyNo`.
2. Looks up the Policy to get `coverageEnd`.
3. Throws `IllegalArgumentException` if `effectiveDate > coverageEnd`.
4. Sums all `AccountDocument.amount` values with `documentDate ≤ effectiveDate`.

---

## UI Layer

The application uses **Jmix Flow UI** (Vaadin Flow with Jmix view framework). Each view consists of a Java controller and an XML descriptor.

### Views

| View ID | Route | Type |
|---|---|---|
| `app_LoginView` | `/login` | Login |
| `app_MainView` | `/` | Main shell with navigation |
| `partner_Partner.list` | `/partners` | `StandardListView<Partner>` |
| `partner_Partner.detail` | `/partners/:id` | `StandardDetailView<Partner>` |
| `policy_Policy.list` | `/policies` | `StandardListView<Policy>` (read-only, no create) |
| `policy_Policy.detail` | `/policies/:id` | Detail + cross-module partner/account sections |
| `quote_Quote.list` | `/quotes` | `StandardListView<Quote>` + accept/reject actions |
| `quote_Quote.detail` | `/quotes/:id` | Detail with partner combo, premium calculator |
| `account_Account.list` | `/accounts` | `StandardListView<Account>` |
| `account_Account.detail` | `/accounts/:id` | Detail + AccountDocument grid |
| `security_User.list` | `/users` | `StandardListView<User>` |
| `security_User.detail` | `/users/:id` | Detail with password management |

### Notable UI Interactions

- **Quote detail**: Populates a `EntityComboBox<PartnerDto>` lazily from `PartnerService.findPartners()`. The premium is only calculated on user demand; `saveAndCloseButton` is disabled until the premium is calculated.
- **Policy detail**: Owns the policy layout and renders cross-module right-column sections. `partner-ui` contributes the policy-holder section, and `account-ui` contributes the live account-balance section.
- **Partner detail**: Owns the partner layout and renders cross-module right-column sections. `policy-ui` and `account-ui` contribute `PartnerSection` beans whose content is implemented as Jmix fragments; `partner-ui` owns the `details` wrapper, ordering, spacing, and title rendering.
- **Quote list**: Custom `rejectAction` and `acceptAction` call the service layer and refresh the grid.

### Cross-Module UI Sections

Reusable cross-module view sections use the generic `ui-sections` contract:

```java
public interface ViewSection<C> {
  String titleMessageKey();

  Component createContent(C context, FragmentOwner fragmentOwner);
}
```

Host views define typed contracts in their UI API artifact, for example
`PartnerSection extends ViewSection<PartnerViewContext>` or
`PolicySection extends ViewSection<PolicyViewContext>`. The concrete context contains only stable
host-view data such as business keys and ids. Contributor modules inject their own services, create
their own Jmix fragments with the provided `FragmentOwner`, and return only the content component.
The host view renders the surrounding `details` component and resolves the section title from the
contributor's message bundle.

---

## Security

### Roles

| Role Code | Scope | Effect |
|---|---|---|
| `system-full-access` | All | Full entity, attribute, view, menu, and specific policy access |
| `ui-minimal` | UI | Access to `app_MainView`, `app_LoginView`, `ui.loginToUi` resource |
| `insurance-agent` | UI/entity | Manages Partner and Quote; reads Policy and Account |
| `insurance-backoffice` | UI/entity | Manages Partner, Quote, Policy, and Account |
| `security-core-manage` / `security-ui-manage` | UI/entity | Manages `security_User` entity and User list/detail views |

### Authentication

- `DatabaseUserRepository` (extends `AbstractDatabaseUserRepository<User>`) manages `User` persistence.
- The system user (`initSystemUser`) is granted `FullAccessRole` automatically.
- An `InsuranceAppSecurityConfiguration` filter chain permits all `/public/**` requests without authentication (reserved for future use, no controllers currently populate this path).

### Default Credentials

The `admin` user is created via Liquibase (`SEC_ROLE_ASSIGNMENT`) and assigned `FullAccessRole`. Default credentials: `admin` / `admin`.

---

## Database Schema

Liquibase changelogs are decentralized: each module owns its changelog and includes it from the application master.

### Master Changelog Assembly Order

```
webapp/liquibase/changelog.xml
  ├── /io/jmix/data/liquibase/changelog.xml           (Jmix platform)
  ├── /io/jmix/flowuidata/liquibase/changelog.xml
  ├── /io/jmix/securitydata/liquibase/changelog.xml
  ├── /com/insurance/security/liquibase/changelog.xml
  ├── /com/insurance/partner/core/liquibase/changelog.xml
  ├── /com/insurance/quote/core/liquibase/changelog.xml
  ├── /com/insurance/policy/core/liquibase/changelog.xml
  ├── /com/insurance/account/core/liquibase/changelog.xml
  └── /com/insurance/app/liquibase/changelog/          (app-level migrations)
```

### Tables

| Table | Module | Notes |
|---|---|---|
| `APP_USER` | security | Unique index on `USERNAME` |
| `PARTNER_PARTNER` | partner-core | Unique on `PARTNER_NO` |
| `POLICY_POLICY` | policy-core | Unique on `POLICY_NO` |
| `QUOTE_QUOTE` | quote-core | Unique on `QUOTE_NO` |
| `ACCOUNT_ACCOUNT` | account-core | Index on `ACCOUNT_NO` |
| `ACCOUNT_ACCOUNT_DOCUMENT` | account-core | FK to `ACCOUNT_ACCOUNT`, index on `ACCOUNT_ID` |

---

## Testing Strategy

### Layers

| Layer | Annotation | Location |
|---|---|---|
| Integration (service) | `@SpringBootTest` + `@ActiveProfiles("test")` | `webapp/src/test` |
| UI integration | `@UiTest` + `FlowuiTestAssistConfiguration` | `webapp/src/test` |

### Infrastructure

- `BaseIntegrationTest` — base class; authenticates as admin via `@ExtendWith(AuthenticatedAsAdmin.class)`.
- `DatabaseCleanup` — `@BeforeEach` JDBC truncation of all domain tables (ordered to respect FKs).
- `*DataProvider` — `Consumer<Entity>` implementations in `testFixtures` of each module; provide sensible defaults for required fields.
- `EntityTestData` — generic factory/save helper using `DataManager` with validation before save. Auto-configured via `TestSupportConfiguration` whenever `test-support` is in the test classpath.
- `InsuranceAssertions` — AssertJ entry point with domain-specific fluent assertions for `Partner`, `Policy`, `Quote`, and `Account`.

---

## Agent Harness

The repo is intended to be safe for autonomous coding agents. The fast deterministic loop is:

```shell
./gradlew spotlessApply
./gradlew :<module>:<layer>:compileJava
./gradlew :<module>:check
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

Use the root `./gradlew check` as the final broad validation. The architecture test is the
canonical guardrail for:

- API/core/UI package boundaries.
- No Flow UI leakage into core modules.
- No direct foreign persistent entity-name references across domain modules.
- No cross-module UI implementation dependencies; domain UI modules must use foreign `*-api`,
  foreign `*-ui-api`, shared `ui-sections`, or the `webapp` composition root.
- Persistent Jmix entity names prefixed by each domain build's `jmixDomainProjectId`.
- No Lombok on persistent entities and no constructor-created Jmix entities.
- View/menu security policies that reference real view and menu ids.
- Domain builds delegating shared build quality rules to `gradle/jmix-domain-conventions.gradle`.

Project-specific skills live in `.skills/`. The most important app-specific skills are:

- `insurance-testing` for `BaseIntegrationTest`, `EntityTestData`, test fixtures, cleanup, and UI helper patterns.
- `insurance-security-roles` for domain role policies and app persona composition.

Agents must preserve these boundaries: use domain APIs rather than foreign core entities, keep
business logic in services/listeners, keep user-visible text in message bundles, and update role,
menu, view, changelog, and tests together when a user-facing Jmix surface changes.

---

## Key Design Decisions

### API/Core Split
Every domain exposes a thin API module (`interface` + DTOs, no persistence classes). Consumers depend only on the API artifact. This allows swapping implementations, independent versioning, and prevents direct entity coupling between domains.

### Cross-Domain References as Strings
Entities reference objects in other domains by storing IDs or business keys as plain strings instead of JPA associations. This removes JPA-level coupling across module boundaries and mirrors a service-oriented integration style within a monolith.

### Decentralized Liquibase
Each module ships its own changelog files on the classpath. The application-level master changelog assembles them in dependency order. Adding a new domain module only requires adding one `<include>` line to the master changelog.

### Event-Driven Account Creation
`PolicyServiceCore` publishes `PolicyCreatedEvent` after persisting a new policy. `PolicyCreatedEventListener` in `account-core` handles this event to create the initial `Account` and `AccountDocument` entries. This keeps `policy-core` free of any direct dependency on `account-core`.

### Dedicated `test-support` Module
Generic test fixtures (`EntityTestData`, `AuthenticatedAsAdmin`, custom assertions) are decoupled from domain-specific modules and extracted to a dedicated standard Java library `test-support` to prevent classpath pollution and keep domain libraries lean.
