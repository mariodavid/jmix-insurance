package com.insurance.policy.api;

import static com.insurance.policy.api.dto.Assertions.assertThat;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.event.PolicyCreatedEvent;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PolicyTest {

  @Autowired private DataManager dataManager;

  @Autowired private Metadata metadata;

  @Test
  void contextLoads() {}

  @Test
  void given_createPolicyRequestDto_when_created_then_payloadValuesRemainReadable() {
    CreatePolicyRequestDto request =
        new CreatePolicyRequestDto(
            "QT-23001",
            "PT-23001",
            "HOME_CONTENT_BASIC_2024_01",
            LocalDate.of(2025, 1, 1),
            new BigDecimal("240.00"),
            "YEARLY");

    assertThat(request.quoteNo()).isEqualTo("QT-23001");
    assertThat(request.partnerNo()).isEqualTo("PT-23001");
    assertThat(request.insuranceProductId()).isEqualTo("HOME_CONTENT_BASIC_2024_01");
    assertThat(request.effectiveDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    assertThat(request.premium()).isEqualByComparingTo("240.00");
    assertThat(request.paymentFrequencyId()).isEqualTo("YEARLY");
  }

  @Test
  void given_policyDto_when_createdThroughDataManager_then_metadataFieldsAreSettableAndReadable() {
    PolicyDto dto = dataManager.create(PolicyDto.class);
    dto.setPolicyNo("HC-2025-000001");
    dto.setPartnerNo("PT-24001");
    dto.setInsuranceProduct("HOME_CONTENT_BASIC_2024_01");
    dto.setCoverageStart(LocalDate.of(2025, 1, 1));
    dto.setCoverageEnd(LocalDate.of(2026, 1, 1));
    dto.setPremium(new BigDecimal("240.00"));
    dto.setPaymentFrequency("YEARLY");

    MetaClass metaClass = metadata.getClass(PolicyDto.class);

    assertThat(dto.getId()).isNotNull();
    assertThat(metaClass.getProperty("id")).isNotNull();
    assertThat(dto).hasPolicyNo("HC-2025-000001");
    assertThat(dto).hasCoverageStart(LocalDate.of(2025, 1, 1));
    assertThat(dto).hasCoverageEnd(LocalDate.of(2026, 1, 1));
    assertThat(dto).hasPremium(new BigDecimal("240.00"));
    assertThat(dto.getPaymentFrequency()).isEqualTo("YEARLY");
    assertThat(dto.instanceName()).isEqualTo("HC-2025-000001");
  }

  @Test
  void given_policyCreatedEvent_when_created_then_payloadContainsAccountFlowData() {
    UUID policyId = UUID.randomUUID();
    PolicyCreatedEvent event =
        new PolicyCreatedEvent(
            this,
            policyId,
            "HC-2025-000001",
            LocalDate.of(2025, 1, 1),
            new BigDecimal("240.00"),
            "YEARLY");

    assertThat(event.getPolicyId()).isEqualTo(policyId);
    assertThat(event.getPolicyNo()).isEqualTo("HC-2025-000001");
    assertThat(event.getCoverageStart()).isEqualTo(LocalDate.of(2025, 1, 1));
    assertThat(event.getPremium()).isEqualByComparingTo("240.00");
    assertThat(event.getPaymentFrequencyId()).isEqualTo("YEARLY");
  }

  @Test
  void policyApiMainSourceDoesNotDependOnCoreOrUiImplementations() throws IOException {
    Path mainRoot = Path.of("src/main");

    try (var paths = Files.walk(mainRoot)) {
      assertThat(
              paths
                  .filter(Files::isRegularFile)
                  .filter(
                      path ->
                          path.toString().endsWith(".java")
                              || path.toString().endsWith(".xml")
                              || path.toString().endsWith(".properties")
                              || path.toString().endsWith(".gradle"))
                  .map(
                      path -> {
                        try {
                          return Files.readString(path);
                        } catch (IOException e) {
                          throw new IllegalStateException(e);
                        }
                      }))
          .allSatisfy(
              content ->
                  assertThat(content)
                      .doesNotContain("com.insurance.policy.core")
                      .doesNotContain("com.insurance.policy.ui")
                      .doesNotContain("io.jmix.flowui")
                      .doesNotContain("jmix.ui."));
    }
  }
}
