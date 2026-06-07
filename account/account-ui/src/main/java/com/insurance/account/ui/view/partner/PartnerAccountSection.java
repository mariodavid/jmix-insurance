package com.insurance.account.ui.view.partner;

import com.insurance.partner.ui.api.PartnerSection;
import com.insurance.partner.ui.api.PartnerViewContext;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.fragment.FragmentOwner;
import org.springframework.core.annotation.Order;

@org.springframework.stereotype.Component
@Order(200)
public class PartnerAccountSection implements PartnerSection {

  private final Fragments fragments;

  public PartnerAccountSection(Fragments fragments) {
    this.fragments = fragments;
  }

  @Override
  public String titleMessageKey() {
    return "partnerAccountSection.title";
  }

  @Override
  public Component createContent(PartnerViewContext context, FragmentOwner fragmentOwner) {
    PartnerAccountFragment fragment = fragments.create(fragmentOwner, PartnerAccountFragment.class);
    fragment.setPartnerNo(context.partnerNo());
    return fragment;
  }
}
