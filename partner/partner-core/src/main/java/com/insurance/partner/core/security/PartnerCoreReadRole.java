package com.insurance.partner.core.security;

import com.insurance.partner.core.entity.Partner;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Partner Core: Read-only", code = PartnerCoreReadRole.CODE)
public interface PartnerCoreReadRole {
  String CODE = "partner-core-read";

  @EntityAttributePolicy(
      entityClass = Partner.class,
      attributes = "*",
      action = EntityAttributePolicyAction.VIEW)
  @EntityPolicy(
      entityClass = Partner.class,
      actions = {EntityPolicyAction.READ})
  void partnerEntity();
}
