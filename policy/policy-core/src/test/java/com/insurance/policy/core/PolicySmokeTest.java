package com.insurance.policy.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.insurance.policy.core.entity.Policy;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PolicySmokeTest {

  @Test
  void test1() {
    Policy policy = new Policy();
    policy.setPremium(new BigDecimal("100.00"));
    assertEquals(new BigDecimal("100.00"), policy.getPremium());
  }
}
