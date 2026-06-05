package com.insurance.policy.ui.view.policy;

import com.insurance.policy.core.entity.Policy;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "policies", layout = DefaultMainViewParent.class)
@ViewController(id = "policy_Policy.list")
@ViewDescriptor(path = "policy-list-view.xml")
@LookupComponent("policiesDataGrid")
@DialogMode(width = "64em")
@CssImport("./policy/styles.css")
public class PolicyListView extends StandardListView<Policy> {

  @ViewComponent private DataGrid<Policy> policiesDataGrid;

  @Autowired private UiComponents uiComponents;
  @Autowired private Messages messages;

  @Supply(to = "policiesDataGrid.status", subject = "renderer")
  protected Renderer<Policy> statusComponentRenderer() {
    return new ComponentRenderer<>(
        () -> {
          Span span = uiComponents.create(Span.class);
          span.getElement().getThemeList().add("badge");
          return span;
        },
        (span, policy) -> {
          // Reset theme classes to prevent style accumulation/drift
          span.getElement().getThemeList().remove("success");
          span.getElement().getThemeList().remove("error");

          LocalDate coverageEnd = policy.getCoverageEnd();
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
