package com.insurance.policy.core;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.policy.core.entity.Policy;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class PolicyServiceTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2025, 1, 1);

    @BeforeEach
    void setUp() {
        systemAuthenticator.begin("admin");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String table = metadataTools.getDatabaseTable(metadata.getClass(Policy.class));
        jdbc.update("DELETE FROM " + table);
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    void given_validPolicyRequest_when_policyCreated_then_policyNoAndCoverageAreCorrect() {
        // given
        CreatePolicyRequestDto request = new CreatePolicyRequestDto(
                "QT-00001",
                "PT-TEST",
                "HOME_CONTENT_BASIC_2024_01",
                EFFECTIVE_DATE,
                new BigDecimal("240.00"),
                "YEARLY"
        );

        // when
        PolicyDto result = policyService.createPolicy(request);

        // then
        assertThat(result).isNotNull();
        Policy policy = loadPolicyByNo(result.getPolicyNo());
        assertThat(policy).isNotNull();
        assertThat(policy.getPolicyNo())
                .as("policyNo should match HC-YYYY-NNNNNN format")
                .matches("HC-\\d{4}-\\d{6}");
        assertThat(policy.getCoverageStart())
                .as("coverageStart")
                .isEqualTo(EFFECTIVE_DATE);
        assertThat(policy.getCoverageEnd())
                .as("coverageEnd should be one year after coverageStart")
                .isEqualTo(EFFECTIVE_DATE.plusYears(1));
        assertThat(policy.getPremium())
                .as("premium")
                .isEqualByComparingTo(new BigDecimal("240.00"));
    }

    @Test
    void given_unknownProductId_when_policyCreated_then_illegalArgumentExceptionThrown() {
        // given
        CreatePolicyRequestDto request = new CreatePolicyRequestDto(
                "QT-00002", "PT-00001", "UNKNOWN_PRODUCT",
                EFFECTIVE_DATE, new BigDecimal("100.00"), "YEARLY"
        );

        // when / then
        assertThatThrownBy(() -> policyService.createPolicy(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNKNOWN_PRODUCT");
    }

    @Test
    void given_existingPolicy_when_loadedById_then_correctDtoReturned() {
        // given
        CreatePolicyRequestDto request = new CreatePolicyRequestDto(
                "QT-00001",
                "PT-TEST",
                "HOME_CONTENT_BASIC_2024_01",
                EFFECTIVE_DATE,
                new BigDecimal("240.00"),
                "YEARLY"
        );
        PolicyDto created = policyService.createPolicy(request);

        // when
        PolicyDto found = policyService.findPolicyById(created.getId().toString());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getPolicyNo()).isEqualTo(created.getPolicyNo());
    }

    private Policy loadPolicyByNo(String policyNo) {
        return dataManager.load(Policy.class)
                .condition(PropertyCondition.equal("policyNo", policyNo))
                .one();
    }
}
