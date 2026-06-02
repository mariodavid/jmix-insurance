package com.insurance.account.core.test_support;

import com.insurance.account.core.entity.Account;
import com.insurance.common.test_support.TestDataProvider;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountDataProvider implements TestDataProvider<Account> {

    @Override
    public Class<Account> getEntityClass() {
        return Account.class;
    }

    @Override
    public void accept(Account account) {
        account.setPolicyId(UUID.randomUUID());
        account.setAccountNo("ACT-" + UUID.randomUUID().toString().substring(0, 8));
        account.setAccountBalance(BigDecimal.ZERO);
    }
}
