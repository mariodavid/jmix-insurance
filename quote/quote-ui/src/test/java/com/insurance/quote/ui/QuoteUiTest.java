package com.insurance.quote.ui;

import static com.insurance.quote.core.test_support.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.insurance.common.test_support.EntityTestData;
import com.insurance.common.test_support_ui.DataGridInteractions;
import com.insurance.common.test_support_ui.FormInteractions;
import com.insurance.common.test_support_ui.UiTestSupport;
import com.insurance.common.test_support_ui.ViewInteractions;
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
import com.vaadin.flow.data.provider.Query;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.notification.NotificationInfo;
import io.jmix.flowui.testassist.notification.OpenedNotifications;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@UiTest
@SpringBootTest(
    classes = {QuoteUiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
    properties = "spring.main.allow-bean-definition-overriding=true")
class QuoteUiTest {

  @Autowired private ViewNavigators viewNavigators;

  @Autowired private DataManager dataManager;

  @Autowired private EntityTestData entityTestData;

  @Autowired private PolicyService policyService;

  @Autowired private PartnerService partnerService;

  @Autowired private OpenedNotifications openedNotifications;

  @Autowired private DataSource dataSource;

  @Autowired private Metadata metadata;

  @Autowired private MetadataTools metadataTools;

  @Autowired private Messages messages;

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
  void
      given_partnerExists_when_quoteCreatedThroughUi_then_quoteIsSavedWithPendingStatusAndDefaults() {
    // given
    PartnerDto partner = dataManager.create(PartnerDto.class);
    partner.setPartnerNo("PT-00001");
    partner.setFirstName("Max");
    partner.setLastName("Mustermann");
    when(partnerService.findPartners(anyString(), anyInt(), anyInt())).thenReturn(List.of(partner));
    when(partnerService.getPartner("PT-00001")).thenReturn(partner);

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    QuoteListView listView = viewInteractions.navigate(QuoteListView.class);
    JmixButton createButton = UiTestSupport.findButtonByText(listView, "Create");
    assertThat(createButton).isNotNull();
    createButton.click();

    // when
    QuoteDetailView detailView = viewInteractions.findOpenView(QuoteDetailView.class);

    FormInteractions form = FormInteractions.of(detailView);
    form.setFieldValueByLabel("Partner", partner);
    form.setFieldValueByLabel("Product Type", ProductType.HOME_CONTENT);
    form.setFieldValueByLabel("Product Variant", ProductVariant.SMALL);
    form.setFieldValueByLabel("Payment Frequency", PaymentFrequency.YEARLY);
    form.setFieldValueByLabel("Effective Date", LocalDate.of(2025, 1, 1));
    form.setFieldValueByLabel("Square Meters", 60);

    JmixButton calculateButton = UiTestSupport.findButtonByText(detailView, "Calculate Premium");
    assertThat(calculateButton).isNotNull();
    calculateButton.click();

    JmixButton saveAndCloseButton = UiTestSupport.findButtonByText(detailView, "OK");
    assertThat(saveAndCloseButton).isNotNull();
    saveAndCloseButton.click();

    // then
    Quote saved = dataManager.load(Quote.class).all().list().stream().findFirst().orElseThrow();
    assertThat(saved).hasStatus(QuoteStatus.PENDING);
    assertThat(saved.getValidFrom()).isNotNull();
    assertThat(saved.getValidUntil()).isNotNull();
    assertThat(saved.getValidFrom()).isBefore(saved.getValidUntil());

    QuoteListView navigatedListView = viewInteractions.findOpenView(QuoteListView.class);
    DataGridInteractions<Quote> quotesDataGrid =
        DataGridInteractions.of(navigatedListView, Quote.class, "quotesDataGrid");
    assertThat(quotesDataGrid.items())
        .anySatisfy(q -> assertThat(q.getId()).isEqualTo(saved.getId()));
  }

  @Test
  void given_quoteDetailView_when_premiumCalculated_then_calculatedPremiumIsSetAndSaveEnabled() {
    // given
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    QuoteListView listView = viewInteractions.navigate(QuoteListView.class);
    JmixButton createButton = UiTestSupport.findButtonByText(listView, "Create");
    assertThat(createButton).isNotNull();
    createButton.click();

    QuoteDetailView detailView = viewInteractions.findOpenView(QuoteDetailView.class);

    FormInteractions form = FormInteractions.of(detailView);
    form.setFieldValueByLabel("Product Type", ProductType.HOME_CONTENT);
    form.setFieldValueByLabel("Product Variant", ProductVariant.SMALL);
    form.setFieldValueByLabel("Effective Date", LocalDate.of(2025, 1, 1));
    form.setFieldValueByLabel("Square Meters", 60);

    // when
    JmixButton calculateButton = UiTestSupport.findButtonByText(detailView, "Calculate Premium");
    assertThat(calculateButton).isNotNull();
    calculateButton.click();

    // then
    JmixButton saveAndCloseButton = UiTestSupport.findButtonByText(detailView, "OK");
    assertThat(saveAndCloseButton).isNotNull();
    assertThat(saveAndCloseButton.isEnabled()).isTrue();

    Object premiumVal = form.getFieldValueByLabel("Calculated Premium");
    assertThat(premiumVal).isNotNull();
    assertThat(((BigDecimal) premiumVal).compareTo(BigDecimal.ZERO)).isGreaterThan(0);
  }

  @Test
  void given_quoteDetailView_when_noMatchingProductFound_then_saveRemainsDisabled() {
    // given
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    QuoteListView listView = viewInteractions.navigate(QuoteListView.class);
    JmixButton createButton = UiTestSupport.findButtonByText(listView, "Create");
    assertThat(createButton).isNotNull();
    createButton.click();

    QuoteDetailView detailView = viewInteractions.findOpenView(QuoteDetailView.class);

    FormInteractions form = FormInteractions.of(detailView);
    form.setFieldValueByLabel("Product Type", ProductType.HOME_CONTENT);
    form.setFieldValueByLabel("Product Variant", ProductVariant.SMALL);
    form.setFieldValueByLabel("Effective Date", LocalDate.of(2000, 1, 1));
    form.setFieldValueByLabel("Square Meters", 60);

    // when
    JmixButton calculateButton = UiTestSupport.findButtonByText(detailView, "Calculate Premium");
    assertThat(calculateButton).isNotNull();
    calculateButton.click();

    // then
    JmixButton saveAndCloseButton = UiTestSupport.findButtonByText(detailView, "OK");
    assertThat(saveAndCloseButton).isNotNull();
    assertThat(saveAndCloseButton.isEnabled()).isFalse();

    Object premiumVal = form.getFieldValueByLabel("Calculated Premium");
    assertThat(premiumVal).isNull();

    NotificationInfo notification = openedNotifications.getLastNotification();
    assertThat(notification).isNotNull();
    assertThat(notification.getType()).isEqualTo(Notifications.Type.ERROR);
    assertThat(notification.getText()).contains("No matching active product found");
  }

  @Test
  void given_pendingQuote_when_acceptedViaDataGrid_then_quoteIsAcceptedAndGridReloads() {
    // given
    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());

    PolicyDto policyDto = dataManager.create(PolicyDto.class);
    policyDto.setId(UUID.randomUUID());
    policyDto.setPolicyNo("HC-2025-000001");
    when(policyService.createPolicy(any())).thenReturn(policyDto);

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    QuoteListView listView = viewInteractions.navigate(QuoteListView.class);

    DataGridInteractions<Quote> quotesDataGrid =
        DataGridInteractions.of(listView, Quote.class, "quotesDataGrid");
    Quote gridQuote =
        quotesDataGrid.items().stream()
            .filter(q -> q.getId().equals(quote.getId()))
            .findFirst()
            .orElseThrow();
    quotesDataGrid.select(gridQuote);

    // when
    String acceptLabel = "Accept";
    JmixButton acceptButton = UiTestSupport.findButtonByText(listView, acceptLabel);
    assertThat(acceptButton).isNotNull();
    acceptButton.click();

    // then
    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    assertThat(reloaded).hasStatus(QuoteStatus.ACCEPTED);
    assertThat(reloaded.getAcceptedAt()).isNotNull();
    assertThat(reloaded.getCreatedPolicyNo()).isEqualTo("HC-2025-000001");

    DataGridInteractions<Quote> reloadedGrid =
        DataGridInteractions.of(
            (QuoteListView) viewInteractions.findOpenView(QuoteListView.class),
            Quote.class,
            "quotesDataGrid");
    assertThat(reloadedGrid.items()).isNotEmpty();
  }

  @Test
  void given_pendingQuote_when_rejectedViaDataGrid_then_quoteIsRejectedAndGridReloads() {
    // given
    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    QuoteListView listView = viewInteractions.navigate(QuoteListView.class);

    DataGridInteractions<Quote> quotesDataGrid =
        DataGridInteractions.of(listView, Quote.class, "quotesDataGrid");
    Quote gridQuote =
        quotesDataGrid.items().stream()
            .filter(q -> q.getId().equals(quote.getId()))
            .findFirst()
            .orElseThrow();
    quotesDataGrid.select(gridQuote);

    // when
    String rejectLabel = "Reject";
    JmixButton rejectButton = UiTestSupport.findButtonByText(listView, rejectLabel);
    assertThat(rejectButton).isNotNull();
    rejectButton.click();

    // then
    Quote reloaded = dataManager.load(Quote.class).id(quote.getId()).one();
    assertThat(reloaded).hasStatus(QuoteStatus.REJECTED);
    assertThat(reloaded.getRejectedAt()).isNotNull();
    assertThat(reloaded.getAcceptedAt()).isNull();
    assertThat(reloaded.getCreatedPolicyNo()).isNull();
    verify(policyService, never()).createPolicy(any());

    DataGridInteractions<Quote> reloadedGrid =
        DataGridInteractions.of(
            (QuoteListView) viewInteractions.findOpenView(QuoteListView.class),
            Quote.class,
            "quotesDataGrid");
    assertThat(reloadedGrid.items()).isNotEmpty();
  }

  @Test
  void given_quoteListView_when_acceptedQuoteSelected_then_acceptAndRejectButtonsAreDisabled() {
    // given
    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());
    quote.setStatus(QuoteStatus.ACCEPTED);
    quote.setCreatedPolicyNo("HC-2025-000001");
    Quote acceptedQuote = dataManager.save(quote);

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    QuoteListView listView = viewInteractions.navigate(QuoteListView.class);

    DataGridInteractions<Quote> quotesDataGrid =
        DataGridInteractions.of(listView, Quote.class, "quotesDataGrid");
    Quote gridQuote =
        quotesDataGrid.items().stream()
            .filter(q -> q.getId().equals(acceptedQuote.getId()))
            .findFirst()
            .orElseThrow();

    // when
    quotesDataGrid.select(gridQuote);
    String acceptLabel = "Accept";
    String rejectLabel = "Reject";
    JmixButton acceptButton = UiTestSupport.findButtonByText(listView, acceptLabel);
    JmixButton rejectButton = UiTestSupport.findButtonByText(listView, rejectLabel);
    assertThat(acceptButton).isNotNull();
    assertThat(rejectButton).isNotNull();
    assertThat(acceptButton.isEnabled()).isFalse();
    assertThat(rejectButton.isEnabled()).isFalse();
  }

  @Test
  void given_pendingQuote_when_accepted_then_successNotificationContainsFormattedPolicyNo() {
    // given
    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());
    quote.setStatus(QuoteStatus.PENDING);
    quote.setCalculatedPremium(BigDecimal.valueOf(100));
    Quote pendingQuote = dataManager.save(quote);

    PolicyDto policyDto = dataManager.create(PolicyDto.class);
    policyDto.setId(UUID.randomUUID());
    policyDto.setPolicyNo("HC-2025-000001");
    when(policyService.createPolicy(any())).thenReturn(policyDto);

    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    QuoteListView listView = viewInteractions.navigate(QuoteListView.class);

    DataGridInteractions<Quote> quotesDataGrid =
        DataGridInteractions.of(listView, Quote.class, "quotesDataGrid");
    Quote gridQuote =
        quotesDataGrid.items().stream()
            .filter(q -> q.getId().equals(pendingQuote.getId()))
            .findFirst()
            .orElseThrow();

    // check enabled first
    quotesDataGrid.select(gridQuote);
    String acceptLabel = "Accept";
    String rejectLabel = "Reject";
    JmixButton acceptButton = UiTestSupport.findButtonByText(listView, acceptLabel);
    JmixButton rejectButton = UiTestSupport.findButtonByText(listView, rejectLabel);
    assertThat(acceptButton).isNotNull();
    assertThat(rejectButton).isNotNull();
    assertThat(acceptButton.isEnabled()).isTrue();
    assertThat(rejectButton.isEnabled()).isTrue();

    // when
    acceptButton.click();

    // then
    NotificationInfo notification = openedNotifications.getLastNotification();
    assertThat(notification).isNotNull();
    assertThat(notification.getType()).isEqualTo(Notifications.Type.SUCCESS);
    assertThat(notification.getTitle()).isEqualTo("Quote accepted");
    assertThat(notification.getMessage()).isEqualTo("Policy issued: HC-2025-000001");
  }

  @Test
  void given_existingQuoteWithPartner_when_edited_then_partnerComboBoxIsPrepopulated() {
    // given
    PartnerDto partner = dataManager.create(PartnerDto.class);
    partner.setPartnerNo("PT-00001");
    partner.setFirstName("Max");
    partner.setLastName("Mustermann");
    when(partnerService.getPartner("PT-00001")).thenReturn(partner);

    Quote quote = entityTestData.saveWithDefaults(new QuoteDataProvider());
    quote.setPartnerNo("PT-00001");
    Quote reloaded = dataManager.save(quote);

    // when
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    viewNavigators
        .detailView(UiTestUtils.getCurrentView(), Quote.class)
        .editEntity(reloaded)
        .navigate();

    // then
    QuoteDetailView detailView = viewInteractions.findOpenView(QuoteDetailView.class);
    FormInteractions form = FormInteractions.of(detailView);
    EntityComboBox<PartnerDto> partnerComboBox =
        form.entityComboBoxField("partnerComboBox", PartnerDto.class);
    assertThat(partnerComboBox.getValue()).isNotNull();
    assertThat(partnerComboBox.getValue().getPartnerNo()).isEqualTo("PT-00001");
  }

  @Test
  void given_quoteDetailView_when_partnerCleared_then_partnerNoIsSetToNull() {
    // given
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    QuoteListView listView = viewInteractions.navigate(QuoteListView.class);
    JmixButton createButton = UiTestSupport.findButtonByText(listView, "Create");
    assertThat(createButton).isNotNull();
    createButton.click();

    QuoteDetailView detailView = viewInteractions.findOpenView(QuoteDetailView.class);
    FormInteractions form = FormInteractions.of(detailView);
    EntityComboBox<PartnerDto> partnerComboBox =
        form.entityComboBoxField("partnerComboBox", PartnerDto.class);

    PartnerDto partner = dataManager.create(PartnerDto.class);
    partner.setPartnerNo("PT-00001");
    partnerComboBox.setValue(partner);
    assertThat(detailView.getEditedEntity().getPartnerNo()).isEqualTo("PT-00001");

    // when
    partnerComboBox.setValue(null);

    // then
    assertThat(detailView.getEditedEntity().getPartnerNo()).isNull();
  }

  @SuppressWarnings("unchecked")
  @Test
  void given_quoteDetailView_when_partnerComboBoxFiltered_then_fetchCallbackIsInvoked() {
    // given
    viewNavigators.view(UiTestUtils.getCurrentView(), QuoteListView.class).navigate();
    QuoteListView listView = UiTestUtils.getCurrentView();
    JmixButton createButton = UiTestSupport.findButtonByText(listView, "Create");
    assertThat(createButton).isNotNull();
    createButton.click();

    QuoteDetailView detailView = UiTestUtils.getCurrentView();

    PartnerDto partner = dataManager.create(PartnerDto.class);
    partner.setPartnerNo("PT-00001");
    partner.setFirstName("Max");
    partner.setLastName("Mustermann");
    when(partnerService.findPartners("Max", 10, 0)).thenReturn(List.of(partner));

    // when
    Query<PartnerDto, String> query = new Query<>(0, 10, List.of(), null, "Max");
    Object callbackResult =
        ReflectionTestUtils.invokeMethod(detailView, "partnerComboBoxItemsFetchCallback", query);
    if (callbackResult == null) {
      throw new AssertionError("Partner combo box fetch callback returned null");
    }
    Stream<PartnerDto> resultStream = (Stream<PartnerDto>) callbackResult;

    // then
    List<PartnerDto> result = resultStream.toList();
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getPartnerNo()).isEqualTo("PT-00001");
  }

  private <T> void deleteAll(Class<T> entityClass) {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    String table = metadataTools.getDatabaseTable(metadata.getClass(entityClass));
    jdbc.update("DELETE FROM " + table);
  }
}
