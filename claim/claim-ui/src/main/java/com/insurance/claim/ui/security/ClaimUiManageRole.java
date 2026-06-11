package com.insurance.claim.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Claim UI: Manage", code = ClaimUiManageRole.CODE)
public interface ClaimUiManageRole {
  String CODE = "claim-ui-manage";

  @ViewPolicy(viewIds = {"claim_Claim.list", "claim_Claim.detail", "claim_ClaimReserve.detail"})
  @MenuPolicy(menuIds = {"claim_Claim.list"})
  void claimUi();
}
