package com.insurance.partner.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Partner UI: Manage", code = PartnerUiManageRole.CODE)
public interface PartnerUiManageRole {
  String CODE = "partner-ui-manage";

  @ViewPolicy(viewIds = {"partner_Partner.list", "partner_Partner.detail"})
  @MenuPolicy(menuIds = {"partner_Partner.list"})
  void partnerUi();
}
