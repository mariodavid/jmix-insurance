package com.insurance.app.partner;

import static com.insurance.partner.core.test_support.Assertions.assertThat;

import com.insurance.app.WebappApplication;
import com.insurance.app.test_support.AuthenticatedAsAdmin;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.common.test_support_ui.DataGridInteractions;
import com.insurance.common.test_support_ui.FormInteractions;
import com.insurance.common.test_support_ui.ViewInteractions;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.test_support.PartnerDataProvider;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@UiTest
@SpringBootTest(classes = {WebappApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
@ExtendWith(AuthenticatedAsAdmin.class)
class PartnerUiTest {

  @Autowired private DataManager dataManager;

  @Autowired private EntityTestData entityTestData;

  @Autowired private DatabaseCleanup databaseCleanup;

  @Autowired private ViewNavigators viewNavigators;

  @BeforeEach
  void setUp() {
    databaseCleanup.removeAllEntities();
  }

  @Test
  void given_partnerListView_when_createdThroughUi_then_partnerIsSavedAndVisibleInGrid() {
    // given
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    View<?> listView = viewInteractions.navigate("partner_Partner.list");

    // when
    DataGridInteractions.of(listView, Partner.class, "partnersDataGrid").actionPerform("create");
    View<?> detailView = viewInteractions.findOpenView("partner_Partner.detail");
    FormInteractions form = FormInteractions.of(detailView);
    form.setTextFieldValue("firstNameField", "UiCreate");
    form.setTextFieldValue("lastNameField", "Partner");
    form.click("saveAndCloseButton");

    // then
    Partner saved = loadPartnerByLastName("Partner");
    assertThat(saved).hasPartnerNoMatchingPattern().hasFirstName("UiCreate").hasLastName("Partner");

    View<?> navigatedListView = viewInteractions.findOpenView("partner_Partner.list");
    DataGridInteractions<Partner> partnersDataGrid =
        DataGridInteractions.of(navigatedListView, Partner.class, "partnersDataGrid");
    assertThat(partnersDataGrid.items())
        .anySatisfy(partner -> assertThat(partner.getId()).isEqualTo(saved.getId()));
  }

  @Test
  void given_existingPartner_when_editedThroughUi_then_nameChangesAndPartnerNoRemainsUnchanged() {
    // given
    Partner saved =
        entityTestData.saveWithDefaults(
            new PartnerDataProvider(),
            partner -> {
              partner.setPartnerNo("PT-83001");
              partner.setFirstName("Before");
              partner.setLastName("Edit");
            });

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    View<?> listView = viewInteractions.navigate("partner_Partner.list");
    DataGridInteractions<Partner> partnersDataGrid =
        DataGridInteractions.of(listView, Partner.class, "partnersDataGrid");
    Partner gridPartner =
        partnersDataGrid.items().stream()
            .filter(partner -> partner.getId().equals(saved.getId()))
            .findFirst()
            .orElseThrow();
    partnersDataGrid.select(gridPartner);

    // when
    partnersDataGrid.actionPerform("edit");

    View<?> detailView = viewInteractions.findOpenView("partner_Partner.detail");
    FormInteractions form = FormInteractions.of(detailView);
    form.setTextFieldValue("firstNameField", "After");
    form.setTextFieldValue("lastNameField", "Edited");
    form.click("saveAndCloseButton");

    // then
    Partner updated = dataManager.load(Partner.class).id(saved.getId()).one();
    assertThat(updated).hasPartnerNo("PT-83001");
    assertThat(updated).hasFirstName("After");
    assertThat(updated).hasLastName("Edited");
  }

  @Test
  void given_existingPartner_when_removedThroughUi_then_partnerIsRemovedFromDatabase() {
    // given
    Partner saved =
        entityTestData.saveWithDefaults(
            new PartnerDataProvider(),
            partner -> {
              partner.setPartnerNo("PT-83002");
              partner.setFirstName("Remove");
              partner.setLastName("Candidate");
            });

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    View<?> listView = viewInteractions.navigate("partner_Partner.list");

    DataGridInteractions<Partner> partnersDataGrid =
        DataGridInteractions.of(listView, Partner.class, "partnersDataGrid");
    Partner gridPartner =
        partnersDataGrid.items().stream()
            .filter(partner -> partner.getId().equals(saved.getId()))
            .findFirst()
            .orElseThrow();
    partnersDataGrid.select(gridPartner);

    // when
    partnersDataGrid.actionPerform("remove");
    DialogInfo confirmationDialog = UiTestUtils.getLastOpenedDialog();
    assertThat(confirmationDialog).isNotNull();
    confirmationDialog.getButtons().get(0).click();

    // then
    assertThat(dataManager.load(Partner.class).id(saved.getId()).optional()).isEmpty();
  }

  private Partner loadPartnerByLastName(String lastName) {
    return dataManager.load(Partner.class).query("e.lastName = ?1", lastName).one();
  }
}
