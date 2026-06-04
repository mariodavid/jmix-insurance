package com.insurance.account.api.dto;

public class Assertions extends org.assertj.core.api.Assertions {

  public static AccountDtoAssert assertThat(AccountDto actual) {
    return new AccountDtoAssert(actual);
  }

  protected Assertions() {}
}
