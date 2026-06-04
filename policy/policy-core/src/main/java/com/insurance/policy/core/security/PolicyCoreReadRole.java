package com.insurance.policy.core.security;

import com.insurance.policy.core.entity.Policy;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Policy Core: Read-only", code = PolicyCoreReadRole.CODE)
public interface PolicyCoreReadRole {
  String CODE = "policy-core-read";

  @EntityAttributePolicy(
      entityClass = Policy.class,
      attributes = "*",
      action = EntityAttributePolicyAction.VIEW)
  @EntityPolicy(
      entityClass = Policy.class,
      actions = {EntityPolicyAction.READ})
  void policyEntity();
}
