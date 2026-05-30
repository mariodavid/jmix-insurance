package com.insurance.app.account;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.account.core.entity.DocumentType;
import com.insurance.account.core.service.AccountServiceCore;
import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;

class AccountServiceTest extends BaseIntegrationTest {

    @Autowired
    private AccountServiceCore accountService;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    private static final LocalDate COVERAGE_START = LocalDate.of(2025, 1, 1);
    private static final BigDecimal PREMIUM = new BigDecimal("120.00");

    // Gültige UUIDs — PolicyService.findPolicyById() gibt null zurück → coverageEnd-Check wird übersprungen
    private static final String POLICY_ID_YEARLY    = "00000000-0000-0000-0000-000000000001";
    private static final String POLICY_ID_MONTHLY   = "00000000-0000-0000-0000-000000000002";
    private static final String POLICY_ID_QUARTERLY = "00000000-0000-0000-0000-000000000003";
    private static final String POLICY_ID_BALANCE   = "00000000-0000-0000-0000-000000000004";

    @BeforeEach
    void setUp() {
        databaseCleanup.removeAllEntities();
    }

    @Test
    void given_yearlyFrequency_when_accountCreated_then_oneDocumentWithFullPremium() {
        // when
        accountService.createAccount(POLICY_ID_YEARLY, "HC-2025-000001", COVERAGE_START, PREMIUM, PaymentFrequency.YEARLY);

        // then
        Account account = loadWithDocuments("HC-2025-000001");
        assertThat(account)
                .hasBalance(PREMIUM.negate())
                .hasDocumentCount(1);
        assertThat(account.getDocuments().get(0).getType()).isEqualTo(DocumentType.CREDIT);
        assertThat(account.getDocuments().get(0).getDocumentDate()).isEqualTo(COVERAGE_START);
    }

    @Test
    void given_monthlyFrequency_when_accountCreated_then_twelveDocumentsSpreadOverYear() {
        // when
        accountService.createAccount(POLICY_ID_MONTHLY, "HC-2025-000002", COVERAGE_START, PREMIUM, PaymentFrequency.MONTHLY);

        // then
        Account account = loadWithDocuments("HC-2025-000002");
        assertThat(account).hasDocumentCount(12);
        assertThat(account.getDocuments().get(0).getDocumentDate()).isEqualTo(COVERAGE_START);
        assertThat(account.getDocuments().get(11).getDocumentDate()).isEqualTo(COVERAGE_START.plusMonths(11));
    }

    @Test
    void given_quarterlyFrequency_when_accountCreated_then_fourDocumentsSpreadOverYear() {
        // when
        accountService.createAccount(POLICY_ID_QUARTERLY, "HC-2025-000003", COVERAGE_START, PREMIUM, PaymentFrequency.QUARTERLY);

        // then
        Account account = loadWithDocuments("HC-2025-000003");
        assertThat(account).hasDocumentCount(4);
        assertThat(account.getDocuments().get(0).getDocumentDate()).isEqualTo(COVERAGE_START);
        assertThat(account.getDocuments().get(3).getDocumentDate()).isEqualTo(COVERAGE_START.plusMonths(9));
    }

    @Test
    void given_quarterlyAccount_when_balanceQueriedBeforeThirdPayment_then_partialSumReturned() {
        // given
        accountService.createAccount(POLICY_ID_BALANCE, "HC-2025-000004", COVERAGE_START, PREMIUM, PaymentFrequency.QUARTERLY);

        // when — Stichtag zwischen 2. Rate (1.4) und 3. Rate (1.7)
        LocalDate effectiveDate = LocalDate.of(2025, 6, 30);
        BigDecimal balance = accountService.getAccountBalance("HC-2025-000004", effectiveDate);

        // then — 2 Raten à 30.00 = -60.00
        BigDecimal expectedPerPayment = PREMIUM.divide(new BigDecimal("4"), java.math.RoundingMode.HALF_UP);
        assertThat(balance).isEqualByComparingTo(expectedPerPayment.negate().multiply(new BigDecimal("2")));
    }

    private Account loadWithDocuments(String accountNo) {
        return dataManager.load(Account.class)
                .condition(PropertyCondition.equal("accountNo", accountNo))
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("documents", FetchPlan.BASE))
                .one();
    }
}
