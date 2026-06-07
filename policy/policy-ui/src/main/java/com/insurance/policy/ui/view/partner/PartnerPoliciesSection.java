package com.insurance.policy.ui.view.partner;

import com.insurance.partner.ui.api.PartnerSection;
import com.insurance.partner.ui.api.PartnerViewContext;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.fragment.FragmentOwner;
import org.springframework.core.annotation.Order;

@org.springframework.stereotype.Component
@Order(100)
public class PartnerPoliciesSection implements PartnerSection {

  private final Fragments fragments;

  public PartnerPoliciesSection(Fragments fragments) {
    this.fragments = fragments;
  }

  @Override
  public String titleMessageKey() {
    return "partnerPoliciesSection.title";
  }

  @Override
  public Component createContent(PartnerViewContext context, FragmentOwner fragmentOwner) {
    PartnerPoliciesFragment fragment =
        fragments.create(fragmentOwner, PartnerPoliciesFragment.class);
    fragment.setPartnerNo(context.partnerNo());
    return fragment;
  }
}
