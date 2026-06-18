package com.insurance.claim.ui.view.claim;

import com.insurance.claim.api.dto.ClaimStatus;
import com.insurance.claim.core.entity.Claim;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "claims", layout = DefaultMainViewParent.class)
@ViewController(id = "claim_Claim.list")
@ViewDescriptor(path = "claim-list-view.xml")
@LookupComponent("claimsDataGrid")
@DialogMode(width = "64em")
public class ClaimListView extends StandardListView<Claim> {

  private final UiComponents uiComponents;
  private final Messages messages;

  @Autowired
  public ClaimListView(UiComponents uiComponents, Messages messages) {
    this.uiComponents = uiComponents;
    this.messages = messages;
  }

  @Supply(to = "claimsDataGrid.status", subject = "renderer")
  protected Renderer<Claim> statusComponentRenderer() {
    return new ComponentRenderer<>(
        () -> {
          Span span = uiComponents.create(Span.class);
          span.getElement().getThemeList().add("badge");
          return span;
        },
        (span, claim) -> {
          span.getElement().getThemeList().remove("success");
          span.getElement().getThemeList().remove("error");
          span.getElement().getThemeList().remove("contrast");

          ClaimStatus status = claim.getStatus();
          if (status == null) {
            span.setText("");
            return;
          }

          span.setText(messages.getMessage(status));
          switch (status) {
            case OPEN -> span.getElement().getThemeList().add("success");
            case IN_PROGRESS -> span.getElement().getThemeList().add("contrast");
            case CLOSED -> span.getElement().getThemeList().add("error");
          }
        });
  }
}
