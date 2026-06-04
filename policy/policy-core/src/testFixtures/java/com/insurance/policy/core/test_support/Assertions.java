package com.insurance.policy.core.test_support;

import com.insurance.policy.core.entity.Policy;
import com.insurance.policy.core.entity.PolicyAssert;

public class Assertions extends org.assertj.core.api.Assertions {

  public static PolicyAssert assertThat(Policy actual) {
    return new PolicyAssert(actual);
  }

  protected Assertions() {
    // empty
  }
}
