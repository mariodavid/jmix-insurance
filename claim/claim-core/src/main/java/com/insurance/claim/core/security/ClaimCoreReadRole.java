package com.insurance.claim.core.security;

import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.ClaimReserve;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Claim Core: Read-only", code = ClaimCoreReadRole.CODE)
public interface ClaimCoreReadRole {
  String CODE = "claim-core-read";

  @EntityAttributePolicy(
      entityClass = Claim.class,
      attributes = "*",
      action = EntityAttributePolicyAction.VIEW)
  @EntityPolicy(
      entityClass = Claim.class,
      actions = {EntityPolicyAction.READ})
  void claimEntity();

  @EntityAttributePolicy(
      entityClass = ClaimReserve.class,
      attributes = "*",
      action = EntityAttributePolicyAction.VIEW)
  @EntityPolicy(
      entityClass = ClaimReserve.class,
      actions = {EntityPolicyAction.READ})
  void claimReserveEntity();
}
