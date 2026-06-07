package com.insurance.policy.api.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.context.ApplicationEvent;

public class PolicyCreatedEvent extends ApplicationEvent {

  private final UUID policyId;
  private final String policyNo;
  private final String partnerNo;
  private final LocalDate coverageStart;
  private final LocalDate coverageEnd;
  private final BigDecimal premium;
  private final String paymentFrequencyId;

  public PolicyCreatedEvent(
      Object source,
      UUID policyId,
      String policyNo,
      String partnerNo,
      LocalDate coverageStart,
      LocalDate coverageEnd,
      BigDecimal premium,
      String paymentFrequencyId) {
    super(source);
    this.policyId = policyId;
    this.policyNo = policyNo;
    this.partnerNo = partnerNo;
    this.coverageStart = coverageStart;
    this.coverageEnd = coverageEnd;
    this.premium = premium;
    this.paymentFrequencyId = paymentFrequencyId;
  }

  public UUID getPolicyId() {
    return policyId;
  }

  public String getPolicyNo() {
    return policyNo;
  }

  public String getPartnerNo() {
    return partnerNo;
  }

  public LocalDate getCoverageStart() {
    return coverageStart;
  }

  public LocalDate getCoverageEnd() {
    return coverageEnd;
  }

  public BigDecimal getPremium() {
    return premium;
  }

  public String getPaymentFrequencyId() {
    return paymentFrequencyId;
  }
}
