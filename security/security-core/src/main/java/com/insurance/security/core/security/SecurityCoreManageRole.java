package com.insurance.security.core.security;

import com.insurance.security.core.entity.User;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Security Core: Manage", code = SecurityCoreManageRole.CODE)
public interface SecurityCoreManageRole {
  String CODE = "security-core-manage";

  @EntityAttributePolicy(
      entityClass = User.class,
      attributes = "*",
      action = EntityAttributePolicyAction.MODIFY)
  @EntityPolicy(
      entityClass = User.class,
      actions = {EntityPolicyAction.ALL})
  void userEntity();
}
