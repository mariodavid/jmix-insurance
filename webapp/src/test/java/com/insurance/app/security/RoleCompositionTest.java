package com.insurance.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.account.core.security.AccountCoreManageRole;
import com.insurance.account.core.security.AccountCoreReadRole;
import com.insurance.account.ui.security.AccountUiManageRole;
import com.insurance.account.ui.security.AccountUiReadRole;
import com.insurance.partner.core.security.PartnerCoreManageRole;
import com.insurance.partner.ui.security.PartnerUiManageRole;
import com.insurance.policy.core.security.PolicyCoreManageRole;
import com.insurance.policy.core.security.PolicyCoreReadRole;
import com.insurance.policy.ui.security.PolicyUiManageRole;
import com.insurance.policy.ui.security.PolicyUiReadRole;
import com.insurance.quote.core.security.QuoteCoreManageRole;
import com.insurance.quote.ui.security.QuoteUiManageRole;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RoleCompositionTest {

  @Test
  void given_insuranceAgentRole_when_interfacesInspected_then_roleHasExpectedDomainAccess() {
    assertThat(directRoleInterfaces(InsuranceAgentRole.class))
        .contains(
            UiMinimalRole.class,
            PartnerCoreManageRole.class,
            PartnerUiManageRole.class,
            QuoteCoreManageRole.class,
            QuoteUiManageRole.class,
            PolicyCoreReadRole.class,
            PolicyUiReadRole.class,
            AccountCoreReadRole.class,
            AccountUiReadRole.class)
        .doesNotContain(
            PolicyCoreManageRole.class,
            PolicyUiManageRole.class,
            AccountCoreManageRole.class,
            AccountUiManageRole.class);
  }

  @Test
  void given_insuranceBackOfficeRole_when_interfacesInspected_then_roleCanManageAllDomains() {
    assertThat(directRoleInterfaces(InsuranceBackOfficeRole.class))
        .contains(
            UiMinimalRole.class,
            PartnerCoreManageRole.class,
            PartnerUiManageRole.class,
            QuoteCoreManageRole.class,
            QuoteUiManageRole.class,
            PolicyCoreManageRole.class,
            PolicyUiManageRole.class,
            AccountCoreManageRole.class,
            AccountUiManageRole.class)
        .doesNotContain(
            PolicyCoreReadRole.class, PolicyUiReadRole.class, AccountCoreReadRole.class);
  }

  private Set<Class<?>> directRoleInterfaces(Class<?> roleClass) {
    return Set.of(roleClass.getInterfaces());
  }
}
