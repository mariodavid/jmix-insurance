package com.insurance.partner.ui.view.partner;

import com.insurance.partner.core.entity.Partner;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.KeyValueCollectionLoader;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "partners/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "partner_Partner.detail")
@ViewDescriptor(path = "partner-detail-view.xml")
@EditedEntityContainer("partnerDc")
@CssImport("./partner/styles.css")
public class PartnerDetailView extends StandardDetailView<Partner> {

  @ViewComponent private KeyValueCollectionLoader policiesDl;

  @ViewComponent private DataGrid<KeyValueEntity> policiesGrid;

  @ViewComponent private TextField accountNoField;

  @ViewComponent private TextField balanceField;

  @Autowired private DataManager dataManager;
  @Autowired private UiComponents uiComponents;
  @Autowired private Messages messages;

  @Subscribe
  public void onBeforeShow(final BeforeShowEvent event) {
    Partner partner = getEditedEntity();

    if (partner.getPartnerNo() != null) {
      // 1. Load Policies key-value collection
      policiesDl.setParameter("partnerNo", partner.getPartnerNo());
      policiesDl.load();

      // 2. Query Accounts associated with partner's policies
      List<KeyValueEntity> accounts =
          dataManager
              .loadValues(
                  "select a.accountNo, a.accountBalance from account_Account a, policy_Policy p "
                      + "where a.accountNo = p.policyNo and p.partnerNo = :partnerNo")
              .properties("accountNo", "accountBalance")
              .parameter("partnerNo", partner.getPartnerNo())
              .list();

      if (!accounts.isEmpty()) {
        KeyValueEntity acc = accounts.get(0);
        accountNoField.setValue(acc.getValue("accountNo") != null ? acc.getValue("accountNo") : "");
        BigDecimal bal = acc.getValue("accountBalance");
        if (bal != null) {
          balanceField.setValue(bal.toString() + " EUR");
          if (bal.compareTo(BigDecimal.ZERO) < 0) {
            balanceField.addClassName("text-danger");
          } else {
            balanceField.removeClassName("text-danger");
          }
        } else {
          balanceField.setValue("0.00 EUR");
        }
      } else {
        accountNoField.setValue("—");
        balanceField.setValue("—");
      }
    }
  }

  @Supply(to = "policiesGrid.status", subject = "renderer")
  protected Renderer<KeyValueEntity> statusComponentRenderer() {
    return new ComponentRenderer<>(
        () -> {
          Span span = uiComponents.create(Span.class);
          span.getElement().getThemeList().add("badge");
          return span;
        },
        (span, keyValueEntity) -> {
          span.getElement().getThemeList().remove("success");
          span.getElement().getThemeList().remove("error");

          LocalDate coverageEnd = keyValueEntity.getValue("coverageEnd");
          if (coverageEnd == null || !coverageEnd.isBefore(LocalDate.now())) {
            span.setText(messages.getMessage(getClass(), "status.active"));
            span.getElement().getThemeList().add("success");
          } else {
            span.setText(messages.getMessage(getClass(), "status.expired"));
            span.getElement().getThemeList().add("error");
          }
        });
  }
}
