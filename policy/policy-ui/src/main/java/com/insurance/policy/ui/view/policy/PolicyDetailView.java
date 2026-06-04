package com.insurance.policy.ui.view.policy;

import com.insurance.account.api.service.AccountService;
import com.insurance.partner.api.dto.PartnerDto;
import com.insurance.partner.api.service.PartnerService;
import com.insurance.policy.core.entity.Policy;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "policies/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "policy_Policy.detail")
@ViewDescriptor(path = "policy-detail-view.xml")
@EditedEntityContainer("policyDc")
public class PolicyDetailView extends StandardDetailView<Policy> {

  private static final Logger log = LoggerFactory.getLogger(PolicyDetailView.class);

  @Autowired private AccountService accountService;

  @ViewComponent private TextField accountBalanceResult;

  @ViewComponent private TypedDatePicker<LocalDate> accountBalanceEffectiveDatePicker;

  @Autowired private TimeSource timeSource;

  @Autowired private PartnerService partnerService;

  @Autowired private Metadata metadata;

  @ViewComponent private InstanceContainer<PartnerDto> partnerDc;

  @Autowired private Notifications notifications;

  @ViewComponent private MessageBundle messageBundle;

  @Subscribe
  public void onReady(final ReadyEvent event) {
    accountBalanceEffectiveDatePicker.setValue(timeSource.now().toLocalDate());

    PartnerDto partnerDto = partnerService.getPartner(getEditedEntity().getPartnerNo());

    if (partnerDto != null) {
      partnerDc.setItem(partnerDto);
    } else {
      notifications
          .create(messageBundle.getMessage("noPartner"))
          .withType(Notifications.Type.WARNING)
          .withPosition(Notification.Position.TOP_END)
          .show();
    }
  }

  @Subscribe("accountBalanceEffectiveDatePicker")
  public void onAccountBalanceEffectiveDatePickerComponentValueChange(
      final AbstractField.ComponentValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {
    try {
      BigDecimal balance =
          accountService.getAccountBalance(getEditedEntity().getPolicyNo(), event.getValue());

      if (balance != null) {
        accountBalanceResult.setValue(balance.toString());
      } else {
        notifications
            .create(messageBundle.getMessage("noAccountBalance"))
            .withType(Notifications.Type.WARNING)
            .withPosition(Notification.Position.TOP_END)
            .show();
      }
    } catch (Exception e) {
      log.error("Could not calculate account balance: {}", e.getMessage(), e);
      notifications
          .create(messageBundle.getMessage("balanceCalculationFailed"))
          .withType(Notifications.Type.ERROR)
          .withPosition(Notification.Position.TOP_END)
          .show();
    }
  }
}
