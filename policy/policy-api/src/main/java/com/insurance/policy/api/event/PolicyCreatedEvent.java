package com.insurance.policy.api.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.context.ApplicationEvent;

public class PolicyCreatedEvent extends ApplicationEvent {

  private final UUID policyId;
  private final String policyNo;
  private final LocalDate coverageStart;
  private final BigDecimal premium;
  private final String paymentFrequencyId;

  public PolicyCreatedEvent(
      Object source,
      UUID policyId,
      String policyNo,
      LocalDate coverageStart,
      BigDecimal premium,
      String paymentFrequencyId) {
    super(source);
    this.policyId = policyId;
    this.policyNo = policyNo;
    this.coverageStart = coverageStart;
    this.premium = premium;
    this.paymentFrequencyId = paymentFrequencyId;
  }

  public UUID getPolicyId() {
    return policyId;
  }

  public String getPolicyNo() {
    return policyNo;
  }

  public LocalDate getCoverageStart() {
    return coverageStart;
  }

  public BigDecimal getPremium() {
    return premium;
  }

  public String getPaymentFrequencyId() {
    return paymentFrequencyId;
  }
}
