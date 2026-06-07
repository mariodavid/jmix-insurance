package com.insurance.app.quote;

import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.policy.core.entity.Policy;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;
import com.insurance.quote.core.test_support.QuoteDataProvider;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.querycondition.PropertyCondition;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Testet die vollständige Akzeptanzkette: QuoteService.accept() → PolicyService →
 * PolicyCreatedEvent → AccountService
 */
class QuoteAcceptanceFlowTest extends BaseIntegrationTest {

  @Autowired private QuoteService quoteService;

  @Autowired private DataManager dataManager;

  @Autowired private EntityTestData entityTestData;

  @Autowired private DatabaseCleanup databaseCleanup;

  @BeforeEach
  void setUp() {
    databaseCleanup.removeAllEntities();
  }

  @Test
  void given_acceptedQuote_when_policyLoaded_then_policyDataMatchesQuote() {
    // given
    BigDecimal premium = new BigDecimal("300.00");
    Quote quote =
        entityTestData.saveWithDefaults(
            new QuoteDataProvider(),
            q -> {
              q.setPaymentFrequency(PaymentFrequency.YEARLY);
              q.setCalculatedPremium(premium);
            });

    // when
    quoteService.accept(Id.of(quote));

    // then
    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    Policy policy = loadPolicyByNo(reloaded.getCreatedPolicyNo());

    assertThat(policy)
        .hasPolicyNo(reloaded.getCreatedPolicyNo())
        .hasCoverageStart(quote.getEffectiveDate())
        .hasPremium(premium);
  }

  @Test
  void given_acceptedQuote_when_accountLoaded_then_balanceEqualsNegativePremium() {
    // given
    BigDecimal premium = new BigDecimal("480.00");
    Quote quote =
        entityTestData.saveWithDefaults(
            new QuoteDataProvider(),
            q -> {
              q.setPaymentFrequency(PaymentFrequency.YEARLY);
              q.setCalculatedPremium(premium);
            });

    // when
    quoteService.accept(Id.of(quote));

    // then
    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    Account account = loadAccountByNo(reloaded.getCreatedPolicyNo());

    assertThat(account).hasAccountNo(reloaded.getCreatedPolicyNo()).hasBalance(premium.negate());
  }

  @Test
  void given_acceptedQuoteWithMonthlyFrequency_when_accountLoaded_then_twelveDocumentsCreated() {
    // given
    Quote quote =
        entityTestData.saveWithDefaults(
            new QuoteDataProvider(),
            q -> {
              q.setPaymentFrequency(PaymentFrequency.MONTHLY);
              q.setCalculatedPremium(new BigDecimal("120.00"));
            });

    // when
    quoteService.accept(Id.of(quote));

    // then
    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    Account account = loadAccountByNo(reloaded.getCreatedPolicyNo());
    List<AccountDocument> docs =
        dataManager
            .load(AccountDocument.class)
            .query("select d from account_AccountDocument d where d.account = :account")
            .parameter("account", account)
            .list();

    assertThat(docs).hasSize(12);
  }

  @Test
  void given_acceptedQuote_when_acceptedAgain_then_noSecondPolicyOrAccountIsCreated() {
    // given
    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());
    quoteService.accept(Id.of(quote));

    // when / then
    assertThatThrownBy(() -> quoteService.accept(Id.of(quote)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Only PENDING quotes");

    org.assertj.core.api.Assertions.assertThat(dataManager.load(Policy.class).all().list())
        .hasSize(1);
    org.assertj.core.api.Assertions.assertThat(dataManager.load(Account.class).all().list())
        .hasSize(1);
  }

  private Policy loadPolicyByNo(String policyNo) {
    return dataManager
        .load(Policy.class)
        .condition(PropertyCondition.equal("policyNo", policyNo))
        .one();
  }

  private Account loadAccountByNo(String accountNo) {
    return dataManager
        .load(Account.class)
        .condition(PropertyCondition.equal("policy.policyNo", accountNo))
        .one();
  }
}
