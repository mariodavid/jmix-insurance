package com.insurance.account.core.security;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Account Core: Read-only", code = AccountCoreReadRole.CODE)
public interface AccountCoreReadRole {
  String CODE = "account-core-read";

  @EntityAttributePolicy(
      entityClass = Account.class,
      attributes = "*",
      action = EntityAttributePolicyAction.VIEW)
  @EntityPolicy(
      entityClass = Account.class,
      actions = {EntityPolicyAction.READ})
  void accountEntity();

  @EntityAttributePolicy(
      entityClass = AccountDocument.class,
      attributes = "*",
      action = EntityAttributePolicyAction.VIEW)
  @EntityPolicy(
      entityClass = AccountDocument.class,
      actions = {EntityPolicyAction.READ})
  void accountDocumentEntity();
}
