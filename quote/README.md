# Quote Module — jmix-insurance

The **Quote Module** manages the lifecycle of insurance quotes. It handles quote calculations, status transitions (Accept / Reject), and orchestrates policy issuance when a quote is accepted.

---

## 1. Module Structure

```
quote/
├── quote-api/          ← Public interfaces, Jmix DTOs, and events (No JPA)
├── quote-api-starter/  ← Spring Boot auto-configuration for the API
├── quote-core/         ← JPA entities, service implementations, and Liquibase
├── quote-core-starter/ ← Spring Boot auto-configuration for the Core
├── quote-ui/           ← Vaadin and Jmix Flow UI view controllers
└── quote-ui-starter/   ← Spring Boot auto-configuration for the UI
```

---

## 2. Database Model

- **Entity**: `Quote` (mapped to table `QUOTE_QUOTE`)
- **Key Attributes**:
  - `quoteNo` (unique business key, formatted as `QT-NNNNN`)
  - `partnerNo` (string value reference to the customer, no direct JPA association)
  - `status` (`QuoteStatus` enum: `DRAFT`, `ACCEPTED`, `REJECTED`)
  - `insuranceProduct` (enum for product variant)
  - `squareMeters` & `calculatedPremium`
  - `effectiveDate`
  - `createdPolicyNo` & `createdPolicyId` (written upon acceptance)

---

## 3. Public API Contract (`quote-api`)

Other modules must interact with this module by importing `quote-api` and utilizing the public `QuoteService` interface and `QuoteDto`.

### 3.1. Public Services
The module exposes the `QuoteService` interface (`com.insurance.quote.api.service.QuoteService`):

```java
public interface QuoteService {
    /** Rejects a draft quote */
    void reject(Id<?> quoteId);

    /** 
     * Accepts a quote, triggers policy creation, and records the 
     * resulting policy reference details.
     */
    QuoteDto accept(Id<?> quoteId);
}
```

---

## 4. Lifecycle Acceptance Flow (Cross-Domain)

When a quote is accepted, the following operations run inside a single database transaction boundary:

```
QuoteService.accept(quoteId)
  │
  ├── 1. Loads the Quote
  ├── 2. Prepares a CreatePolicyRequestDto (from Quote effective date, premium, and partnerNo)
  ├── 3. Invokes PolicyService.createPolicy(request) (cross-domain service call)
  │       │
  │       └── (policy-core creates Policy, saves it, and publishes PolicyCreatedEvent)
  │
  ├── 4. Records the returned Policy ID & Policy Number on the Quote
  └── 5. Sets Quote status to ACCEPTED
```

---

## 5. Development Guidelines & Rules

1. **Jmix ID Handling**: Methods in `QuoteService` take `Id<?>` parameters (Jmix entity references) to load entity records securely.
2. **String Ref Coupling**: We link Quotes to Partners and Policies using **plain string values** (`partnerNo`, `createdPolicyNo`). Do **not** establish direct JPA entity mapping annotations (`@ManyToOne` or `@OneToOne`) to `Partner` or `Policy`.
3. **No Direct Core Dependencies**: Never import classes from `partner-core`, `policy-core`, or `account-core` into this module.
