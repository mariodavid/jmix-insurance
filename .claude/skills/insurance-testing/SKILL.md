---
name: insurance-testing
description: Project-specific testing guide for jmix-insurance. Covers BaseIntegrationTest, DatabaseCleanup, EntityTestData, TestDataProvider fixtures, generated assertions, UI test helpers, Given/When/Then test names, and cross-module flow tests. Use when writing or reviewing tests in this repo.
---

# Insurance Project Testing Guide

Use the current test harness instead of older sample-test patterns.

## Integration Tests

Webapp service and flow tests extend `BaseIntegrationTest`:

```java
class PartnerServiceTest extends BaseIntegrationTest {
  @Autowired private EntityTestData entityTestData;
  @Autowired private DatabaseCleanup databaseCleanup;

  @BeforeEach
  void setUp() {
    databaseCleanup.removeAllEntities();
  }
}
```

`BaseIntegrationTest` provides `@SpringBootTest`, `@ActiveProfiles("test")`, and authenticated
admin access. Do not repeat those annotations on normal webapp integration tests.

Use `DatabaseCleanup.removeAllEntities()` for business entities (`Partner`, `Quote`, `Policy`,
`Account`, `AccountDocument`). Security user tests should clean up only their own `test-*` users so
the built-in admin user stays intact.

## Test Data

Use `EntityTestData` with module test fixtures:

```java
Partner partner = entityTestData.saveWithDefaults(new PartnerDataProvider());
Quote quote =
    entityTestData.saveWithDefaults(
        new QuoteDataProvider(), q -> q.setPartnerNo(partner.getPartnerNo()));
```

Available providers:

- `PartnerDataProvider`
- `QuoteDataProvider`
- `PolicyDataProvider`
- `AccountDataProvider`
- `UserDataProvider`

There are no `PartnerFactory`, `QuoteFactory`, `PolicyFactory`, or `*Data` records in the current
harness. Do not introduce that older pattern.

## Assertions

Prefer the app aggregate assertion import in webapp tests:

```java
import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;
```

Use module assertions directly when a test lives in the module itself, for example
`com.insurance.security.test_support.Assertions.assertThat`.

## UI Tests

Use `@UiTest` plus `FlowuiTestAssistConfiguration`. Navigate through `ViewInteractions` and prefer
component ids/actions over translated text:

```java
View<?> listView = ViewInteractions.forNavigation(viewNavigators).navigate("security_User.list");
DataGridInteractions.of(listView, User.class, "usersDataGrid").actionPerform("createAction");

View<?> detailView = viewInteractions.findOpenView("security_User.detail");
FormInteractions form = FormInteractions.of(detailView);
form.setTextFieldValue("usernameField", username);
form.setPasswordFieldValue("passwordField", password);
form.click("saveAndCloseButton");
```

Text selectors are acceptable only when the component has no stable id.

## Test Shape

Name methods `given_X_when_Y_then_Z` and separate phases with `// given`, `// when`, `// then`.
Reload persisted entities after a service call before asserting changed state.

## Cross-Module Flow Tests

For Quote -> Policy -> Account behavior, assert the final persisted state across all touched
modules. Existing references:

- `webapp/src/test/java/com/insurance/app/quote/QuoteAcceptanceFlowTest.java`
- `webapp/src/test/java/com/insurance/app/policy/PolicyCreatedEventTest.java`
- `webapp/src/test/java/com/insurance/app/account/AccountBalanceWithPolicyTest.java`

## Validation

Run the smallest relevant command:

```shell
./gradlew :webapp:test --tests "com.insurance.app.<package>.*"
./gradlew :webapp:test --tests "com.insurance.app.arch.ArchitectureTest"
```

## Forbidden

- `new Entity()` for Jmix entities in persistence tests.
- Manual cleanup lists for business entities when `DatabaseCleanup` is available.
- Hardcoded cleanup table names.
- Test-managed `@Transactional` on Jmix integration test classes.
- UI tests that assert only that navigation did not throw.
