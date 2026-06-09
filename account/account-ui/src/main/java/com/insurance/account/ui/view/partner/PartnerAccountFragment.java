package com.insurance.account.ui.view.partner;

import com.insurance.account.api.dto.PartnerAccountSummaryDto;
import com.insurance.account.api.service.PartnerAccountOverviewService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewComponent;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("partner-account-fragment.xml")
public class PartnerAccountFragment extends Fragment<VerticalLayout> {

  private static final String STYLE_TEXT_DANGER = "text-danger";

  @ViewComponent private TextField accountNoField;

  @ViewComponent private TextField balanceField;

  private final PartnerAccountOverviewService partnerAccountOverviewService;

  @Autowired
  public PartnerAccountFragment(PartnerAccountOverviewService partnerAccountOverviewService) {
    this.partnerAccountOverviewService = partnerAccountOverviewService;
  }

  public void setPartnerNo(String partnerNo) {
    if (partnerNo == null || partnerNo.isBlank()) {
      showNoAccountSummary();
      return;
    }

    partnerAccountOverviewService
        .findAccountSummaryForPartner(partnerNo)
        .ifPresentOrElse(this::showAccountSummary, this::showNoAccountSummary);
  }

  private void showAccountSummary(PartnerAccountSummaryDto accountSummary) {
    accountNoField.setValue(
        accountSummary.getAccountNo() != null ? accountSummary.getAccountNo() : "");
    BigDecimal balance = accountSummary.getAccountBalance();
    if (balance == null) {
      balanceField.setValue("0.00 EUR");
      balanceField.removeClassName(STYLE_TEXT_DANGER);
      return;
    }

    balanceField.setValue(balance + " EUR");
    if (balance.compareTo(BigDecimal.ZERO) < 0) {
      balanceField.addClassName(STYLE_TEXT_DANGER);
    } else {
      balanceField.removeClassName(STYLE_TEXT_DANGER);
    }
  }

  private void showNoAccountSummary() {
    accountNoField.setValue("-");
    balanceField.setValue("-");
    balanceField.removeClassName(STYLE_TEXT_DANGER);
  }
}
