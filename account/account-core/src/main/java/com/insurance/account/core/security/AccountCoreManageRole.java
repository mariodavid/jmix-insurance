package com.insurance.account.core.security;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Account Core: Manage", code = AccountCoreManageRole.CODE)
public interface AccountCoreManageRole {
    String CODE = "account-core-manage";

    @EntityAttributePolicy(entityClass = Account.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Account.class, actions = {EntityPolicyAction.ALL})
    void accountEntity();

    @EntityAttributePolicy(entityClass = AccountDocument.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = AccountDocument.class, actions = {EntityPolicyAction.ALL})
    void accountDocumentEntity();
}
