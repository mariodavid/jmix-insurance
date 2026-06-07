package com.insurance.partner.ui.view.partner;

import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.ui.api.PartnerSection;
import com.insurance.partner.ui.api.PartnerViewContext;
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
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "partners/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "partner_Partner.detail")
@ViewDescriptor(path = "partner-detail-view.xml")
@EditedEntityContainer("partnerDc")
@CssImport("./partner/styles.css")
public class PartnerDetailView extends StandardDetailView<Partner> {

  @ViewComponent private VerticalLayout partnerSectionsBox;

  @Autowired private ObjectProvider<PartnerSection> partnerSections;
  @Autowired private Messages messages;

  @Subscribe
  public void onBeforeShow(final BeforeShowEvent event) {
    PartnerViewContext context = contextFor(getEditedEntity());

    partnerSectionsBox.removeAll();
    partnerSections.orderedStream().forEach(section -> addSection(section, context));
  }

  private PartnerViewContext contextFor(Partner partner) {
    return new PartnerViewContextImpl(partner.getId(), partner.getPartnerNo());
  }

  private void addSection(PartnerSection section, PartnerViewContext context) {
    Component content = section.createContent(context, this);
    setWidthFull(content);

    Details details = new Details();
    details.setSummaryText(messages.getMessage(section.getClass(), section.titleMessageKey()));
    details.setWidthFull();
    details.getElement().getThemeList().add("filled");
    details.setOpened(true);
    details.add(content);

    partnerSectionsBox.add(details);
  }

  private void setWidthFull(Component component) {
    if (component instanceof HasSize hasSize) {
      hasSize.setWidthFull();
    } else {
      component.getElement().getStyle().set("width", "100%");
    }
  }

  private record PartnerViewContextImpl(UUID partnerId, String partnerNo)
      implements PartnerViewContext {}
}
