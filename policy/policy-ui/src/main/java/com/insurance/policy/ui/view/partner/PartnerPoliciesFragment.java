package com.insurance.policy.ui.view.partner;

import com.insurance.policy.api.dto.PartnerPolicySummaryDto;
import com.insurance.policy.api.service.PartnerPolicyOverviewService;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("partner-policies-fragment.xml")
public class PartnerPoliciesFragment extends Fragment<VerticalLayout> {

  @ViewComponent private CollectionContainer<PartnerPolicySummaryDto> policiesDc;

  @Autowired private PartnerPolicyOverviewService partnerPolicyOverviewService;
  @Autowired private UiComponents uiComponents;
  @Autowired private Messages messages;

  public void setPartnerNo(String partnerNo) {
    if (partnerNo == null || partnerNo.isBlank()) {
      policiesDc.setItems(List.of());
      return;
    }

    policiesDc.setItems(partnerPolicyOverviewService.findPoliciesForPartner(partnerNo));
  }

  @Supply(to = "policiesGrid.status", subject = "renderer")
  protected Renderer<PartnerPolicySummaryDto> statusComponentRenderer() {
    return new ComponentRenderer<>(
        () -> {
          Span span = uiComponents.create(Span.class);
          span.getElement().getThemeList().add("badge");
          return span;
        },
        (span, policySummary) -> {
          span.getElement().getThemeList().remove("success");
          span.getElement().getThemeList().remove("error");

          LocalDate coverageEnd = policySummary.getCoverageEnd();
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
