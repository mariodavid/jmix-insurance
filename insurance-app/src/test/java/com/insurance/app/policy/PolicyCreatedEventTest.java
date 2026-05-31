package com.insurance.app.policy;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.account.core.entity.DocumentType;
import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.test_support.PartnerDataProvider;
import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;

/**
 * Testet den cross-modul Ablauf: PolicyService.createPolicy() → PolicyCreatedEvent → PolicyCreatedEventListener → Account-Anlage
 */
class PolicyCreatedEventTest extends BaseIntegrationTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private EntityTestData entityTestData;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    private static final LocalDate COVERAGE_START = LocalDate.of(2025, 1, 1);
    private static final BigDecimal PREMIUM = new BigDecimal("240.00");

    private PolicyDto createPolicy(String paymentFrequencyId, BigDecimal premium) {
        Partner partner = entityTestData.saveWithDefaults(new PartnerDataProvider());
        return policyService.createPolicy(new CreatePolicyRequestDto(
                "QT-FACTORY",
                partner.getPartnerNo(),
                "HOME_CONTENT_BASIC_2024_01",
                COVERAGE_START,
                premium,
                paymentFrequencyId
        ));
    }

    @BeforeEach
    void setUp() {
        databaseCleanup.removeAllEntities();
    }

    @Test
    void given_policyWithYearlyFrequency_when_policyCreated_then_accountHasOneDocument() {
        // when
        PolicyDto policy = createPolicy("YEARLY", PREMIUM);

        // then
        Account account = loadAccountByNo(policy.getPolicyNo());
        assertThat(account)
                .hasAccountNo(policy.getPolicyNo())
                .hasBalance(PREMIUM.negate())
                .hasDocumentCount(1);

        List<AccountDocument> docs = loadDocuments(account);
        assertThat(docs.get(0))
                .hasType(DocumentType.CREDIT)
                .hasDocumentDate(COVERAGE_START);
    }

    @Test
    void given_policyWithMonthlyFrequency_when_policyCreated_then_accountHasTwelveDocuments() {
        // when
        PolicyDto policy = createPolicy("MONTHLY", PREMIUM);

        // then
        Account account = loadAccountByNo(policy.getPolicyNo());
        assertThat(account).hasDocumentCount(12);

        List<AccountDocument> docs = loadDocuments(account);
        assertThat(docs.get(0)).hasDocumentDate(COVERAGE_START);
        assertThat(docs.get(11)).hasDocumentDate(COVERAGE_START.plusMonths(11));
    }

    @Test
    void given_policyWithQuarterlyFrequency_when_policyCreated_then_accountHasFourDocuments() {
        // when
        PolicyDto policy = createPolicy("QUARTERLY", PREMIUM);

        // then
        Account account = loadAccountByNo(policy.getPolicyNo());
        assertThat(account).hasDocumentCount(4);

        List<AccountDocument> docs = loadDocuments(account);
        assertThat(docs.get(0)).hasDocumentDate(COVERAGE_START);
        assertThat(docs.get(3)).hasDocumentDate(COVERAGE_START.plusMonths(9));
    }

    private Account loadAccountByNo(String accountNo) {
        return dataManager.load(Account.class)
                .condition(PropertyCondition.equal("accountNo", accountNo))
                .fetchPlan(fp -> fp.addFetchPlan(io.jmix.core.FetchPlan.BASE).add("documents", io.jmix.core.FetchPlan.BASE))
                .one();
    }

    private List<AccountDocument> loadDocuments(Account account) {
        return dataManager.load(AccountDocument.class)
                .query("select d from account_AccountDocument d where d.account = :account order by d.documentDate")
                .parameter("account", account)
                .list();
    }
}
