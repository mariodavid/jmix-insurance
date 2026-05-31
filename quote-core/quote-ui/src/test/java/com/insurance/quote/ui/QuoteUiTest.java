package com.insurance.quote.ui;

import com.insurance.common.test_support.EntityTestData;
import com.insurance.partner.api.dto.PartnerDto;
import com.insurance.partner.api.service.PartnerService;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.ProductType;
import com.insurance.product.api.dto.ProductVariant;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.core.entity.Quote;
import com.insurance.quote.core.test_support.QuoteDataProvider;
import com.insurance.quote.ui.view.quote.QuoteDetailView;
import com.insurance.quote.ui.view.quote.QuoteListView;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.JmixIntegerField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.notification.NotificationInfo;
import io.jmix.flowui.testassist.notification.OpenedNotifications;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.insurance.quote.core.test_support.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UiTest
@SpringBootTest(classes = {QuoteUiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
        properties = "spring.main.allow-bean-definition-overriding=true")
class QuoteUiTest {

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntityTestData entityTestData;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private OpenedNotifications openedNotifications;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    @BeforeEach
    void setUp() {
        reset(policyService, partnerService);
        deleteAll(Quote.class);
    }

    @AfterEach
    void tearDown() {
        deleteAll(Quote.class);
    }

    @Test
    void given_partnerExists_when_quoteCreatedThroughUi_then_quoteIsSavedWithPendingStatusAndDefaults() {
        // given
        PartnerDto partner = dataManager.create(PartnerDto.class);
        partner.setPartnerNo("PT-00001");
        partner.setFirstName("Max");
        partner.setLastName("Mustermann");
        when(partnerService.findPartners(anyString(), anyInt(), anyInt())).thenReturn(List.of(partner));
        when(partnerService.getPartner("PT-00001")).thenReturn(partner);

        viewNavigators.view(UiTestUtils.getCurrentView(), QuoteListView.class).navigate();
        QuoteListView listView = UiTestUtils.getCurrentView();
        JmixButton createButton = UiTestUtils.getComponent(listView, "createButton");
        createButton.click();

        // when
        QuoteDetailView detailView = UiTestUtils.getCurrentView();

        EntityComboBox<PartnerDto> partnerComboBox = UiTestUtils.getComponent(detailView, "partnerComboBox");
        partnerComboBox.setValue(partner);

        JmixSelect<ProductType> productTypeField = UiTestUtils.getComponent(detailView, "productTypeField");
        productTypeField.setValue(ProductType.HOME_CONTENT);

        JmixSelect<ProductVariant> productVariantField = UiTestUtils.getComponent(detailView, "productVariantField");
        productVariantField.setValue(ProductVariant.SMALL);

        JmixSelect<PaymentFrequency> paymentFrequencyField = UiTestUtils.getComponent(detailView, "paymentFrequencyField");
        paymentFrequencyField.setValue(PaymentFrequency.YEARLY);

        TypedDatePicker<LocalDate> effectiveDateField = UiTestUtils.getComponent(detailView, "effectiveDateField");
        effectiveDateField.setValue(LocalDate.of(2025, 1, 1));

        JmixIntegerField squareMetersField = UiTestUtils.getComponent(detailView, "squareMetersField");
        squareMetersField.setValue(60);

        JmixButton calculateButton = UiTestUtils.getComponent(detailView, "calculatePremium");
        calculateButton.click();

        JmixButton saveAndCloseButton = UiTestUtils.getComponent(detailView, "saveAndCloseButton");
        saveAndCloseButton.click();

        // then
        Quote saved = dataManager.load(Quote.class).all().list().stream().findFirst().orElseThrow();
        assertThat(saved).hasStatus(QuoteStatus.PENDING);
        assertThat(saved.getValidFrom()).isNotNull();
        assertThat(saved.getValidUntil()).isNotNull();
        assertThat(saved.getValidFrom()).isBefore(saved.getValidUntil());

        QuoteListView navigatedListView = UiTestUtils.getCurrentView();
        DataGrid<Quote> quotesDataGrid = UiTestUtils.getComponent(navigatedListView, "quotesDataGrid");
        assertThat(gridItems(quotesDataGrid)).anySatisfy(q -> assertThat(q.getId()).isEqualTo(saved.getId()));
    }

    @Test
    void given_quoteDetailView_when_premiumCalculated_then_calculatedPremiumIsSetAndSaveEnabled() {
        // given
        viewNavigators.view(UiTestUtils.getCurrentView(), QuoteListView.class).navigate();
        QuoteListView listView = UiTestUtils.getCurrentView();
        JmixButton createButton = UiTestUtils.getComponent(listView, "createButton");
        createButton.click();

        QuoteDetailView detailView = UiTestUtils.getCurrentView();

        JmixSelect<ProductType> productTypeField = UiTestUtils.getComponent(detailView, "productTypeField");
        productTypeField.setValue(ProductType.HOME_CONTENT);

        JmixSelect<ProductVariant> productVariantField = UiTestUtils.getComponent(detailView, "productVariantField");
        productVariantField.setValue(ProductVariant.SMALL);

        TypedDatePicker<LocalDate> effectiveDateField = UiTestUtils.getComponent(detailView, "effectiveDateField");
        effectiveDateField.setValue(LocalDate.of(2025, 1, 1));

        JmixIntegerField squareMetersField = UiTestUtils.getComponent(detailView, "squareMetersField");
        squareMetersField.setValue(60);

        // when
        JmixButton calculateButton = UiTestUtils.getComponent(detailView, "calculatePremium");
        calculateButton.click();

        // then
        JmixButton saveAndCloseButton = UiTestUtils.getComponent(detailView, "saveAndCloseButton");
        assertThat(saveAndCloseButton.isEnabled()).isTrue();

        TypedTextField<BigDecimal> premiumField = UiTestUtils.getComponent(detailView, "calculatedPremiumField");
        assertThat(premiumField.getTypedValue()).isNotNull();
        assertThat(premiumField.getTypedValue().compareTo(BigDecimal.ZERO)).isGreaterThan(0);
    }

    @Test
    void given_quoteDetailView_when_noMatchingProductFound_then_saveRemainsDisabled() {
        // given
        viewNavigators.view(UiTestUtils.getCurrentView(), QuoteListView.class).navigate();
        QuoteListView listView = UiTestUtils.getCurrentView();
        JmixButton createButton = UiTestUtils.getComponent(listView, "createButton");
        createButton.click();

        QuoteDetailView detailView = UiTestUtils.getCurrentView();

        JmixSelect<ProductType> productTypeField = UiTestUtils.getComponent(detailView, "productTypeField");
        productTypeField.setValue(ProductType.HOME_CONTENT);

        JmixSelect<ProductVariant> productVariantField = UiTestUtils.getComponent(detailView, "productVariantField");
        productVariantField.setValue(ProductVariant.SMALL);

        // Datum vor gültigem Produktzeitraum — kein Produkt gefunden
        TypedDatePicker<LocalDate> effectiveDateField = UiTestUtils.getComponent(detailView, "effectiveDateField");
        effectiveDateField.setValue(LocalDate.of(2000, 1, 1));

        JmixIntegerField squareMetersField = UiTestUtils.getComponent(detailView, "squareMetersField");
        squareMetersField.setValue(60);

        // when
        JmixButton calculateButton = UiTestUtils.getComponent(detailView, "calculatePremium");
        calculateButton.click();

        // then
        JmixButton saveAndCloseButton = UiTestUtils.getComponent(detailView, "saveAndCloseButton");
        assertThat(saveAndCloseButton.isEnabled()).isFalse();

        TypedTextField<BigDecimal> premiumField = UiTestUtils.getComponent(detailView, "calculatedPremiumField");
        assertThat(premiumField.getTypedValue()).isNull();

        NotificationInfo notification = openedNotifications.getLastNotification();
        assertThat(notification).isNotNull();
        assertThat(notification.getType()).isEqualTo(Notifications.Type.ERROR);
        assertThat(notification.getText())
                .contains("No matching active product found");
    }

    @Test
    void given_pendingQuote_when_acceptedViaDataGrid_then_quoteIsAcceptedAndGridReloads() {
        // given
        Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());

        PolicyDto policyDto = dataManager.create(PolicyDto.class);
        policyDto.setId(UUID.randomUUID());
        policyDto.setPolicyNo("HC-2025-000001");
        when(policyService.createPolicy(any())).thenReturn(policyDto);

        viewNavigators.view(UiTestUtils.getCurrentView(), QuoteListView.class).navigate();
        QuoteListView listView = UiTestUtils.getCurrentView();

        DataGrid<Quote> quotesDataGrid = UiTestUtils.getComponent(listView, "quotesDataGrid");
        Quote gridQuote = gridItems(quotesDataGrid).stream()
                .filter(q -> q.getId().equals(quote.getId()))
                .findFirst()
                .orElseThrow();
        quotesDataGrid.select(gridQuote);

        // when
        JmixButton acceptButton = UiTestUtils.getComponent(listView, "acceptButton");
        acceptButton.click();

        // then
        Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
        assertThat(reloaded).hasStatus(QuoteStatus.ACCEPTED);
        assertThat(reloaded.getAcceptedAt()).isNotNull();
        assertThat(reloaded.getCreatedPolicyNo()).isEqualTo("HC-2025-000001");

        DataGrid<Quote> reloadedGrid = UiTestUtils.getComponent(
                (QuoteListView) UiTestUtils.getCurrentView(), "quotesDataGrid");
        assertThat(reloadedGrid.getItems()).isNotNull();
    }

    @Test
    void given_pendingQuote_when_rejectedViaDataGrid_then_quoteIsRejectedAndGridReloads() {
        // given
        Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());

        viewNavigators.view(UiTestUtils.getCurrentView(), QuoteListView.class).navigate();
        QuoteListView listView = UiTestUtils.getCurrentView();

        DataGrid<Quote> quotesDataGrid = UiTestUtils.getComponent(listView, "quotesDataGrid");
        Quote gridQuote = gridItems(quotesDataGrid).stream()
                .filter(q -> q.getId().equals(quote.getId()))
                .findFirst()
                .orElseThrow();
        quotesDataGrid.select(gridQuote);

        // when
        JmixButton rejectButton = UiTestUtils.getComponent(listView, "rejectButton");
        rejectButton.click();

        // then
        Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
        assertThat(reloaded).hasStatus(QuoteStatus.REJECTED);
        assertThat(reloaded.getRejectedAt()).isNotNull();
        assertThat(reloaded.getAcceptedAt()).isNull();
        assertThat(reloaded.getCreatedPolicyNo()).isNull();
        verify(policyService, never()).createPolicy(any());

        DataGrid<Quote> reloadedGrid = UiTestUtils.getComponent(
                (QuoteListView) UiTestUtils.getCurrentView(), "quotesDataGrid");
        assertThat(reloadedGrid.getItems()).isNotNull();
    }

    private List<Quote> gridItems(DataGrid<Quote> dataGrid) {
        DataGridItems<Quote> items = dataGrid.getItems();
        assertThat(items).isNotNull();
        return items.getItems().stream().toList();
    }

    private <T> void deleteAll(Class<T> entityClass) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String table = metadataTools.getDatabaseTable(metadata.getClass(entityClass));
        jdbc.update("DELETE FROM " + table);
    }
}
