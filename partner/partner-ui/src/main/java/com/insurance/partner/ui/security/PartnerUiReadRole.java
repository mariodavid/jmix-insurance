package com.insurance.partner.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Partner UI: Read-only", code = PartnerUiReadRole.CODE)
public interface PartnerUiReadRole {
  String CODE = "partner-ui-read";

  @ViewPolicy(viewIds = {"partner_Partner.list"})
  @MenuPolicy(menuIds = {"partner_Partner.list"})
  void partnerUi();
}
