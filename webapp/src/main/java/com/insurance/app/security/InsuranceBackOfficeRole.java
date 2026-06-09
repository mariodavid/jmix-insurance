package com.insurance.app.security;

import com.insurance.account.core.security.AccountCoreManageRole;
import com.insurance.account.ui.security.AccountUiManageRole;
import com.insurance.partner.core.security.PartnerCoreManageRole;
import com.insurance.partner.ui.security.PartnerUiManageRole;
import com.insurance.policy.core.security.PolicyCoreManageRole;
import com.insurance.policy.ui.security.PolicyUiManageRole;
import com.insurance.quote.core.security.QuoteCoreManageRole;
import com.insurance.quote.ui.security.QuoteUiManageRole;
import io.jmix.security.role.annotation.ResourceRole;

@SuppressWarnings("PMD.ConstantsInInterface")
@ResourceRole(name = "Insurance Backoffice", code = InsuranceBackOfficeRole.CODE)
public interface InsuranceBackOfficeRole
    extends UiMinimalRole,
        PartnerCoreManageRole,
        PartnerUiManageRole,
        QuoteCoreManageRole,
        QuoteUiManageRole,
        PolicyCoreManageRole,
        PolicyUiManageRole,
        AccountCoreManageRole,
        AccountUiManageRole {

  String CODE = "insurance-backoffice";
}
