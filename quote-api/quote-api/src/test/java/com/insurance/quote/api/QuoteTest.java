package com.insurance.quote.api;

import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.ProductType;
import com.insurance.product.api.dto.ProductVariant;
import com.insurance.quote.api.dto.QuoteDto;
import com.insurance.quote.api.dto.QuoteStatus;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.insurance.quote.api.dto.Assertions.assertThat;

@SpringBootTest
class QuoteTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Metadata metadata;

    @Test
    void contextLoads() {
    }

    @Test
    void given_quoteDto_when_createdThroughDataManager_then_metadataAndEnumFieldsAreStable() {
        QuoteDto dto = dataManager.create(QuoteDto.class);
        UUID policyId = UUID.randomUUID();
        LocalDate acceptedDate = LocalDate.of(2025, 1, 2);

        dto.setPartnerNo("PT-36001");
        dto.setQuoteNo("QT-36001");
        dto.setStatus(QuoteStatus.PENDING);
        dto.setProductType(ProductType.HOME_CONTENT);
        dto.setProductVariant(ProductVariant.SMALL);
        dto.setPaymentFrequency(PaymentFrequency.YEARLY);
        dto.setInsuranceProduct(InsuranceProduct.HOME_CONTENT_BASIC_2024_01);
        dto.setEffectiveDate(LocalDate.of(2025, 1, 1));
        dto.setSquareMeters(65);
        dto.setCalculatedPremium(new BigDecimal("240.00"));
        dto.setValidFrom(LocalDate.of(2025, 1, 1));
        dto.setValidUntil(LocalDate.of(2025, 12, 31));
        dto.setCreatedPolicyNo("HC-2025-000001");
        dto.setCreatedPolicyId(policyId);
        dto.setAcceptedAt(acceptedDate.atStartOfDay());

        MetaClass metaClass = metadata.getClass(QuoteDto.class);

        assertThat(dto.getId()).isNotNull();
        assertThat(metaClass.getProperty("id")).isNotNull();
        assertThat(dto).hasQuoteNo("QT-36001");
        assertThat(dto.getStatus()).isEqualTo(QuoteStatus.PENDING);
        assertThat(dto.getProductType()).isEqualTo(ProductType.HOME_CONTENT);
        assertThat(dto.getProductVariant()).isEqualTo(ProductVariant.SMALL);
        assertThat(dto.getPaymentFrequency()).isEqualTo(PaymentFrequency.YEARLY);
        assertThat(dto.getInsuranceProduct()).isEqualTo(InsuranceProduct.HOME_CONTENT_BASIC_2024_01);
        assertThat(dto).hasEffectiveDate(LocalDate.of(2025, 1, 1));
        assertThat(dto).hasCalculatedPremium(new BigDecimal("240.00"));
        assertThat(dto).hasCreatedPolicyNo("HC-2025-000001");
        assertThat(dto).hasCreatedPolicyId(policyId);
        assertThat(dto.getAcceptedAt()).isEqualTo(LocalDateTime.of(2025, 1, 2, 0, 0));
        assertThat(dto.instanceName()).isEqualTo("QT-36001");
    }

    @Test
    void given_quoteStatusId_when_fromId_then_knownValuesResolveAndUnknownReturnsNull() {
        assertThat(QuoteStatus.fromId("PENDING")).isEqualTo(QuoteStatus.PENDING);
        assertThat(QuoteStatus.fromId("ACCEPTED")).isEqualTo(QuoteStatus.ACCEPTED);
        assertThat(QuoteStatus.fromId("REJECTED")).isEqualTo(QuoteStatus.REJECTED);
        assertThat(QuoteStatus.fromId("UNKNOWN")).isNull();
        assertThat(QuoteStatus.fromId(null)).isNull();
    }

    @Test
    void quoteApiMainSourceDoesNotDependOnCoreOrUiImplementations() throws IOException {
        Path mainRoot = Path.of("src/main");

        try (var paths = Files.walk(mainRoot)) {
            assertThat(paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java")
                            || path.toString().endsWith(".xml")
                            || path.toString().endsWith(".properties")
                            || path.toString().endsWith(".gradle"))
                    .map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }))
                    .allSatisfy(content -> assertThat(content)
                            .doesNotContain("com.insurance.quote.core")
                            .doesNotContain("com.insurance.quote.ui")
                            .doesNotContain("io.jmix.flowui")
                            .doesNotContain("jmix.ui."));
        }
    }
}
