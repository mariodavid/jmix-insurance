package com.insurance.partner.ui.view.policy;

import com.insurance.partner.api.dto.PartnerDto;
import com.insurance.partner.api.service.PartnerService;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.ViewComponent;
import org.springframework.beans.factory.annotation.Autowired;

@FragmentDescriptor("policy-holder-fragment.xml")
public class PolicyHolderFragment extends Fragment<VerticalLayout> {

  @ViewComponent private InstanceContainer<PartnerDto> partnerDc;

  private final PartnerService partnerService;
  private final Metadata metadata;
  private final Notifications notifications;
  private final Messages messages;

  @Autowired
  public PolicyHolderFragment(
      PartnerService partnerService,
      Metadata metadata,
      Notifications notifications,
      Messages messages) {
    this.partnerService = partnerService;
    this.metadata = metadata;
    this.notifications = notifications;
    this.messages = messages;
  }

  public void setPartnerNo(String partnerNo) {
    if (partnerNo == null || partnerNo.isBlank()) {
      showNoPartner();
      return;
    }

    PartnerDto partnerDto = partnerService.getPartner(partnerNo);
    if (partnerDto != null) {
      partnerDc.setItem(partnerDto);
    } else {
      showNoPartner();
    }
  }

  private void showNoPartner() {
    partnerDc.setItem(metadata.create(PartnerDto.class));
    notifications
        .create(messages.getMessage(getClass(), "noPolicyHolder"))
        .withType(Notifications.Type.WARNING)
        .withPosition(Notification.Position.TOP_END)
        .show();
  }
}
