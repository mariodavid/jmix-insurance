package com.insurance.policy.core.security;

import com.insurance.policy.core.entity.Policy;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Policy Core: Manage", code = PolicyCoreManageRole.CODE)
public interface PolicyCoreManageRole {
  String CODE = "policy-core-manage";

  @EntityAttributePolicy(
      entityClass = Policy.class,
      attributes = "*",
      action = EntityAttributePolicyAction.MODIFY)
  @EntityPolicy(
      entityClass = Policy.class,
      actions = {EntityPolicyAction.ALL})
  void policyEntity();
}
