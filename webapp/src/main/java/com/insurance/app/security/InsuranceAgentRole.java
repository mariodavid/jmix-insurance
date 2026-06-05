package com.insurance.app.security;

import com.insurance.account.core.security.AccountCoreReadRole;
import com.insurance.account.ui.security.AccountUiReadRole;
import com.insurance.partner.core.security.PartnerCoreManageRole;
import com.insurance.partner.ui.security.PartnerUiManageRole;
import com.insurance.policy.core.security.PolicyCoreReadRole;
import com.insurance.policy.ui.security.PolicyUiReadRole;
import com.insurance.quote.core.security.QuoteCoreManageRole;
import com.insurance.quote.ui.security.QuoteUiManageRole;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Insurance Agent", code = InsuranceAgentRole.CODE)
public interface InsuranceAgentRole
    extends UiMinimalRole,
        PartnerCoreManageRole,
        PartnerUiManageRole,
        QuoteCoreManageRole,
        QuoteUiManageRole,
        PolicyCoreReadRole,
        PolicyUiReadRole,
        AccountCoreReadRole,
        AccountUiReadRole {

  String CODE = "insurance-agent";
}
