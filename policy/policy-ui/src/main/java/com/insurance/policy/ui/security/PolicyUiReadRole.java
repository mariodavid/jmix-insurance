package com.insurance.policy.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Policy UI: Read-only", code = PolicyUiReadRole.CODE)
public interface PolicyUiReadRole {
  String CODE = "policy-ui-read";

  @ViewPolicy(viewIds = {"policy_Policy.list"})
  @MenuPolicy(menuIds = {"policy_Policy.list"})
  void policyUi();
}
