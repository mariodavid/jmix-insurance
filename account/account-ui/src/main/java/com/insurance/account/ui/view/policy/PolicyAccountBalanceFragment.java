package com.insurance.account.ui.view.policy;

import com.insurance.account.api.service.AccountService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.core.Messages;
import io.jmix.core.TimeSource;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("PMD.GuardLogStatement")
@FragmentDescriptor("policy-account-balance-fragment.xml")
public class PolicyAccountBalanceFragment extends Fragment<VerticalLayout> {

  private static final Logger log = LoggerFactory.getLogger(PolicyAccountBalanceFragment.class);

  @ViewComponent private TextField accountBalanceResult;

  @ViewComponent private TypedDatePicker<LocalDate> accountBalanceEffectiveDatePicker;

  private final AccountService accountService;
  private final TimeSource timeSource;
  private final Notifications notifications;
  private final Messages messages;

  @Autowired
  public PolicyAccountBalanceFragment(
      AccountService accountService,
      TimeSource timeSource,
      Notifications notifications,
      Messages messages) {
    this.accountService = accountService;
    this.timeSource = timeSource;
    this.notifications = notifications;
    this.messages = messages;
  }

  private String policyNo;

  public void setPolicyNo(String policyNo) {
    this.policyNo = policyNo;
    accountBalanceEffectiveDatePicker.setValue(timeSource.now().toLocalDate());
  }

  @Subscribe("accountBalanceEffectiveDatePicker")
  public void onAccountBalanceEffectiveDatePickerComponentValueChange(
      final AbstractField.ComponentValueChangeEvent<TypedDatePicker<LocalDate>, LocalDate> event) {
    updateBalance(event.getValue());
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void updateBalance(LocalDate effectiveDate) {
    if (policyNo == null || policyNo.isBlank() || effectiveDate == null) {
      accountBalanceResult.clear();
      return;
    }

    try {
      BigDecimal balance = accountService.getAccountBalance(policyNo, effectiveDate);

      if (balance != null) {
        accountBalanceResult.setValue(balance.toString());
      } else {
        notifications
            .create(messages.getMessage(getClass(), "noAccountBalance"))
            .withType(Notifications.Type.WARNING)
            .withPosition(Notification.Position.TOP_END)
            .show();
      }
    } catch (Exception e) {
      log.error("Could not calculate account balance: {}", e.getMessage(), e);
      notifications
          .create(messages.getMessage(getClass(), "balanceCalculationFailed"))
          .withType(Notifications.Type.ERROR)
          .withPosition(Notification.Position.TOP_END)
          .show();
    }
  }
}
