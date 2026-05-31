package com.insurance.app.policy;

import com.insurance.account.core.entity.Account;
import com.insurance.app.InsuranceAppApplication;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.test_support.PartnerDataProvider;
import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.policy.core.entity.Policy;
import com.insurance.policy.ui.view.policy.PolicyDetailView;
import com.insurance.policy.ui.view.policy.PolicyListView;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;
import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThatThrownBy;

@UiTest
@SpringBootTest(classes = {InsuranceAppApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
class PolicyUiTest {

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private EntityTestData entityTestData;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    private PolicyDto createPolicy(String paymentFrequencyId, BigDecimal premium) {
        Partner partner = entityTestData.saveWithDefaults(new PartnerDataProvider());
        return policyService.createPolicy(new CreatePolicyRequestDto(
                "QT-FACTORY",
                partner.getPartnerNo(),
                "HOME_CONTENT_BASIC_2024_01",
                LocalDate.of(2025, 1, 1),
                premium,
                paymentFrequencyId
        ));
    }

    @BeforeEach
    void setUp() {
        databaseCleanup.removeAllEntities();
    }

    @Test
    void given_policyCreatedAtomically_when_policyListOpened_then_policyIsVisibleAndAccountExists() {
        PolicyDto policy = createPolicy("YEARLY", new BigDecimal("240.00"));

        viewNavigators.view(UiTestUtils.getCurrentView(), PolicyListView.class).navigate();

        PolicyListView listView = UiTestUtils.getCurrentView();
        DataGrid<Policy> policiesDataGrid = UiTestUtils.getComponent(listView, "policiesDataGrid");
        DataGridItems<Policy> items = policiesDataGrid.getItems();

        assertThat(items).isNotNull();
        assertThat(items.getItems())
                .anySatisfy(item -> assertThat(item.getPolicyNo()).isEqualTo(policy.getPolicyNo()));
        assertThat(loadAccountByNo(policy.getPolicyNo())).hasPolicyId(policy.getId().toString());
    }

    @Test
    void given_policyWithAccount_when_detailBalanceDateChanged_then_balanceIsDisplayed() {
        PolicyDto policy = createPolicy("YEARLY", new BigDecimal("240.00"));
        Policy persistedPolicy = loadPolicyByNo(policy.getPolicyNo());

        viewNavigators.detailView(UiTestUtils.getCurrentView(), Policy.class)
                .editEntity(persistedPolicy)
                .navigate();

        PolicyDetailView detailView = UiTestUtils.getCurrentView();
        TypedDatePicker<LocalDate> effectiveDatePicker =
                UiTestUtils.getComponent(detailView, "accountBalanceEffectiveDatePicker");
        effectiveDatePicker.setValue(LocalDate.of(2025, 1, 1));

        TypedTextField<String> balanceResult = UiTestUtils.getComponent(detailView, "accountBalanceResult");
        assertThat(balanceResult.getValue()).isEqualTo("-240.00");
    }

    @Test
    void given_policyListView_when_opened_then_manualPolicyCreationIsNotOffered() {
        viewNavigators.view(UiTestUtils.getCurrentView(), PolicyListView.class).navigate();

        PolicyListView listView = UiTestUtils.getCurrentView();
        JmixButton readButton = UiTestUtils.getComponent(listView, "readButton");

        assertThat(readButton).isNotNull();
        assertThatThrownBy(() -> UiTestUtils.getComponent(listView, "createButton"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(dataManager.load(Policy.class).all().list()).isEmpty();
    }

    private Policy loadPolicyByNo(String policyNo) {
        return dataManager.load(Policy.class)
                .condition(PropertyCondition.equal("policyNo", policyNo))
                .one();
    }

    private Account loadAccountByNo(String accountNo) {
        return dataManager.load(Account.class)
                .condition(PropertyCondition.equal("accountNo", accountNo))
                .one();
    }
}
