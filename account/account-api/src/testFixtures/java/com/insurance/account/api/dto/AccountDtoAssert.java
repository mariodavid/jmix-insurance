package com.insurance.account.api.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import org.assertj.core.api.AbstractAssert;

public class AccountDtoAssert extends AbstractAssert<AccountDtoAssert, AccountDto> {

  public AccountDtoAssert(AccountDto actual) {
    super(actual, AccountDtoAssert.class);
  }

  public static AccountDtoAssert assertThat(AccountDto actual) {
    return new AccountDtoAssert(actual);
  }

  public AccountDtoAssert hasId(UUID expected) {
    isNotNull();
    if (!Objects.equals(actual.getId(), expected)) {
      failWithMessage("Expected AccountDto id to be <%s> but was <%s>", expected, actual.getId());
    }
    return this;
  }

  public AccountDtoAssert hasPolicyNo(String expected) {
    isNotNull();
    if (!Objects.equals(actual.getPolicyNo(), expected)) {
      failWithMessage(
          "Expected AccountDto policyNo to be <%s> but was <%s>", expected, actual.getPolicyNo());
    }
    return this;
  }

  public AccountDtoAssert hasBalance(BigDecimal expected) {
    isNotNull();
    if (actual.getBalance() == null || actual.getBalance().compareTo(expected) != 0) {
      failWithMessage(
          "Expected AccountDto balance to be <%s> but was <%s>", expected, actual.getBalance());
    }
    return this;
  }
}
