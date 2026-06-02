package com.insurance.partner.core.security;

import com.insurance.partner.core.entity.Partner;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Partner Core: Manage", code = PartnerCoreManageRole.CODE)
public interface PartnerCoreManageRole {
    String CODE = "partner-core-manage";

    @EntityAttributePolicy(entityClass = Partner.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Partner.class, actions = {EntityPolicyAction.ALL})
    void partnerEntity();
}
