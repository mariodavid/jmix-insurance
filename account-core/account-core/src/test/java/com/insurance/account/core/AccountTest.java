package com.insurance.account.core;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.test_support.AccountDataProvider;
import com.insurance.common.test_support.AuthenticatedAsAdmin;
import com.insurance.common.test_support.EntityTestData;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
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
}
