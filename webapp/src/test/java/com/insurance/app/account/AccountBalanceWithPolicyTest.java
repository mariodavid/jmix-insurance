package com.insurance.app.account;

import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.insurance.account.api.service.AccountService;
import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.test_support.PartnerDataProvider;
import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import io.jmix.core.DataManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Testet getAccountBalance() mit einer echten Policy — deckt den coverageEnd-Check ab, der im
 * AccountServiceTest mit Fake-UUIDs übersprungen wird.
 */
class AccountBalanceWithPolicyTest extends BaseIntegrationTest {

  @Autowired private AccountService accountService;

  @Autowired private PolicyService policyService;

  @Autowired private EntityTestData entityTestData;

  @Autowired private DataManager dataManager;

  @Autowired private DatabaseCleanup databaseCleanup;

  private static final LocalDate COVERAGE_START = LocalDate.of(2025, 1, 1);
  private static final BigDecimal PREMIUM = new BigDecimal("120.00");

  private PolicyDto createPolicy(String paymentFrequencyId, BigDecimal premium) {
    Partner partner = entityTestData.saveWithDefaults(new PartnerDataProvider());
    return policyService.createPolicy(
        new CreatePolicyRequestDto(
            "QT-FACTORY",
            partner.getPartnerNo(),
            "HOME_CONTENT_BASIC_2024_01",
            COVERAGE_START,
            premium,
            paymentFrequencyId));
  }

  @BeforeEach
  void setUp() {
    databaseCleanup.removeAllEntities();
  }

  @Test
  void
      given_quarterlyPolicyWithinCoverage_when_balanceQueriedAfterTwoPayments_then_partialSumReturned() {
    // given — QUARTERLY: Raten am 1.1, 1.4, 1.7, 1.10
    PolicyDto policy = createPolicy("QUARTERLY", PREMIUM);

    // when — Stichtag 30.6 → 2 von 4 Raten fällig
    LocalDate effectiveDate = LocalDate.of(2025, 6, 30);
    BigDecimal balance = accountService.getAccountBalance(policy.getPolicyNo(), effectiveDate);

    // then
    BigDecimal expectedPerPayment = PREMIUM.divide(new BigDecimal("4"), RoundingMode.HALF_UP);
    assertThat(balance)
        .isEqualByComparingTo(expectedPerPayment.negate().multiply(new BigDecimal("2")));
  }

  @Test
  void given_policyWithExpiredCoverage_when_balanceQueried_then_illegalArgumentExceptionThrown() {
    // given — coverageEnd = coverageStart + 1 Jahr = 2026-01-01
    PolicyDto policy = createPolicy("YEARLY", PREMIUM);

    // when / then
    LocalDate afterCoverageEnd = policy.getCoverageEnd().plusDays(1);
    assertThatThrownBy(
            () -> accountService.getAccountBalance(policy.getPolicyNo(), afterCoverageEnd))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Coverage end");
  }
}
