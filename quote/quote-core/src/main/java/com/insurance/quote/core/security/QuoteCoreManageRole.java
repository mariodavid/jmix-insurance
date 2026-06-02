package com.insurance.quote.core.security;

import com.insurance.quote.core.entity.Quote;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Quote Core: Manage", code = QuoteCoreManageRole.CODE)
public interface QuoteCoreManageRole {
    String CODE = "quote-core-manage";

    @EntityAttributePolicy(entityClass = Quote.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Quote.class, actions = {EntityPolicyAction.ALL})
    void quoteEntity();
}
