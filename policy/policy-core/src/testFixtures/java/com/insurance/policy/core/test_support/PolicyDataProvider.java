package com.insurance.policy.core.test_support;

import com.insurance.common.test_support.TestDataProvider;
import com.insurance.policy.core.entity.Policy;
import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PolicyDataProvider implements TestDataProvider<Policy> {

  @Override
  public Class<Policy> getEntityClass() {
    return Policy.class;
  }

  @Override
  public void accept(Policy policy) {
    policy.setPartnerNo("PT-" + UUID.randomUUID().toString().substring(0, 5));
    policy.setInsuranceProduct(InsuranceProduct.HOME_CONTENT_BASIC_2024_01);
    policy.setPolicyNo("POL-" + UUID.randomUUID().toString().substring(0, 8));
    policy.setCoverageStart(LocalDate.of(2025, 1, 1));
    policy.setPremium(new BigDecimal("150.00"));
    policy.setPaymentFrequency(PaymentFrequency.YEARLY);
  }
}
