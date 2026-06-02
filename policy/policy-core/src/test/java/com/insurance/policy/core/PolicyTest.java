package com.insurance.policy.core;

import com.insurance.common.test_support.AuthenticatedAsAdmin;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.policy.core.entity.Policy;
import com.insurance.policy.core.test_support.PolicyDataProvider;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static com.insurance.policy.core.test_support.Assertions.assertThat;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ExtendWith(AuthenticatedAsAdmin.class)
class PolicyTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntityTestData entityTestData;

    private final List<Policy> cleanup = new ArrayList<>();

    @Test
    void contextLoads() {
    }

    @Test
    void given_policy_when_savedAndUpdated_then_commonEntityAuditFieldsAndVersionAreMaintained() {
        Policy saved = entityTestData.saveWithDefaults(new PolicyDataProvider(), policy -> {
            policy.setPremium(new java.math.BigDecimal("300.00"));
        });
        cleanup.add(saved);

        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getLastModifiedDate()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(1);

        Policy reloaded = dataManager.load(Policy.class).id(saved.getId()).one();
        reloaded.setPremium(new java.math.BigDecimal("320.00"));
        Policy updated = dataManager.save(reloaded);

        assertThat(updated.getCreatedDate()).isEqualTo(saved.getCreatedDate());
        assertThat(updated.getLastModifiedDate()).isNotNull();
        assertThat(updated.getVersion()).isGreaterThan(saved.getVersion());
        cleanup.remove(saved);
        cleanup.add(updated);
    }

    @AfterEach
    void tearDown() {
        cleanup.forEach(policy -> {
            dataManager.load(Policy.class)
                    .id(policy.getId())
                    .optional()
                    .ifPresent(dataManager::remove);
        });
        cleanup.clear();
    }
}
