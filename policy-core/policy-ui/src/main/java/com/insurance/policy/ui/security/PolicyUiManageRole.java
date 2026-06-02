package com.insurance.policy.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Policy UI: Manage", code = PolicyUiManageRole.CODE)
public interface PolicyUiManageRole {
    String CODE = "policy-ui-manage";

    @ViewPolicy(viewIds = {"policy_Policy.list", "policy_Policy.detail"})
    @MenuPolicy(menuIds = {"policy_Policy.list"})
    void policyUi();
}
