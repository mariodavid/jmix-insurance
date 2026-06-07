package com.insurance.account.core.test_support;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountPolicyReference;
import com.insurance.common.test_support.TestDataProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class AccountDataProvider implements TestDataProvider<Account> {

  @Override
  public Class<Account> getEntityClass() {
    return Account.class;
  }

  @Override
  public void accept(Account account) {
    AccountPolicyReference policyRef = account.getPolicy();
    if (policyRef != null) {
      policyRef.setPolicyId(UUID.randomUUID());
      policyRef.setPolicyNo("ACT-" + UUID.randomUUID().toString().substring(0, 8));
      policyRef.setPartnerNo("PT-" + UUID.randomUUID().toString().substring(0, 8));
    }
    account.setAccountingPeriodStart(LocalDate.of(2025, 1, 1));
    account.setAccountingPeriodEnd(LocalDate.of(2026, 1, 1));
    account.setAccountBalance(BigDecimal.ZERO);
  }
}
