package com.insurance.account.ui;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.account.core.service.AccountServiceCore;
import com.insurance.account.ui.view.account.AccountDetailView;
import com.insurance.account.ui.view.account.AccountListView;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static com.insurance.account.core.test_support.Assertions.assertThat;
import static com.insurance.account.core.test_support.Assertions.assertThatThrownBy;

@UiTest
@SpringBootTest(classes = {AccountUiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
        properties = "spring.main.allow-bean-definition-overriding=true")
class AccountUiTest {

    private static final LocalDate COVERAGE_START = LocalDate.of(2025, 1, 1);
    private static final BigDecimal PREMIUM = new BigDecimal("120.00");
    private static final java.util.UUID POLICY_ID = java.util.UUID.fromString("00000000-0000-0000-0000-000000000065");

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private AccountServiceCore accountService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataManager dataManager;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("DELETE FROM ACCOUNT_ACCOUNT_DOCUMENT");
        jdbc.update("DELETE FROM ACCOUNT_ACCOUNT");
    }

    @Test
    void given_accountCreated_when_accountListOpened_then_accountIsVisible() {
        // given
        accountService.createAccount(POLICY_ID, "HC-2025-000065", COVERAGE_START, PREMIUM, PaymentFrequency.YEARLY);

        // when
        viewNavigators.view(UiTestUtils.getCurrentView(), AccountListView.class).navigate();

        // then
        AccountListView listView = UiTestUtils.getCurrentView();
        DataGrid<Account> accountsDataGrid = UiTestUtils.getComponent(listView, "accountsDataGrid");
        assertThat(gridItems(accountsDataGrid))
                .anySatisfy(account -> {
                    assertThat(account)
                            .hasAccountNo("HC-2025-000065")
                            .hasPolicyId(POLICY_ID)
                            .hasBalance(PREMIUM.negate());
                });
    }

    @Test
    void given_accountWithDocuments_when_accountDetailOpened_then_documentsAreVisibleAndSorted() {
        // given
        Account account = accountService.createAccount(
                POLICY_ID, "HC-2025-000066", COVERAGE_START, PREMIUM, PaymentFrequency.QUARTERLY);
        Account reloaded = dataManager.load(Account.class).id(account.getId()).one();

        // when
        viewNavigators.detailView(UiTestUtils.getCurrentView(), Account.class)
                .editEntity(reloaded)
                .navigate();

        // then
        AccountDetailView detailView = UiTestUtils.getCurrentView();
        DataGrid<AccountDocument> documentsDataGrid = UiTestUtils.getComponent(detailView, "documentsDataGrid");
        List<AccountDocument> documents = gridItems(documentsDataGrid);

        assertThat(documents).hasSize(4);
        assertThat(documents).isSortedAccordingTo(Comparator.comparing(AccountDocument::getDocumentDate));
        for (int i = 0; i < documents.size(); i++) {
            assertThat(documents.get(i))
                    .hasAmount(new BigDecimal("-30.00"))
                    .hasDocumentDate(COVERAGE_START.plusMonths((long) i * 3));
        }
    }

    @Test
    void given_accountListView_when_opened_then_manualAccountCreationIsNotOffered() {
        viewNavigators.view(UiTestUtils.getCurrentView(), AccountListView.class).navigate();

        AccountListView listView = UiTestUtils.getCurrentView();
        JmixButton readButton = UiTestUtils.getComponent(listView, "readButton");

        assertThat(readButton).isNotNull();
        assertThatThrownBy(() -> UiTestUtils.getComponent(listView, "createButton"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(dataManager.load(Account.class).all().list()).isEmpty();
    }

    private <T> List<T> gridItems(DataGrid<T> dataGrid) {
        DataGridItems<T> items = dataGrid.getItems();
        assertThat(items).isNotNull();
        return items.getItems().stream().toList();
    }
}
