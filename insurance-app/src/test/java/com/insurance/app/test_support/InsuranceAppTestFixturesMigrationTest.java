package com.insurance.app.test_support;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.test_support.AccountDataProvider;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.test_support.PartnerDataProvider;
import com.insurance.policy.core.entity.Policy;
import com.insurance.policy.core.test_support.PolicyDataProvider;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.core.entity.Quote;
import com.insurance.quote.core.test_support.QuoteDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

class InsuranceAppTestFixturesMigrationTest extends BaseIntegrationTest {

    @Autowired
    private EntityTestData entityTestData;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    @BeforeEach
    void setUp() {
        databaseCleanup.removeAllEntities();
    }

    @Test
    void insuranceAppCanUseCoreModuleTestFixturesAndGeneratedAssertions() {
        Partner partner = entityTestData.saveWithDefaults(new PartnerDataProvider(), p ->
                p.setPartnerNo("PT-" + UUID.randomUUID().toString().substring(0, 8)));

        Policy policy = entityTestData.saveWithDefaults(new PolicyDataProvider(), p -> {
            p.setPartnerNo(partner.getPartnerNo());
            p.setPolicyNo("POL-" + UUID.randomUUID().toString().substring(0, 8));
        });

        Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider(), q ->
                q.setPartnerNo(partner.getPartnerNo()));

        Account account = entityTestData.saveWithDefaults(new AccountDataProvider(), a -> {
            a.setPolicyId(policy.getId().toString());
            a.setAccountNo(policy.getPolicyNo());
            a.setAccountBalance(new BigDecimal("0.00"));
        });

        com.insurance.partner.core.test_support.Assertions.assertThat(partner)
                .hasPartnerNo(partner.getPartnerNo());
        com.insurance.policy.core.test_support.Assertions.assertThat(policy)
                .hasPartnerNo(partner.getPartnerNo());
        com.insurance.quote.core.test_support.Assertions.assertThat(quote)
                .hasPartnerNo(partner.getPartnerNo())
                .hasStatus(QuoteStatus.PENDING);
        com.insurance.account.core.test_support.Assertions.assertThat(account)
                .hasAccountNo(policy.getPolicyNo());
        org.assertj.core.api.Assertions.assertThat(account.getAccountBalance())
                .isEqualByComparingTo("0.00");
    }
}
