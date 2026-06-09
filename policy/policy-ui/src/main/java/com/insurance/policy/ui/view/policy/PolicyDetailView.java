package com.insurance.policy.ui.view.policy;

import com.insurance.policy.core.entity.Policy;
import com.insurance.policy.ui.api.PolicySection;
import com.insurance.policy.ui.api.PolicyViewContext;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "policies/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "policy_Policy.detail")
@ViewDescriptor(path = "policy-detail-view.xml")
@EditedEntityContainer("policyDc")
@CssImport("./policy/styles.css")
public class PolicyDetailView extends StandardDetailView<Policy> {

  @ViewComponent private VerticalLayout policySectionsBox;

  private final ObjectProvider<PolicySection> policySections;
  private final Messages messages;

  @Autowired
  public PolicyDetailView(ObjectProvider<PolicySection> policySections, Messages messages) {
    this.policySections = policySections;
    this.messages = messages;
  }

  @Subscribe
  public void onBeforeShow(final BeforeShowEvent event) {
    PolicyViewContext context = contextFor(getEditedEntity());

    policySectionsBox.removeAll();
    policySections.orderedStream().forEach(section -> addSection(section, context));
  }

  private PolicyViewContext contextFor(Policy policy) {
    return new PolicyViewContextImpl(
        policy.getId(),
        policy.getPolicyNo(),
        policy.getPartnerNo(),
        policy.getCoverageStart(),
        policy.getCoverageEnd());
  }

  private void addSection(PolicySection section, PolicyViewContext context) {
    Component content = section.createContent(context, this);
    setWidthFull(content);

    Details details = new Details();
    details.setSummaryText(messages.getMessage(section.getClass(), section.titleMessageKey()));
    details.setWidthFull();
    details.getElement().getThemeList().add("filled");
    details.setOpened(true);
    details.add(content);

    policySectionsBox.add(details);
  }

  private void setWidthFull(Component component) {
    if (component instanceof HasSize hasSize) {
      hasSize.setWidthFull();
    } else {
      component.getElement().getStyle().set("width", "100%");
    }
  }

  private record PolicyViewContextImpl(
      UUID policyId,
      String policyNo,
      String partnerNo,
      LocalDate coverageStart,
      LocalDate coverageEnd)
      implements PolicyViewContext {}
}
