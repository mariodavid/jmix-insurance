package com.insurance.app.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.service.AccountServiceCore;
import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.test_support.PartnerDataProvider;
import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.policy.core.entity.Policy;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.DataManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PolicyRollbackTest extends BaseIntegrationTest {

  private static final LocalDate COVERAGE_START = LocalDate.of(2025, 1, 1);
  private static final BigDecimal PREMIUM = new BigDecimal("240.00");

  @Autowired private PolicyService policyService;

  @Autowired private EntityTestData entityTestData;

  @Autowired private DataManager dataManager;

  @Autowired private DatabaseCleanup databaseCleanup;

  @MockitoBean private AccountServiceCore accountService;

  @BeforeEach
  void setUp() {
    databaseCleanup.removeAllEntities();
  }

  @Test
  void given_accountCreationFails_when_policyCreated_then_policyAndAccountRollback() {
    // given
    Partner partner = entityTestData.saveWithDefaults(new PartnerDataProvider());
    CreatePolicyRequestDto request =
        new CreatePolicyRequestDto(
            "QT-ROLLBACK",
            partner.getPartnerNo(),
            "HOME_CONTENT_BASIC_2024_01",
            COVERAGE_START,
            PREMIUM,
            PaymentFrequency.YEARLY.getId());
    doThrow(new IllegalStateException("Account creation failed"))
        .when(accountService)
        .createAccount(
            any(java.util.UUID.class),
            anyString(),
            any(LocalDate.class),
            any(BigDecimal.class),
            any(PaymentFrequency.class));

    // when / then
    org.assertj.core.api.Assertions.assertThatThrownBy(() -> policyService.createPolicy(request))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Account creation failed");

    // then — policy is rolled back synchronously, so it should not exist in the database
    assertThat(dataManager.load(Policy.class).all().list()).isEmpty();
    verify(accountService)
        .createAccount(
            any(java.util.UUID.class),
            anyString(),
            any(LocalDate.class),
            any(BigDecimal.class),
            any(PaymentFrequency.class));
    assertThat(dataManager.load(Account.class).all().list()).isEmpty();
  }
}
