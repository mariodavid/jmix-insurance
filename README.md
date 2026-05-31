# jmix-insurance

A modular insurance management demo application built with [Jmix 2](https://www.jmix.io/). It demonstrates how to structure a Jmix project as a set of independently publishable domain add-ons — Partners, Quotes, Policies, and Accounts — assembled into a single runnable application.

## Features

- **Partner management** — create and search business partners with auto-generated partner numbers (`PT-NNNNN`)
- **Quote management** — create quotes with premium calculation, accept or reject them
- **Policy management** — policies are created automatically when a quote is accepted; read-only view with live account balance
- **Account management** — accounts and payment documents are generated automatically on policy creation
- **Event-driven flow** — policy creation publishes a Spring event that triggers account setup
- **Modular add-on architecture** — each domain is an independently publishable Gradle sub-build with API/core/UI separation

## Technology Stack

|                   |                                       |
|-------------------|---------------------------------------|
| Language          | Java 17                               |
| Framework         | Jmix 2.8.1 (Spring Boot 3, Vaadin 24) |
| Persistence       | EclipseLink JPA                       |
| Database          | HSQLDB (embedded, file-based)         |
| Schema migrations | Liquibase                             |
| Build             | Gradle (composite build)              |

## Project Structure

```
jmix-insurance/
├── insurance-app/      ← Runnable Spring Boot application
├── common/             ← Shared base entity + test utilities
├── security/           ← User entity and roles
├── partner-core/       ← Partner domain
├── partner-api/        ← PartnerService interface + DTO
├── policy-core/        ← Policy domain
├── policy-api/         ← PolicyService interface + DTO + events
├── quote-core/         ← Quote domain
├── quote-api/          ← QuoteService interface + DTO
├── account-core/       ← Account domain
├── account-api/        ← AccountService interface + DTO
├── product-core/       ← Product configuration
└── product-api/        ← Product enums (InsuranceProduct, ProductType, …)
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for a detailed description of the module layout, domain model, and key design decisions.

## Getting Started

### Prerequisites

- Java 17 or later
- Gradle (or use the included `gradlew` wrapper)

### Run the Application

```bash
./gradlew :insurance-app:bootRun
```

The application starts at `http://localhost:8080`.  
Default credentials: **admin / admin**

The embedded HSQLDB database is stored under `.jmix/hsqldb/` in the project root. The schema is created automatically by Liquibase on first startup.

### Run the Tests

```bash
./gradlew :insurance-app:test
```

Or run all module tests at once:

```bash
./run_all_tests.sh
```

## Usage

1. **Create a Partner** — navigate to *Partners* and add a new record.
2. **Create a Quote** — navigate to *Quotes*, open a new quote, select the partner, choose a product, and calculate the premium.
3. **Accept the Quote** — from the quote list, use the *Accept* action. This creates a Policy and an Account automatically.
4. **View the Policy** — navigate to *Policies* to see the generated policy; use the date picker to inspect the account balance at any point in time.
5. **View the Account** — navigate to *Accounts* to see the generated payment documents.

## Development

The project follows the Jmix composite build pattern. Each domain add-on can be opened and built independently. The `insurance-app` assembles all add-ons at runtime by including their `*-starter` artifacts.

Coding guidelines and skill routing are documented in [CLAUDE.md](CLAUDE.md).
