package com.insurance.claim.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Claim UI: Read-only", code = ClaimUiReadRole.CODE)
public interface ClaimUiReadRole {
  String CODE = "claim-ui-read";

  @ViewPolicy(viewIds = {"claim_Claim.list"})
  @MenuPolicy(menuIds = {"claim_Claim.list"})
  void claimUi();
}
