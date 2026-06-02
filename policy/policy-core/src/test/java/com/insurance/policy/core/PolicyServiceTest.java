package com.insurance.policy.core;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.policy.core.entity.Policy;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.querycondition.PropertyCondition;
import com.insurance.common.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.insurance.policy.core.test_support.Assertions.assertThat;
import static com.insurance.policy.core.test_support.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ExtendWith(AuthenticatedAsAdmin.class)
class PolicyServiceTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    @Autowired
    private PolicyTestConfiguration.FailingPolicyCreatedEventOrchestrator failingPolicyCreatedEventOrchestrator;

    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2025, 1, 1);

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String table = metadataTools.getDatabaseTable(metadata.getClass(Policy.class));
        jdbc.update("DELETE FROM " + table);
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
        assertThat(result.getId()).isNotNull();
        assertThat(result.getPolicyNo())
                .as("DTO policyNo should match HC-YYYY-NNNNNN format")
                .matches("HC-\\d{4}-\\d{6}");
        assertThat(result.getCoverageStart()).isEqualTo(EFFECTIVE_DATE);
        assertThat(result.getCoverageEnd()).isEqualTo(EFFECTIVE_DATE.plusYears(1));
        assertThat(result.getPremium()).isEqualByComparingTo(new BigDecimal("240.00"));
        assertThat(result.getPaymentFrequency()).isEqualTo("YEARLY");

        Policy policy = loadPolicyByNo(result.getPolicyNo());
        assertThat(policy).isNotNull();
        assertThat(policy.getId()).isEqualTo(result.getId());
        assertThat(policy.getPolicyNo())
                .as("policyNo should match HC-YYYY-NNNNNN format")
                .matches("HC-\\d{4}-\\d{6}");
        assertThat(policy.getPartnerNo()).isEqualTo("PT-TEST");
        assertThat(policy.getInsuranceProduct().getId()).isEqualTo("HOME_CONTENT_BASIC_2024_01");
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
        assertThat(loadPolicies()).isEmpty();
    }

    @Test
    void given_unknownPaymentFrequencyId_when_policyCreated_then_illegalArgumentExceptionThrownAndNoPolicySaved() {
        // given
        CreatePolicyRequestDto request = new CreatePolicyRequestDto(
                "QT-00003",
                "PT-00001",
                "HOME_CONTENT_BASIC_2024_01",
                EFFECTIVE_DATE,
                new BigDecimal("100.00"),
                "WEEKLY"
        );

        // when / then
        assertThatThrownBy(() -> policyService.createPolicy(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("WEEKLY");
        assertThat(loadPolicies()).isEmpty();
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
        PolicyDto found = policyService.findPolicyById(created.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getPolicyNo()).isEqualTo(created.getPolicyNo());
        assertThat(found.getPartnerNo()).isEqualTo(created.getPartnerNo());
        assertThat(found.getInsuranceProduct()).isEqualTo(created.getInsuranceProduct());
        assertThat(found.getCoverageStart()).isEqualTo(created.getCoverageStart());
        assertThat(found.getCoverageEnd()).isEqualTo(created.getCoverageEnd());
        assertThat(found.getPremium()).isEqualByComparingTo(created.getPremium());
        assertThat(found.getPaymentFrequency()).isEqualTo(created.getPaymentFrequency());

        Policy persisted = dataManager.load(Policy.class).id(created.getId()).one();
        assertThat(found.getPolicyNo()).isEqualTo(persisted.getPolicyNo());
        assertThat(found.getPartnerNo()).isEqualTo(persisted.getPartnerNo());
        assertThat(found.getInsuranceProduct()).isEqualTo(persisted.getInsuranceProduct().getId());
        assertThat(found.getCoverageStart()).isEqualTo(persisted.getCoverageStart());
        assertThat(found.getCoverageEnd()).isEqualTo(persisted.getCoverageEnd());
        assertThat(found.getPremium()).isEqualByComparingTo(persisted.getPremium());
        assertThat(found.getPaymentFrequency()).isEqualTo(persisted.getPaymentFrequency().getId());
    }

    @Test
    void given_unknownPolicyUuid_when_loadedById_then_nullReturned() {
        assertThat(policyService.findPolicyById(UUID.randomUUID())).isNull();
    }

    @Test
    void given_accountOrchestratorFails_when_policyCreated_then_exceptionPropagatesAndPolicyRollsBack() {
        // given
        CreatePolicyRequestDto request = new CreatePolicyRequestDto(
                "QT-00004",
                "PT-00001",
                "HOME_CONTENT_BASIC_2024_01",
                EFFECTIVE_DATE,
                new BigDecimal("240.00"),
                "YEARLY"
        );
        failingPolicyCreatedEventOrchestrator.failNextPolicyCreatedEvent();

        // when / then
        assertThatThrownBy(() -> policyService.createPolicy(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Account orchestration failed");
        assertThat(loadPolicies()).isEmpty();
    }

    private Policy loadPolicyByNo(String policyNo) {
        return dataManager.load(Policy.class)
                .condition(PropertyCondition.equal("policyNo", policyNo))
                .one();
    }

    private List<Policy> loadPolicies() {
        return dataManager.load(Policy.class).all().list();
    }
}
