package com.insurance.partner.ui.view.policy;

import com.insurance.policy.ui.api.PolicySection;
import com.insurance.policy.ui.api.PolicyViewContext;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.fragment.FragmentOwner;
import org.springframework.core.annotation.Order;

@org.springframework.stereotype.Component
@Order(100)
public class PolicyHolderSection implements PolicySection {

  private final Fragments fragments;

  public PolicyHolderSection(Fragments fragments) {
    this.fragments = fragments;
  }

  @Override
  public String titleMessageKey() {
    return "policyHolderSection.title";
  }

  @Override
  public Component createContent(PolicyViewContext context, FragmentOwner fragmentOwner) {
    PolicyHolderFragment fragment = fragments.create(fragmentOwner, PolicyHolderFragment.class);
    fragment.setPartnerNo(context.partnerNo());
    return fragment;
  }
}
