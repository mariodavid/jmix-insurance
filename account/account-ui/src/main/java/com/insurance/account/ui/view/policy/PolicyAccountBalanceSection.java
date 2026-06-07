package com.insurance.account.ui.view.policy;

import com.insurance.policy.ui.api.PolicySection;
import com.insurance.policy.ui.api.PolicyViewContext;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.Fragments;
import io.jmix.flowui.fragment.FragmentOwner;
import org.springframework.core.annotation.Order;

@org.springframework.stereotype.Component
@Order(200)
public class PolicyAccountBalanceSection implements PolicySection {

  private final Fragments fragments;

  public PolicyAccountBalanceSection(Fragments fragments) {
    this.fragments = fragments;
  }

  @Override
  public String titleMessageKey() {
    return "policyAccountBalanceSection.title";
  }

  @Override
  public Component createContent(PolicyViewContext context, FragmentOwner fragmentOwner) {
    PolicyAccountBalanceFragment fragment =
        fragments.create(fragmentOwner, PolicyAccountBalanceFragment.class);
    fragment.setPolicyNo(context.policyNo());
    return fragment;
  }
}
