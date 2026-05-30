package com.insurance.app.quote;

import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.app.test_support.QuoteFactory;
import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;

class QuoteServiceTest extends BaseIntegrationTest {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private QuoteFactory quoteFactory;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    @BeforeEach
    void setUp() {
        databaseCleanup.removeAllEntities();
    }

    @Test
    void given_pendingQuote_when_rejected_then_quoteIsMarkedAsRejected() {
        // given
        Quote quote = quoteFactory.saveDefault();

        // when
        quoteService.reject(Id.of(quote));

        // then
        Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
        assertThat(reloaded).isRejected();
    }

    @Test
    void given_pendingQuote_when_accepted_then_quoteIsMarkedAsAcceptedWithPolicyReference() {
        // given
        Quote quote = quoteFactory.saveDefault();

        // when
        quoteService.accept(Id.of(quote));

        // then
        Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
        assertThat(reloaded)
                .isAccepted()
                .hasPolicyReference();
    }
}
