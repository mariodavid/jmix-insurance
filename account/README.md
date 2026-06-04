# Account Module — jmix-insurance

The **Account Module** handles customer billing accounts and ledger document registers. It tracks account balances, generates debit documents based on payment frequencies, and calculates historical balances for specific dates.

---

## 1. Module Structure

```
account/
├── account-api/          ← Public interfaces, DTOs, and value types (No JPA)
├── account-api-starter/  ← Spring Boot auto-configuration for the API
├── account-core/         ← JPA entities, service implementations, event listeners
├── account-core-starter/ ← Spring Boot auto-configuration for the Core
├── account-ui/           ← Vaadin and Jmix Flow UI view controllers
└── account-ui-starter/   ← Spring Boot auto-configuration for the UI
```

---

## 2. Database Model

### 2.1. Account Entity
- **Entity**: `Account` (mapped to table `ACCOUNT_ACCOUNT`)
- **Key Attributes**:
  - `accountNo` (the account business key)
  - `policyId` (references the parent `Policy` technical UUID as a `String`)
  - **Composition Relation**: Contains a Jmix parent-child composition list of `AccountDocument` entities.

### 2.2. AccountDocument Entity
- **Entity**: `AccountDocument` (mapped to table `ACCOUNT_ACCOUNT_DOCUMENT`)
- **Key Attributes**:
  - `type` (`DocumentType` enum: `DEBIT` or `CREDIT`)
  - `amount` & `documentDate`
  - `description`

---

## 3. Public API Contract (`account-api`)

Other modules must interact with this module by importing `account-api` and utilizing the public `AccountService` interface.

### 3.1. Public Services
The module exposes the `AccountService` interface (`com.insurance.account.api.service.AccountService`):

```java
public interface AccountService {
    /** 
     * Computes the current balance of the account associated with the given 
     * policy number at a specific effective date.
     * 
     * Only documents with a documentDate <= effectiveDate are included.
     * Throws IllegalArgumentException if the date exceeds policy coverage boundaries.
     */
    BigDecimal getAccountBalance(String policyNo, LocalDate effectiveDate);
}
```

---

## 4. Account Lifecycle Generation

This module creates accounts in response to synchronous **PolicyCreatedEvents** emitted by the Policy Module:

```
PolicyCreatedEvent (published by policy-core)
  │
  └── PolicyCreatedEventListener.onPolicyCreated(event) (inside account-core)
        │
        ├── 1. Generates an Account record mapped to the Policy technical UUID
        ├── 2. Inspects policy premium and payment frequency (e.g. MONTHLY, QUARTERLY)
        └── 3. Generates consecutive AccountDocuments (DEBIT records) 
               distributed throughout the 1-year coverage period
```

---

## 5. Development Guidelines & Rules

1. **Date Bound Verification**: In `AccountService.getAccountBalance()`, the implementation must load the policy information to check that `effectiveDate` is not after the policy's `coverageEnd` date.
2. **Composition Cascade**: Since `AccountDocument` is a child composition of `Account`, saving the parent `Account` using `DataManager` will automatically persist or update all modified/added documents.
3. **No Direct Core Dependencies**: Never import classes from `quote-core`, `partner-core`, or `policy-core` into this module.
