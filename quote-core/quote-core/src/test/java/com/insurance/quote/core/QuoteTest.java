package com.insurance.quote.core;

import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.ProductType;
import com.insurance.product.api.dto.ProductVariant;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class QuoteTest {

    @Autowired
    private QuoteService quoteService;

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
        reset(policyService);
        deleteQuotes();
    }

    @AfterEach
    void tearDown() {
        systemAuthenticator.end();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void given_pendingQuote_when_rejected_then_statusAndRejectedTimestampArePersisted() {
        Quote quote = savePendingQuote();

        quoteService.reject(Id.of(quote));

        Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
        assertThat(reloaded.getStatus()).isEqualTo(QuoteStatus.REJECTED);
        assertThat(reloaded.getRejectedAt()).isNotNull();
        assertThat(reloaded.getAcceptedAt()).isNull();
    }

    @Test
    void given_pendingQuoteAndPolicyServiceResponse_when_accepted_then_quoteHasAcceptedStatusAndPolicyReference() {
        Quote quote = savePendingQuote();
        UUID policyId = UUID.randomUUID();
        PolicyDto policyDto = dataManager.create(PolicyDto.class);
        policyDto.setId(policyId);
        policyDto.setPolicyNo("HC-2025-000123");
        when(policyService.createPolicy(org.mockito.ArgumentMatchers.any()))
                .thenReturn(policyDto);

        Quote acceptedQuote = (Quote) quoteService.accept(Id.of(quote));

        Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
        assertThat(acceptedQuote.getId()).isEqualTo(quote.getId());
        assertThat(reloaded.getStatus()).isEqualTo(QuoteStatus.ACCEPTED);
        assertThat(reloaded.getAcceptedAt()).isNotNull();
        assertThat(reloaded.getRejectedAt()).isNull();
        assertThat(reloaded.getCreatedPolicyId()).isEqualTo(policyId.toString());
        assertThat(reloaded.getCreatedPolicyNo()).isEqualTo("HC-2025-000123");

        ArgumentCaptor<com.insurance.policy.api.dto.CreatePolicyRequestDto> requestCaptor =
                ArgumentCaptor.forClass(com.insurance.policy.api.dto.CreatePolicyRequestDto.class);
        verify(policyService).createPolicy(requestCaptor.capture());
        assertThat(requestCaptor.getValue().quoteNo()).isEqualTo(quote.getQuoteNo());
        assertThat(requestCaptor.getValue().partnerNo()).isEqualTo("PT-40001");
        assertThat(requestCaptor.getValue().insuranceProductId()).isEqualTo("HOME_CONTENT_BASIC_2024_01");
        assertThat(requestCaptor.getValue().effectiveDate()).isEqualTo(EFFECTIVE_DATE);
        assertThat(requestCaptor.getValue().premium()).isEqualByComparingTo("220.00");
        assertThat(requestCaptor.getValue().paymentFrequencyId()).isEqualTo("YEARLY");
    }

    private Quote savePendingQuote() {
        Quote quote = dataManager.create(Quote.class);
        quote.setPartnerNo("PT-40001");
        quote.setStatus(QuoteStatus.PENDING);
        quote.setProductType(ProductType.HOME_CONTENT);
        quote.setProductVariant(ProductVariant.SMALL);
        quote.setPaymentFrequency(PaymentFrequency.YEARLY);
        quote.setInsuranceProduct(InsuranceProduct.HOME_CONTENT_BASIC_2024_01);
        quote.setEffectiveDate(EFFECTIVE_DATE);
        quote.setSquareMeters(60);
        quote.setCalculatedPremium(new BigDecimal("220.00"));
        quote.setValidFrom(EFFECTIVE_DATE);
        quote.setValidUntil(EFFECTIVE_DATE.plusMonths(1));
        return dataManager.save(quote);
    }

    private void deleteQuotes() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String table = metadataTools.getDatabaseTable(metadata.getClass(Quote.class));
        jdbc.update("DELETE FROM " + table);
    }
}
