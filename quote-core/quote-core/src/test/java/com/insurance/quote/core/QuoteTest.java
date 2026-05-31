package com.insurance.quote.core;

import com.insurance.common.test_support.AuthenticatedAsAdmin;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;
import com.insurance.quote.core.test_support.QuoteDataProvider;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static com.insurance.quote.core.test_support.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ExtendWith(AuthenticatedAsAdmin.class)
class QuoteTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntityTestData entityTestData;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    private static final LocalDate EFFECTIVE_DATE = LocalDate.of(2025, 1, 1);

    @BeforeEach
    void setUp() {
        reset(policyService);
        deleteQuotes();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void given_pendingQuote_when_rejected_then_statusAndRejectedTimestampArePersisted() {
        Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());

        quoteService.reject(Id.of(quote));

        Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
        assertThat(reloaded)
                .hasStatus(QuoteStatus.REJECTED);
        assertThat(reloaded.getRejectedAt()).isNotNull();
        assertThat(reloaded.getAcceptedAt()).isNull();
    }

    @Test
    void given_pendingQuoteAndPolicyServiceResponse_when_accepted_then_quoteHasAcceptedStatusAndPolicyReference() {
        Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider(), q -> q.setPartnerNo("PT-40001"));
        UUID policyId = UUID.randomUUID();
        PolicyDto policyDto = dataManager.create(PolicyDto.class);
        policyDto.setId(policyId);
        policyDto.setPolicyNo("HC-2025-000123");
        when(policyService.createPolicy(org.mockito.ArgumentMatchers.any()))
                .thenReturn(policyDto);

        Quote acceptedQuote = (Quote) quoteService.accept(Id.of(quote));

        Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
        assertThat(acceptedQuote.getId()).isEqualTo(quote.getId());
        assertThat(reloaded)
                .hasStatus(QuoteStatus.ACCEPTED)
                .hasCreatedPolicyId(policyId.toString())
                .hasCreatedPolicyNo("HC-2025-000123");
        assertThat(reloaded.getAcceptedAt()).isNotNull();
        assertThat(reloaded.getRejectedAt()).isNull();

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

    @Test
    void given_pendingQuoteAndPolicyServiceThrowsException_when_accepted_then_exceptionIsThrownAndStatusRemainsPending() {
        Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());
        when(policyService.createPolicy(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("Policy creation failed"));

        assertThatThrownBy(() -> quoteService.accept(Id.of(quote)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Policy creation failed");

        Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
        assertThat(reloaded)
                .hasStatus(QuoteStatus.PENDING)
                .hasCreatedPolicyId(null)
                .hasCreatedPolicyNo(null);
        assertThat(reloaded.getAcceptedAt()).isNull();
        assertThat(reloaded.getRejectedAt()).isNull();
    }

    private void deleteQuotes() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String table = metadataTools.getDatabaseTable(metadata.getClass(Quote.class));
        jdbc.update("DELETE FROM " + table);
    }
}
