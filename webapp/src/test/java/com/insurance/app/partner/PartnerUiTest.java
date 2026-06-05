package com.insurance.app.partner;

import static com.insurance.partner.core.test_support.Assertions.assertThat;

import com.insurance.app.WebappApplication;
import com.insurance.app.test_support.AuthenticatedAsAdmin;
import com.insurance.common.test_support_ui.DataGridInteractions;
import com.insurance.common.test_support_ui.FormInteractions;
import com.insurance.common.test_support_ui.UiTestSupport;
import com.insurance.common.test_support_ui.ViewInteractions;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.ui.view.partner.PartnerDetailView;
import com.insurance.partner.ui.view.partner.PartnerListView;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
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

  @Autowired private ViewNavigators viewNavigators;

  private final List<UUID> cleanupIds = new ArrayList<>();

  @Test
  void given_partnerListView_when_createdThroughUi_then_partnerIsSavedAndVisibleInGrid() {
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    PartnerListView listView = viewInteractions.navigate(PartnerListView.class);
    JmixButton createButton = UiTestSupport.findButtonByText(listView, "Create");
    assertThat(createButton).isNotNull();
    createButton.click();

    PartnerDetailView detailView = viewInteractions.findOpenView(PartnerDetailView.class);
    FormInteractions form = FormInteractions.of(detailView);
    form.setFieldValueByLabel("First name", "UiCreate");
    form.setFieldValueByLabel("Last name", "Partner");

    JmixButton saveAndCloseButton = UiTestSupport.findButtonByText(detailView, "OK");
    assertThat(saveAndCloseButton).isNotNull();
    saveAndCloseButton.click();

    Partner saved = loadPartnerByLastName("Partner");
    cleanupIds.add(saved.getId());

    assertThat(saved).hasPartnerNoMatchingPattern();
    assertThat(saved).hasFirstName("UiCreate");
    assertThat(saved).hasLastName("Partner");

    PartnerListView navigatedListView = viewInteractions.findOpenView(PartnerListView.class);
    DataGridInteractions<Partner> partnersDataGrid =
        DataGridInteractions.of(navigatedListView, Partner.class, "partnersDataGrid");
    assertThat(partnersDataGrid.items())
        .anySatisfy(partner -> assertThat(partner.getId()).isEqualTo(saved.getId()));
  }

  @Test
  void given_existingPartner_when_editedThroughUi_then_nameChangesAndPartnerNoRemainsUnchanged() {
    Partner existing = dataManager.create(Partner.class);
    existing.setPartnerNo("PT-83001");
    existing.setFirstName("Before");
    existing.setLastName("Edit");
    Partner saved = dataManager.save(existing);
    cleanupIds.add(saved.getId());

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    PartnerListView listView = viewInteractions.navigate(PartnerListView.class);
    DataGridInteractions<Partner> partnersDataGrid =
        DataGridInteractions.of(listView, Partner.class, "partnersDataGrid");
    Partner gridPartner =
        partnersDataGrid.items().stream()
            .filter(partner -> partner.getId().equals(saved.getId()))
            .findFirst()
            .orElseThrow();
    partnersDataGrid.select(gridPartner);

    JmixButton editButton = UiTestSupport.findButtonByText(listView, "Edit");
    assertThat(editButton).isNotNull();
    editButton.click();

    PartnerDetailView detailView = viewInteractions.findOpenView(PartnerDetailView.class);
    FormInteractions form = FormInteractions.of(detailView);
    form.setFieldValueByLabel("First name", "After");
    form.setFieldValueByLabel("Last name", "Edited");

    JmixButton saveAndCloseButton = UiTestSupport.findButtonByText(detailView, "OK");
    assertThat(saveAndCloseButton).isNotNull();
    saveAndCloseButton.click();

    Partner updated = dataManager.load(Partner.class).id(saved.getId()).one();

    assertThat(updated).hasPartnerNo("PT-83001");
    assertThat(updated).hasFirstName("After");
    assertThat(updated).hasLastName("Edited");
  }

  @Test
  void given_existingPartner_when_removedThroughUi_then_partnerIsRemovedFromDatabase() {
    Partner existing = dataManager.create(Partner.class);
    existing.setPartnerNo("PT-83002");
    existing.setFirstName("Remove");
    existing.setLastName("Candidate");
    Partner saved = dataManager.save(existing);
    cleanupIds.add(saved.getId());

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    PartnerListView listView = viewInteractions.navigate(PartnerListView.class);

    DataGridInteractions<Partner> partnersDataGrid =
        DataGridInteractions.of(listView, Partner.class, "partnersDataGrid");
    Partner gridPartner =
        partnersDataGrid.items().stream()
            .filter(partner -> partner.getId().equals(saved.getId()))
            .findFirst()
            .orElseThrow();
    partnersDataGrid.select(gridPartner);

    JmixButton removeButton = UiTestSupport.findButtonByText(listView, "Remove");
    assertThat(removeButton).isNotNull();
    removeButton.click();
    DialogInfo confirmationDialog = UiTestUtils.getLastOpenedDialog();
    assertThat(confirmationDialog).isNotNull();
    confirmationDialog.getButtons().get(0).click();

    assertThat(dataManager.load(Partner.class).id(saved.getId()).optional()).isEmpty();
    cleanupIds.remove(saved.getId());
  }

  @AfterEach
  void tearDown() {
    cleanupIds.forEach(
        id -> {
          dataManager.load(Partner.class).id(id).optional().ifPresent(dataManager::remove);
        });
    cleanupIds.clear();
  }

  private Partner loadPartnerByLastName(String lastName) {
    return dataManager.load(Partner.class).query("e.lastName = ?1", lastName).one();
  }
}
