package com.insurance.quote.ui.view.quote;

import com.insurance.partner.api.dto.PartnerDto;
import com.insurance.partner.api.service.PartnerService;
import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.core.entity.Quote;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import io.jmix.core.TimeSource;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "quotes/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "quote_Quote.detail")
@ViewDescriptor(path = "quote-detail-view.xml")
@EditedEntityContainer("quoteDc")
@CssImport("./quote/styles.css")
public class QuoteDetailView extends StandardDetailView<Quote> {

  @Autowired private TimeSource timeSource;

  @Autowired private Notifications notifications;

  @ViewComponent private MessageBundle messageBundle;

  @ViewComponent private JmixButton saveAndCloseButton;

  @ViewComponent private EntityComboBox<PartnerDto> partnerComboBox;

  @ViewComponent private TextField firstNameField;

  @ViewComponent private TextField lastNameField;

  @ViewComponent private Action calculatePremiumAction;

  @Autowired private PartnerService partnerService;

  @Subscribe
  public void onBeforeShow(final BeforeShowEvent event) {
    Quote quote = getEditedEntity();

    if (quote.getPartnerNo() != null) {
      PartnerDto partnerDto = partnerService.getPartner(quote.getPartnerNo());
      if (partnerDto != null) {
        partnerComboBox.setValue(partnerDto);
        firstNameField.setValue(partnerDto.getFirstName() != null ? partnerDto.getFirstName() : "");
        lastNameField.setValue(partnerDto.getLastName() != null ? partnerDto.getLastName() : "");
      }
    }

    if (quote.getStatus() != QuoteStatus.PENDING) {
      setReadOnly(true);
      partnerComboBox.setReadOnly(true);
      calculatePremiumAction.setEnabled(false);
    } else if (quote.getCalculatedPremium() != null) {
      saveAndCloseButton.setEnabled(true);
    }
  }

  @Subscribe
  public void onInitEntity(final InitEntityEvent<Quote> event) {
    Quote quote = event.getEntity();
    LocalDate today = timeSource.now().toLocalDate();
    quote.setStatus(QuoteStatus.PENDING);
    quote.setValidFrom(today);
    quote.setValidUntil(today.plusDays(14));
  }

  @Subscribe("calculatePremiumAction")
  public void onCalculatePremiumAction(final ActionPerformedEvent event) {
    saveAndCloseButton.setEnabled(false);

    Quote quote = getEditedEntity();
    if (quote.getProductType() == null
        || quote.getProductVariant() == null
        || quote.getEffectiveDate() == null
        || quote.getSquareMeters() == null) {
      notifications
          .create(messageBundle.getMessage("missingFieldsForCalculation"))
          .withType(Notifications.Type.WARNING)
          .show();
      return;
    }

    Optional<InsuranceProduct> matchingProduct =
        InsuranceProduct.findFirstMatchingProduct(
            quote.getProductType(), quote.getProductVariant(), quote.getEffectiveDate());

    if (matchingProduct.isEmpty()) {
      notifications
          .create(messageBundle.getMessage("noMatchingProduct"))
          .withType(Notifications.Type.ERROR)
          .show();
      return;
    }

    InsuranceProduct insuranceProduct = matchingProduct.get();
    quote.setInsuranceProduct(insuranceProduct);
    BigDecimal calculatedPremium =
        insuranceProduct.calculatePremium(BigDecimal.valueOf(quote.getSquareMeters()));
    quote.setCalculatedPremium(calculatedPremium);

    saveAndCloseButton.setEnabled(true);
  }

  @Subscribe(id = "quoteDc", target = Target.DATA_CONTAINER)
  public void onQuoteDcItemPropertyChange(
      final InstanceContainer.ItemPropertyChangeEvent<Quote> event) {
    if (java.util.List.of("productType", "productVariant", "effectiveDate", "squareMeters")
        .contains(event.getProperty())) {
      saveAndCloseButton.setEnabled(false);
    }
  }

  @Install(to = "partnerComboBox", subject = "itemsFetchCallback")
  private Stream<PartnerDto> partnerComboBoxItemsFetchCallback(
      final Query<PartnerDto, String> query) {
    String filter = query.getFilter().orElse("");
    int limit = query.getLimit();
    int offset = query.getOffset();
    return partnerService.findPartners(filter, limit, offset).stream();
  }

  @Subscribe("partnerComboBox")
  public void onPartnerComboBoxComponentValueChange(
      final AbstractField.ComponentValueChangeEvent<EntityComboBox<PartnerDto>, PartnerDto> event) {
    PartnerDto value = event.getValue();
    if (value != null) {
      getEditedEntity().setPartnerNo(value.getPartnerNo());
      firstNameField.setValue(value.getFirstName() != null ? value.getFirstName() : "");
      lastNameField.setValue(value.getLastName() != null ? value.getLastName() : "");
    } else {
      getEditedEntity().setPartnerNo(null);
      firstNameField.setValue("");
      lastNameField.setValue("");
    }
  }
}
