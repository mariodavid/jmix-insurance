---
name: insurance-testing
description: Project-specific testing guide for jmix-insurance. Covers the established patterns for integration tests in webapp: BaseIntegrationTest, DatabaseCleanup, *Factory + *Data test data setup, InsuranceAssertions custom assertions, Given/When/Then structure, and cross-module flow tests. Use this skill when writing or reviewing tests in this project.
---

# Insurance Project Testing Guide

This guide documents how tests are written in the `jmix-insurance` project.
All integration tests live in `webapp/src/test`.

## Base Class

Every integration test extends `BaseIntegrationTest` instead of repeating three annotations:

```java
// CORRECT
class PartnerServiceTest extends BaseIntegrationTest { ... }

// WRONG — do not repeat these on every class
@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
class PartnerServiceTest { ... }
```

`BaseIntegrationTest` is defined in `test_support/BaseIntegrationTest.java`:

```java
@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest { }
```

Authentication is provided by `AuthenticatedAsAdmin` — it calls
`SystemAuthenticator.begin("admin")` before each test and `end()` after.
Never call `SystemAuthenticator` manually in tests that extend `BaseIntegrationTest`.

## Database Cleanup

Use `DatabaseCleanup.removeAllEntities()` in `@BeforeEach`, not a manual `List<Object> cleanup`
in `@AfterEach`. The `@BeforeEach` approach gives each test a clean slate regardless of what
a previous test left behind.

```java
@Autowired
private DatabaseCleanup databaseCleanup;

@BeforeEach
void setUp() {
    databaseCleanup.removeAllEntities();
}
```

`DatabaseCleanup` is in `test_support/DatabaseCleanup.java`. It uses `JdbcTemplate` (not
`DataManager`) to delete all rows in the correct FK order:
`AccountDocument → Account → Policy → Quote → Partner`.

**Why JdbcTemplate and not DataManager?**
- Bypasses Jmix soft-delete — rows are physically removed
- Faster than loading and removing entities one by one
- Table names are resolved via `MetadataTools.getDatabaseTable()`, so they stay in sync with entity mappings

Do not add `@AfterEach` cleanup lists when `DatabaseCleanup` is used. The two approaches
must not be mixed.

## Test Data: *Data Records + *Factory Beans

Every entity domain has a pair:

| Class | Role |
|---|---|
| `PartnerData` | Immutable record with a fluent `Builder`; holds field values only |
| `PartnerFactory` | Spring `@Component`; creates/saves entities via `DataManager` |

Available factories in `test_support/`:
- `PartnerFactory` — saves `Partner` via `DataManager.create() + save()`
- `QuoteFactory` — saves `Quote` with all required fields
- `PolicyFactory` — calls `PolicyService.createPolicy()` (also triggers `PolicyCreatedEvent` → Account)

### Usage pattern

```java
@Autowired
private PartnerFactory partnerFactory;

// Save with all defaults
Partner partner = partnerFactory.saveDefault();

// Save with one field overridden
Partner partner = partnerFactory.save(
        partnerFactory.defaultData()
                .lastName("Müller")
                .build()
);
```

```java
@Autowired
private PolicyFactory policyFactory;

// PolicyFactory delegates to PolicyService — this also creates an Account via the event
PolicyDto policy = policyFactory.createDefault();

PolicyDto policy = policyFactory.create(
        policyFactory.defaultData()
                .paymentFrequencyId("MONTHLY")
                .premium(new BigDecimal("120.00"))
                .build()
);
```

**Never instantiate entities with `new`** in tests that touch the database.
Always use `DataManager.create()` (or a factory that does so).

## Custom Assertions: InsuranceAssertions

Use `import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat`
as the single static import. This covers both standard AssertJ and all domain assertions.

```java
import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;
```

Available domain assertions:

```java
// PartnerAssert
assertThat(partner)
        .hasPartnerNoMatchingPattern()   // matches PT-\d{5}
        .hasFirstName("Anna")
        .hasLastName("Schmidt");

// PolicyAssert
assertThat(policy)
        .hasPolicyNoMatchingFormat()             // matches HC-YYYY-NNNNNN
        .hasCoverageStart(LocalDate.of(2025,1,1))
        .hasCoverageEndOneYearAfter(coverageStart)
        .hasPremium(new BigDecimal("240.00"));

// QuoteAssert
assertThat(quote).isAccepted();    // status == ACCEPTED && acceptedAt != null
assertThat(quote).isRejected();    // status == REJECTED && rejectedAt != null
assertThat(quote).hasPolicyReference();  // createdPolicyNo + createdPolicyId != null

// AccountAssert
assertThat(account)
        .hasAccountNo("HC-2025-000001")
        .hasBalance(new BigDecimal("-240.00"))
        .hasDocumentCount(1);
```

Standard AssertJ methods like `assertThat(list).hasSize(3)` work through the same import.

## Test Structure: Given / When / Then

Method names follow `given_X_when_Y_then_Z`. The body uses `// given`, `// when`, `// then`
comments to separate the three phases. Use `// and` for additional steps within a phase.

```java
@Test
void given_newPartnerWithoutPartnerNo_when_saved_then_partnerNoIsGenerated() {
    // given
    PartnerDto dto = dataManager.create(PartnerDto.class);
    dto.setFirstName("Anna");
    dto.setLastName("Schmidt");

    // when
    partnerService.savePartner(dto);

    // then
    Partner saved = loadPartnerByLastName("Schmidt");
    assertThat(saved)
            .hasPartnerNoMatchingPattern()
            .hasFirstName("Anna");
}
```

Avoid bundling given+when+then without comments, and avoid `_test` or `should_` prefixes.

## Cross-Module Flow Tests

When a business operation spans multiple modules (e.g. Quote → Policy → Account),
write a dedicated flow test that asserts the **end result across all modules**, not just
the return value of the entry-point service.

The module dependency chain in this project:

```
QuoteService.accept()
  └─► PolicyService.createPolicy()
        └─► ApplicationEventPublisher → PolicyCreatedEvent
              └─► PolicyCreatedEventListener
                    └─► AccountService.createAccount()
                          └─► Account + AccountDocuments saved
```

Example — asserting the full chain after `accept()`:

```java
@Test
void given_acceptedQuote_when_accountLoaded_then_balanceEqualsNegativePremium() {
    // given
    Quote quote = quoteFactory.save(quoteFactory.defaultData()
            .paymentFrequency(PaymentFrequency.YEARLY)
            .calculatedPremium(new BigDecimal("480.00"))
            .build());

    // when
    quoteService.accept(Id.of(quote));

    // then — assert across modules
    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    Account account = loadAccountByNo(reloaded.getCreatedPolicyNo());

    assertThat(account)
            .hasAccountNo(reloaded.getCreatedPolicyNo())
            .hasBalance(new BigDecimal("-480.00"));
}
```

Existing flow tests to use as reference:
- `policy/PolicyCreatedEventTest.java` — Policy → Account via event (3 payment frequencies)
- `quote/QuoteAcceptanceFlowTest.java` — Quote → Policy → Account full chain
- `account/AccountBalanceWithPolicyTest.java` — balance calculation with real Policy (tests the `coverageEnd` guard)

## Loading Entities for Assertions

After a service call, reload the entity fresh from the DB before asserting.
Do not assert on the object returned by `dataManager.save()` if the entity was
modified inside the service (version will mismatch on later removal).

```java
// CORRECT — reload after service call
quoteService.reject(Id.of(quote));
Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
assertThat(reloaded).isRejected();

// WRONG — stale object, will cause OptimisticLockException on cleanup
quoteService.reject(Id.of(quote));
assertThat(quote).isRejected();  // quote still has old version
```

When a service modifies a composition (e.g. Account + AccountDocuments), load with an
explicit fetch plan:

```java
Account account = dataManager.load(Account.class)
        .condition(PropertyCondition.equal("accountNo", policyNo))
        .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("documents", FetchPlan.BASE))
        .one();
assertThat(account).hasDocumentCount(4);
```

## Forbidden

- `new Entity()` for any class that extends `CommonEntity` in persistence tests
- Manual `List<Object> cleanup` in `@AfterEach` — use `DatabaseCleanup` instead
- Hardcoded table names in cleanup SQL — use `MetadataTools.getDatabaseTable()`
- `@Transactional` on test classes — Jmix entity lifecycle does not work correctly with test-managed transactions
- Asserting only on the service return value in cross-module tests — always verify the downstream side effect (Account created, Policy saved, etc.)
- `@WithUserDetails` — use `AuthenticatedAsAdmin` or `SystemAuthenticator` instead

## Run Tests

```bash
# Single class
./gradlew :webapp:test --tests "com.insurance.app.partner.PartnerServiceTest"

# Full suite
./gradlew :webapp:test
```
