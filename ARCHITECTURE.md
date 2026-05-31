# Architecture Documentation — jmix-insurance

## Overview

`jmix-insurance` is a modular insurance management demo application built on the **Jmix 2** framework (Spring Boot 3, Vaadin 24, EclipseLink JPA). It demonstrates how to structure a Jmix project as a set of independently publishable domain add-ons assembled into a single runnable application.

The application covers four core insurance domains — **Partners**, **Quotes**, **Policies**, and **Accounts** — plus shared infrastructure for common base entities, security, and product configuration.

---

## Module Structure

The project is a Gradle composite build with 13 sub-builds declared in the root `settings.gradle`.

```
jmix-insurance/
├── insurance-app/          ← Runnable Spring Boot application
├── common/                 ← Shared base entity + test support utilities
├── security/               ← User entity, roles, DatabaseUserRepository
├── partner-core/           ← Partner domain (entity, service, UI)
├── partner-api/            ← PartnerService interface + PartnerDto
├── policy-core/            ← Policy domain (entity, service, UI, event)
├── policy-api/             ← PolicyService interface, PolicyDto, events
├── quote-core/             ← Quote domain (entity, service, UI)
├── quote-api/              ← QuoteService interface, QuoteDto, enums
├── account-core/           ← Account domain (entity, service, UI)
├── account-api/            ← AccountService interface + AccountDto
├── product-core/           ← Product configuration (no persistence)
└── product-api/            ← InsuranceProduct, ProductType, enums
```

### Domain Add-on Layout

Each domain module follows a consistent four-artifact pattern:

| Artifact | Contents |
|---|---|
| `<domain>-api` | Service interface, DTOs, events — no persistence |
| `<domain>-api-starter` | Spring Boot auto-configuration for the api artifact |
| `<domain>-core` | JPA entities, service implementations, event listeners |
| `<domain>-core-starter` | Spring Boot auto-configuration for the core artifact |
| `<domain>-ui` | Jmix Flow UI views (controllers + XML descriptors) |
| `<domain>-ui-starter` | Spring Boot auto-configuration for the ui artifact |

The `product-*` modules are exceptions: `product-api` contains only enum DTOs (no persistence needed), and `product-core` contains only `ProductConfiguration` with no entities.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
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
│                        insurance-app                            │
│  Spring Boot application, assembles all domain add-ons          │
│  LoginView / MainView / app menu / Liquibase master changelog   │
└────────────┬──────┬──────┬──────┬──────┬──────┬────────────────┘
             │      │      │      │      │      │
   ┌─────────▼──┐ ┌─▼────┐ ┌─▼──────┐ ┌─▼─────────┐ ┌──────────┐
   │  security  │ │common│ │partner │ │   quote   │ │  product │
   │  User      │ │ base │ │Partner │ │  Quote    │ │   enums  │
   │  roles     │ │entity│ │Service │ │  Service  │ │  config  │
   └────────────┘ └──────┘ └───┬────┘ └────┬──────┘ └──────────┘
                                │           │
                         ┌──────▼──┐  ┌─────▼──────┐
                         │ policy  │  │  account   │
                         │ Policy  │◄─│  Account   │
                         │ Service │  │  Service   │
                         └─────────┘  └────────────┘
```

Domain dependencies (read as "depends on"):

```
quote-core    → policy-api, partner-api, product-api
policy-core   → partner-api, account-api, product-api
account-core  → policy-api, product-api
partner-core  → partner-api
security      → common
insurance-app → all *-starter artifacts
```

All inter-domain communication goes through **API interfaces** (`*Service` + DTOs). Core modules never import from another core module directly.

---

## Domain Model

### Entity Hierarchy

All persistent entities extend `CommonEntity` (mapped superclass):

```
CommonEntity (abstract)
├── UUID id           @JmixGeneratedValue
├── Integer version   @Version
├── String createdBy, OffsetDateTime createdDate
├── String lastModifiedBy, OffsetDateTime lastModifiedDate
└── String deletedBy, OffsetDateTime deletedDate   (soft-delete)

CommonEntity
├── Address        (COMMON_ADDRESS)
├── Partner        (PARTNER_PARTNER)
├── Policy         (POLICY_POLICY)
├── Quote          (QUOTE_QUOTE)
├── Account        (ACCOUNT_ACCOUNT)
└── AccountDocument (ACCOUNT_ACCOUNT_DOCUMENT)
```

`User` (`APP_USER`) does not extend `CommonEntity`; it implements `JmixUserDetails` directly.

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
| `policy_Policy.detail` | `/policies/:id` | Detail + partner info panel + live account balance |
| `quote_Quote.list` | `/quotes` | `StandardListView<Quote>` + accept/reject actions |
| `quote_Quote.detail` | `/quotes/:id` | Detail with partner combo, premium calculator |
| `account_Account.list` | `/accounts` | `StandardListView<Account>` |
| `account_Account.detail` | `/accounts/:id` | Detail + AccountDocument grid |
| `app_User.list` | `/users` | `StandardListView<User>` |
| `app_User.detail` | `/users/:id` | Detail with password management |

### Notable UI Interactions

- **Quote detail**: Populates a `EntityComboBox<PartnerDto>` lazily from `PartnerService.findPartners()`. The premium is only calculated on user demand; `saveAndCloseButton` is disabled until the premium is calculated.
- **Policy detail**: Calls `AccountService.getAccountBalance()` reactively when the user changes the effective-date picker.
- **Quote list**: Custom `rejectAction` and `acceptAction` call the service layer and refresh the grid.

---

## Security

### Roles

| Role Code | Scope | Effect |
|---|---|---|
| `system-full-access` | All | Full entity, attribute, view, menu, and specific policy access |
| `ui-minimal` | UI | Access to `app_MainView`, `app_LoginView`, `ui.loginToUi` resource |

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
insurance-app/liquibase/changelog.xml
  ├── /io/jmix/data/liquibase/changelog.xml           (Jmix platform)
  ├── /io/jmix/flowuidata/liquibase/changelog.xml
  ├── /io/jmix/securitydata/liquibase/changelog.xml
  ├── /com/insurance/common/liquibase/changelog.xml
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
| `COMMON_ADDRESS` | common | Standalone address entity |
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
| Integration (service) | `@SpringBootTest` + `@ActiveProfiles("test")` | `insurance-app/src/test` |
| UI integration | `@UiTest` + `FlowuiTestAssistConfiguration` | `insurance-app/src/test` |

### Infrastructure

- `BaseIntegrationTest` — base class; authenticates as admin via `@ExtendWith(AuthenticatedAsAdmin.class)`.
- `DatabaseCleanup` — `@BeforeEach` JDBC truncation of all domain tables (ordered to respect FKs).
- `*DataProvider` — `Consumer<Entity>` implementations in `testFixtures` of each module; provide sensible defaults for required fields.
- `EntityTestData` — generic factory/save helper using `DataManager` with validation before save.
- `InsuranceAssertions` — AssertJ entry point with domain-specific fluent assertions for `Partner`, `Policy`, `Quote`, and `Account`.

### Key Cross-Module Flow Tests

| Test Class | What it covers |
|---|---|
| `PolicyCreatedEventTest` | `PolicyService.createPolicy()` → `PolicyCreatedEvent` → `AccountService.createAccount()` |
| `QuoteAcceptanceFlowTest` | Full quote → policy → account creation flow |
| `AccountBalanceWithPolicyTest` | `AccountService.getAccountBalance()` against real persisted Policy |
| `PartnerServiceTest` | Partner number generation, update, search |
| `QuoteServiceTest` | Reject/accept transitions |
| `AccountServiceTest` | Balance calculation |
| `PartnerUiTest`, `PolicyUiTest` | Vaadin UI integration tests |

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

### Test Fixtures as `testFixtures` Artifacts
Each `-core` module publishes a `testFixtures` artifact containing `*DataProvider` classes and generated AssertJ assertions. The `insurance-app` test classpath aggregates all of these to enable cross-module integration tests without duplicating test data setup.
