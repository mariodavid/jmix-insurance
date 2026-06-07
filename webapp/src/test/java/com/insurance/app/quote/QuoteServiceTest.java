package com.insurance.app.quote;

import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.policy.core.entity.Policy;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;
import com.insurance.quote.core.test_support.QuoteDataProvider;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class QuoteServiceTest extends BaseIntegrationTest {

  @Autowired private QuoteService quoteService;

  @Autowired private DataManager dataManager;

  @Autowired private EntityTestData entityTestData;

  @Autowired private DatabaseCleanup databaseCleanup;

  @BeforeEach
  void setUp() {
    databaseCleanup.removeAllEntities();
  }

  @Test
  void given_pendingQuote_when_rejected_then_quoteIsMarkedAsRejected() {
    // given
    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());

    // when
    quoteService.reject(Id.of(quote));

    // then
    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    assertThat(reloaded).isRejected();
  }

  @Test
  void given_pendingQuote_when_accepted_then_quoteIsMarkedAsAcceptedWithPolicyReference() {
    // given
    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());

    // when
    quoteService.accept(Id.of(quote));

    // then
    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    assertThat(reloaded).isAccepted().hasPolicyReference();
  }

  @Test
  void given_acceptedQuote_when_rejected_then_illegalStateExceptionIsThrown() {
    // given
    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());
    quoteService.accept(Id.of(quote));

    // when / then
    assertThatThrownBy(() -> quoteService.reject(Id.of(quote)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Only PENDING quotes");

    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    assertThat(reloaded).isAccepted().hasPolicyReference();
  }

  @Test
  void given_rejectedQuote_when_accepted_then_illegalStateExceptionIsThrownAndNoPolicyIsCreated() {
    // given
    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());
    quoteService.reject(Id.of(quote));

    // when / then
    assertThatThrownBy(() -> quoteService.accept(Id.of(quote)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Only PENDING quotes");

    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    assertThat(reloaded).isRejected();
    org.assertj.core.api.Assertions.assertThat(dataManager.load(Policy.class).all().list())
        .isEmpty();
  }

  @Test
  void given_expiredQuote_when_accepted_then_illegalStateExceptionIsThrown() {
    // given
    LocalDate yesterday = LocalDate.now().minusDays(1);
    Quote quote =
        entityTestData.saveWithDefaults(
            new QuoteDataProvider(),
            q -> {
              q.setValidFrom(yesterday.minusDays(14));
              q.setValidUntil(yesterday);
            });

    // when / then
    assertThatThrownBy(() -> quoteService.accept(Id.of(quote)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("is not valid");

    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    assertThat(reloaded).hasStatus(QuoteStatus.PENDING);
  }

  @Test
  void given_pendingQuoteWithoutPositivePremium_when_accepted_then_illegalStateExceptionIsThrown() {
    // given
    Quote quote =
        entityTestData.saveWithDefaults(
            new QuoteDataProvider(), q -> q.setCalculatedPremium(BigDecimal.ZERO));

    // when / then
    assertThatThrownBy(() -> quoteService.accept(Id.of(quote)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("positive calculated premium");

    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    assertThat(reloaded).hasStatus(QuoteStatus.PENDING);
  }
}
