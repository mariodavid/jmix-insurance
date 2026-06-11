package com.insurance.claim.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.insurance.claim.api.dto.ClaimStatus;
import com.insurance.claim.api.dto.ReserveType;
import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.ClaimReserve;
import com.insurance.claim.core.test_support.ClaimDataProvider;
import com.insurance.claim.ui.view.claim.ClaimDetailView;
import com.insurance.claim.ui.view.claim.ClaimListView;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.common.test_support_ui.DataGridInteractions;
import com.insurance.common.test_support_ui.FormInteractions;
import com.insurance.common.test_support_ui.UiTestSupport;
import com.insurance.common.test_support_ui.ViewInteractions;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@UiTest
@SpringBootTest(
    classes = {ClaimUiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
    properties = "spring.main.allow-bean-definition-overriding=true")
class ClaimUiTest {

  @Autowired private ViewNavigators viewNavigators;

  @Autowired private DataManager dataManager;

  @Autowired private EntityTestData entityTestData;

  @Autowired private PolicyService policyService;

  @Autowired private DataSource dataSource;

  @BeforeEach
  void setUp() {
    reset(policyService);
    deleteAllClaims();
  }

  @AfterEach
  void tearDown() {
    deleteAllClaims();
  }

  @Test
  void given_claimListView_when_opened_then_createButtonIsAvailable() {
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    ClaimListView listView = viewInteractions.navigate(ClaimListView.class);

    JmixButton createButton = UiTestSupport.findButtonByText(listView, "Create");
    assertThat(createButton).isNotNull();
    assertThat(createButton.isEnabled()).isTrue();
  }

  @Test
  void given_claimCreated_when_listViewOpened_then_claimIsVisibleInGrid() {
    // given
    Claim claim = entityTestData.createWithDefaults(new ClaimDataProvider());
    dataManager.save(claim);

    // when
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    ClaimListView listView = viewInteractions.navigate(ClaimListView.class);

    // then
    DataGridInteractions<Claim> grid =
        DataGridInteractions.of(listView, Claim.class, "claimsDataGrid");
    assertThat(grid.items()).hasSize(1);
    assertThat(grid.items().getFirst().getClaimNo()).startsWith("CLM-");
  }

  @Test
  void given_policyExists_when_claimCreatedThroughUi_then_claimIsSavedWithReserve() {
    // given
    UUID policyId = UUID.randomUUID();
    PolicyDto policyDto = dataManager.create(PolicyDto.class);
    policyDto.setId(policyId);
    policyDto.setPolicyNo("HC-2026-000001");
    when(policyService.findPolicies(anyString(), anyInt(), anyInt()))
        .thenReturn(List.of(policyDto));

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    ClaimListView listView = viewInteractions.navigate(ClaimListView.class);
    JmixButton createButton = UiTestSupport.findButtonByText(listView, "Create");
    createButton.click();

    // when
    ClaimDetailView detailView = viewInteractions.findOpenView(ClaimDetailView.class);

    FormInteractions form = FormInteractions.of(detailView);
    EntityComboBox<PolicyDto> policyComboBox =
        form.entityComboBoxField("policyComboBox", PolicyDto.class);
    policyComboBox.setValue(policyDto);

    form.setFieldValueByLabel("Incident Date", LocalDate.of(2026, 6, 1));
    form.setFieldValueByLabel("Expected Claim Amount", new BigDecimal("2000.00"));

    JmixButton saveButton = UiTestSupport.findButtonByText(detailView, "OK");
    assertThat(saveButton).isNotNull();
    saveButton.click();

    // then
    List<Claim> claims = dataManager.load(Claim.class).all().list();
    assertThat(claims).hasSize(1);

    Claim savedClaim = claims.getFirst();
    assertThat(savedClaim.getClaimNo()).startsWith("CLM-2026-");
    assertThat(savedClaim.getPolicyNo()).isEqualTo("HC-2026-000001");
    assertThat(savedClaim.getClaimStatus()).isEqualTo(ClaimStatus.OPEN);

    Claim claimWithReserves = dataManager.load(Claim.class)
        .id(savedClaim.getId())
        .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("reserves", FetchPlan.BASE))
        .one();
    assertThat(claimWithReserves.getReserves()).hasSize(1);

    ClaimReserve reserve = claimWithReserves.getReserves().getFirst();
    assertThat(reserve.getReserveType()).isEqualTo(ReserveType.INDEMNITY);
    assertThat(reserve.getReserveAmount()).isEqualByComparingTo("2000.00");
  }

  private void deleteAllClaims() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    jdbc.update("DELETE FROM CLAIM_CLAIM_RESERVE");
    jdbc.update("DELETE FROM CLAIM_CLAIM");
  }
}
