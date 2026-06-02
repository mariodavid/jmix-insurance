package com.insurance.quote.core.security;

import com.insurance.quote.core.entity.Quote;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Quote Core: Read-only", code = QuoteCoreReadRole.CODE)
public interface QuoteCoreReadRole {
    String CODE = "quote-core-read";

    @EntityAttributePolicy(entityClass = Quote.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Quote.class, actions = {EntityPolicyAction.READ})
    void quoteEntity();
}
