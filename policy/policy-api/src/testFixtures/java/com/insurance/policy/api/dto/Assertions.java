package com.insurance.policy.api.dto;

public class Assertions extends org.assertj.core.api.Assertions {

  public static PolicyDtoAssert assertThat(PolicyDto actual) {
    return new PolicyDtoAssert(actual);
  }

  protected Assertions() {}
}
