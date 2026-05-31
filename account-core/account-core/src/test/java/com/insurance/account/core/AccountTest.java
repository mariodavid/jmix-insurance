package com.insurance.account.core;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.account.core.entity.DocumentType;
import com.insurance.account.core.service.AccountServiceCore;
import com.insurance.account.core.test_support.AccountDataProvider;
import com.insurance.common.test_support.AuthenticatedAsAdmin;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.insurance.account.core.test_support.Assertions.assertThat;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ExtendWith(AuthenticatedAsAdmin.class)
class AccountTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntityTestData entityTestData;

    @Autowired
    private AccountServiceCore accountService;

    private static final LocalDate COVERAGE_START = LocalDate.of(2025, 1, 1);
    private static final BigDecimal PREMIUM = new BigDecimal("120.00");
    private static final String POLICY_ID_YEARLY = "00000000-0000-0000-0000-000000000001";
    private static final String POLICY_ID_QUARTERLY = "00000000-0000-0000-0000-000000000003";

    private final List<Account> cleanup = new ArrayList<>();

    @Test
    void contextLoads() {
    }

    @Test
    void given_account_when_savedAndUpdated_then_commonEntityAuditFieldsAndVersionAreMaintained() {
        Account saved = entityTestData.saveWithDefaults(new AccountDataProvider(), account -> {
            account.setAccountBalance(new BigDecimal("100.00"));
        });
        cleanup.add(saved);

        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getLastModifiedDate()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(1);

        Account reloaded = dataManager.load(Account.class).id(saved.getId()).one();
        reloaded.setAccountBalance(new BigDecimal("120.00"));
        Account updated = dataManager.save(reloaded);

        assertThat(updated.getCreatedDate()).isEqualTo(saved.getCreatedDate());
        assertThat(updated.getLastModifiedDate()).isNotNull();
        assertThat(updated.getVersion()).isGreaterThan(saved.getVersion());
        cleanup.remove(saved);
        cleanup.add(updated);
    }

    @Test
    void given_yearlyFrequency_when_accountCreated_then_oneDocumentWithFullPremium() {
        // when
        Account saved = accountService.createAccount(
                POLICY_ID_YEARLY, "HC-2025-000001", COVERAGE_START, PREMIUM, PaymentFrequency.YEARLY);
        cleanup.add(saved);

        // then
        Account account = loadWithDocuments("HC-2025-000001");
        assertThat(account)
                .hasBalance(PREMIUM.negate())
                .hasDocumentCount(1);

        AccountDocument document = account.getDocuments().get(0);
        assertThat(document)
                .hasType(DocumentType.CREDIT)
                .hasAmount(PREMIUM.negate())
                .hasDocumentDate(COVERAGE_START);
    }

    @Test
    void given_quarterlyFrequency_when_accountCreated_then_fourDocumentsSpreadOverYear() {
        // when
        Account saved = accountService.createAccount(
                POLICY_ID_QUARTERLY, "HC-2025-000003", COVERAGE_START, PREMIUM, PaymentFrequency.QUARTERLY);
        cleanup.add(saved);

        // then
        Account account = loadWithDocuments("HC-2025-000003");
        assertThat(account)
                .hasBalance(PREMIUM.negate())
                .hasDocumentCount(4);

        BigDecimal expectedAmount = new BigDecimal("-30.00");
        for (int i = 0; i < account.getDocuments().size(); i++) {
            assertThat(account.getDocuments().get(i))
                    .hasType(DocumentType.CREDIT)
                    .hasAmount(expectedAmount)
                    .hasDocumentDate(COVERAGE_START.plusMonths((long) i * 3));
        }
    }

    @AfterEach
    void tearDown() {
        cleanup.forEach(account -> {
            dataManager.load(Account.class)
                    .id(account.getId())
                    .optional()
                    .ifPresent(dataManager::remove);
        });
        cleanup.clear();
    }

    private Account loadWithDocuments(String accountNo) {
        return dataManager.load(Account.class)
                .condition(PropertyCondition.equal("accountNo", accountNo))
                .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("documents", FetchPlan.BASE))
                .one();
    }
}
