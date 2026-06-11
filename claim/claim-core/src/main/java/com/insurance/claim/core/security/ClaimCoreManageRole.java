package com.insurance.claim.core.security;

import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.ClaimReserve;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Claim Core: Manage", code = ClaimCoreManageRole.CODE)
public interface ClaimCoreManageRole {
  String CODE = "claim-core-manage";

  @EntityAttributePolicy(
      entityClass = Claim.class,
      attributes = "*",
      action = EntityAttributePolicyAction.MODIFY)
  @EntityPolicy(
      entityClass = Claim.class,
      actions = {EntityPolicyAction.ALL})
  void claimEntity();

  @EntityAttributePolicy(
      entityClass = ClaimReserve.class,
      attributes = "*",
      action = EntityAttributePolicyAction.MODIFY)
  @EntityPolicy(
      entityClass = ClaimReserve.class,
      actions = {EntityPolicyAction.ALL})
  void claimReserveEntity();
}
