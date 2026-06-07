package com.insurance.security.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Security UI: Manage", code = SecurityUiManageRole.CODE)
public interface SecurityUiManageRole {
  String CODE = "security-ui-manage";

  @ViewPolicy(viewIds = {"security_User.list", "security_User.detail"})
  @MenuPolicy(menuIds = {"security_User.list"})
  void userUi();
}
