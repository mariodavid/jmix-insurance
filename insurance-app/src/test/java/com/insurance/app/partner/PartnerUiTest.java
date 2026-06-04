package com.insurance.app.partner;

import static com.insurance.partner.core.test_support.Assertions.assertThat;

import com.insurance.app.InsuranceAppApplication;
import com.insurance.app.test_support.AuthenticatedAsAdmin;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.ui.view.partner.PartnerDetailView;
import com.insurance.partner.ui.view.partner.PartnerListView;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
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
@SpringBootTest(classes = {InsuranceAppApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
@ExtendWith(AuthenticatedAsAdmin.class)
class PartnerUiTest {

  @Autowired private DataManager dataManager;

  @Autowired private ViewNavigators viewNavigators;

  private final List<UUID> cleanupIds = new ArrayList<>();

  @Test
  void given_partnerListView_when_createdThroughUi_then_partnerIsSavedAndVisibleInGrid() {
    viewNavigators.view(UiTestUtils.getCurrentView(), PartnerListView.class).navigate();

    PartnerListView listView = UiTestUtils.getCurrentView();
    JmixButton createButton = UiTestUtils.getComponent(listView, "createButton");
    createButton.click();

    PartnerDetailView detailView = UiTestUtils.getCurrentView();
    TypedTextField<String> firstNameField = UiTestUtils.getComponent(detailView, "firstNameField");
    TypedTextField<String> lastNameField = UiTestUtils.getComponent(detailView, "lastNameField");
    firstNameField.setValue("UiCreate");
    lastNameField.setValue("Partner");

    JmixButton saveAndCloseButton = UiTestUtils.getComponent(detailView, "saveAndCloseButton");
    saveAndCloseButton.click();

    Partner saved = loadPartnerByLastName("Partner");
    cleanupIds.add(saved.getId());

    assertThat(saved).hasPartnerNoMatchingPattern();
    assertThat(saved).hasFirstName("UiCreate");
    assertThat(saved).hasLastName("Partner");

    PartnerListView navigatedListView = UiTestUtils.getCurrentView();
    DataGrid<Partner> partnersDataGrid =
        UiTestUtils.getComponent(navigatedListView, "partnersDataGrid");
    assertThat(gridItems(partnersDataGrid))
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

    viewNavigators.view(UiTestUtils.getCurrentView(), PartnerListView.class).navigate();

    PartnerListView listView = UiTestUtils.getCurrentView();
    DataGrid<Partner> partnersDataGrid = UiTestUtils.getComponent(listView, "partnersDataGrid");
    Partner gridPartner =
        gridItems(partnersDataGrid).stream()
            .filter(partner -> partner.getId().equals(saved.getId()))
            .findFirst()
            .orElseThrow();
    partnersDataGrid.select(gridPartner);

    JmixButton editButton = UiTestUtils.getComponent(listView, "editButton");
    editButton.click();

    PartnerDetailView detailView = UiTestUtils.getCurrentView();
    TypedTextField<String> firstNameField = UiTestUtils.getComponent(detailView, "firstNameField");
    TypedTextField<String> lastNameField = UiTestUtils.getComponent(detailView, "lastNameField");
    firstNameField.setValue("After");
    lastNameField.setValue("Edited");

    JmixButton saveAndCloseButton = UiTestUtils.getComponent(detailView, "saveAndCloseButton");
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

    viewNavigators.view(UiTestUtils.getCurrentView(), PartnerListView.class).navigate();

    PartnerListView listView = UiTestUtils.getCurrentView();
    DataGrid<Partner> partnersDataGrid = UiTestUtils.getComponent(listView, "partnersDataGrid");
    Partner gridPartner =
        gridItems(partnersDataGrid).stream()
            .filter(partner -> partner.getId().equals(saved.getId()))
            .findFirst()
            .orElseThrow();
    partnersDataGrid.select(gridPartner);

    JmixButton removeButton = UiTestUtils.getComponent(listView, "removeButton");
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

  private List<Partner> gridItems(DataGrid<Partner> dataGrid) {
    DataGridItems<Partner> items = dataGrid.getItems();
    assertThat(items).isNotNull();
    return items.getItems().stream().toList();
  }
}
