package com.insurance.account.ui;

import static com.insurance.account.core.test_support.Assertions.assertThat;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.account.core.service.AccountServiceCore;
import com.insurance.account.ui.view.account.AccountDetailView;
import com.insurance.account.ui.view.account.AccountListView;
import com.insurance.common.test_support_ui.DataGridInteractions;
import com.insurance.common.test_support_ui.UiTestSupport;
import com.insurance.common.test_support_ui.ViewInteractions;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@UiTest
@SpringBootTest(
    classes = {AccountUiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
    properties = "spring.main.allow-bean-definition-overriding=true")
class AccountUiTest {

  private static final LocalDate COVERAGE_START = LocalDate.of(2025, 1, 1);
  private static final BigDecimal PREMIUM = new BigDecimal("120.00");
  private static final java.util.UUID POLICY_ID =
      java.util.UUID.fromString("00000000-0000-0000-0000-000000000065");

  @Autowired private ViewNavigators viewNavigators;

  @Autowired private AccountServiceCore accountService;

  @Autowired private DataSource dataSource;

  @Autowired private DataManager dataManager;

  @BeforeEach
  void setUp() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    jdbc.update("DELETE FROM ACCOUNT_ACCOUNT_DOCUMENT");
    jdbc.update("DELETE FROM ACCOUNT_ACCOUNT");
  }

  @Test
  void given_accountCreated_when_accountListOpened_then_accountIsVisible() {
    // given
    accountService.createAccount(
        POLICY_ID, "HC-2025-000065", COVERAGE_START, PREMIUM, PaymentFrequency.YEARLY);

    // when
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    AccountListView listView = viewInteractions.navigate(AccountListView.class);

    // then
    DataGridInteractions<Account> accountsDataGrid =
        DataGridInteractions.of(listView, Account.class, "accountsDataGrid");
    assertThat(accountsDataGrid.items())
        .anySatisfy(
            account -> {
              assertThat(account)
                  .hasAccountNo("HC-2025-000065")
                  .hasPolicyId(POLICY_ID)
                  .hasBalance(PREMIUM.negate());
            });
  }

  @Test
  void given_accountWithDocuments_when_accountDetailOpened_then_documentsAreVisibleAndSorted() {
    // given
    Account account =
        accountService.createAccount(
            POLICY_ID, "HC-2025-000066", COVERAGE_START, PREMIUM, PaymentFrequency.QUARTERLY);
    Account reloaded = dataManager.load(Account.class).id(account.getId()).one();

    // when
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    viewNavigators
        .detailView(UiTestUtils.getCurrentView(), Account.class)
        .editEntity(reloaded)
        .navigate();

    // then
    AccountDetailView detailView = viewInteractions.findOpenView(AccountDetailView.class);
    DataGridInteractions<AccountDocument> documentsDataGrid =
        DataGridInteractions.of(detailView, AccountDocument.class, "documentsDataGrid");
    List<AccountDocument> documents = documentsDataGrid.items();

    assertThat(documents).hasSize(4);
    assertThat(documents)
        .isSortedAccordingTo(Comparator.comparing(AccountDocument::getDocumentDate));
    for (int i = 0; i < documents.size(); i++) {
      assertThat(documents.get(i))
          .hasAmount(new BigDecimal("-30.00"))
          .hasDocumentDate(COVERAGE_START.plusMonths((long) i * 3));
    }
  }

  @Test
  void given_accountListView_when_opened_then_manualAccountCreationIsNotOffered() {
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    AccountListView listView = viewInteractions.navigate(AccountListView.class);
    JmixButton readButton = UiTestSupport.findButtonByText(listView, "Read");

    assertThat(readButton).isNotNull();
    JmixButton createButton = UiTestSupport.findButtonByText(listView, "Create");
    assertThat(createButton).isNull();
    assertThat(dataManager.load(Account.class).all().list()).isEmpty();
  }
}
