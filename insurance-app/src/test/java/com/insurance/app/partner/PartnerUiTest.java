package com.insurance.app.partner;

import com.insurance.app.InsuranceAppApplication;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.ui.view.partner.PartnerDetailView;
import com.insurance.partner.ui.view.partner.PartnerListView;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {InsuranceAppApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
class PartnerUiTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

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

        assertThat(saved.getPartnerNo()).matches("PT-\\d{5}");
        assertThat(saved.getFirstName()).isEqualTo("UiCreate");
        assertThat(saved.getLastName()).isEqualTo("Partner");

        PartnerListView navigatedListView = UiTestUtils.getCurrentView();
        DataGrid<Partner> partnersDataGrid = UiTestUtils.getComponent(navigatedListView, "partnersDataGrid");
        assertThat(gridItems(partnersDataGrid))
                .anySatisfy(partner -> assertThat(partner.getId()).isEqualTo(saved.getId()));
    }

    @Test
    void given_existingPartner_when_editedThroughUi_then_nameChangesAndPartnerNoRemainsUnchanged() {
        Partner existing = systemAuthenticator.withUser("admin", () -> {
            Partner partner = dataManager.create(Partner.class);
            partner.setPartnerNo("PT-83001");
            partner.setFirstName("Before");
            partner.setLastName("Edit");
            return dataManager.save(partner);
        });
        cleanupIds.add(existing.getId());

        viewNavigators.view(UiTestUtils.getCurrentView(), PartnerListView.class).navigate();

        PartnerListView listView = UiTestUtils.getCurrentView();
        DataGrid<Partner> partnersDataGrid = UiTestUtils.getComponent(listView, "partnersDataGrid");
        Partner gridPartner = gridItems(partnersDataGrid).stream()
                .filter(partner -> partner.getId().equals(existing.getId()))
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

        Partner updated = systemAuthenticator.withUser("admin",
                () -> dataManager.load(Partner.class).id(existing.getId()).one());

        assertThat(updated.getPartnerNo()).isEqualTo("PT-83001");
        assertThat(updated.getFirstName()).isEqualTo("After");
        assertThat(updated.getLastName()).isEqualTo("Edited");
    }

    @Test
    void given_existingPartner_when_removedThroughUi_then_partnerIsRemovedFromDatabase() {
        Partner existing = systemAuthenticator.withUser("admin", () -> {
            Partner partner = dataManager.create(Partner.class);
            partner.setPartnerNo("PT-83002");
            partner.setFirstName("Remove");
            partner.setLastName("Candidate");
            return dataManager.save(partner);
        });
        cleanupIds.add(existing.getId());

        viewNavigators.view(UiTestUtils.getCurrentView(), PartnerListView.class).navigate();

        PartnerListView listView = UiTestUtils.getCurrentView();
        DataGrid<Partner> partnersDataGrid = UiTestUtils.getComponent(listView, "partnersDataGrid");
        Partner gridPartner = gridItems(partnersDataGrid).stream()
                .filter(partner -> partner.getId().equals(existing.getId()))
                .findFirst()
                .orElseThrow();
        partnersDataGrid.select(gridPartner);

        JmixButton removeButton = UiTestUtils.getComponent(listView, "removeButton");
        removeButton.click();
        DialogInfo confirmationDialog = UiTestUtils.getLastOpenedDialog();
        assertThat(confirmationDialog).isNotNull();
        confirmationDialog.getButtons().get(0).click();

        assertThat(systemAuthenticator.withUser("admin", () -> dataManager.load(Partner.class)
                .id(existing.getId())
                .optional()))
                .isEmpty();
        cleanupIds.remove(existing.getId());
    }

    @AfterEach
    void tearDown() {
        cleanupIds.forEach(id -> systemAuthenticator.withUser("admin", () -> {
            dataManager.load(Partner.class)
                    .id(id)
                    .optional()
                    .ifPresent(dataManager::remove);
            return null;
        }));
        cleanupIds.clear();
    }

    private Partner loadPartnerByLastName(String lastName) {
        return systemAuthenticator.withUser("admin", () -> dataManager.load(Partner.class)
                .query("e.lastName = ?1", lastName)
                .one());
    }

    private List<Partner> gridItems(DataGrid<Partner> dataGrid) {
        DataGridItems<Partner> items = dataGrid.getItems();
        assertThat(items).isNotNull();
        return items.getItems().stream().toList();
    }
}
