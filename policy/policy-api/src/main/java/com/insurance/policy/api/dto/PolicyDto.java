package com.insurance.policy.api.dto;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JmixEntity(name = "policy_api_PolicyDto")
public class PolicyDto {

  @JmixGeneratedValue @JmixId private UUID id;

  private String partnerNo;

  private String insuranceProduct;

  private String policyNo;

  private LocalDate coverageStart;

  private LocalDate coverageEnd;

  private BigDecimal premium;

  private String paymentFrequency;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getPartnerNo() {
    return partnerNo;
  }

  public void setPartnerNo(String partnerNo) {
    this.partnerNo = partnerNo;
  }

  public String getInsuranceProduct() {
    return insuranceProduct;
  }

  public void setInsuranceProduct(String insuranceProduct) {
    this.insuranceProduct = insuranceProduct;
  }

  public String getPolicyNo() {
    return policyNo;
  }

  public void setPolicyNo(String policyNo) {
    this.policyNo = policyNo;
  }

  public LocalDate getCoverageStart() {
    return coverageStart;
  }

  public void setCoverageStart(LocalDate coverageStart) {
    this.coverageStart = coverageStart;
  }

  public LocalDate getCoverageEnd() {
    return coverageEnd;
  }

  public void setCoverageEnd(LocalDate coverageEnd) {
    this.coverageEnd = coverageEnd;
  }

  public BigDecimal getPremium() {
    return premium;
  }

  public void setPremium(BigDecimal premium) {
    this.premium = premium;
  }

  public String getPaymentFrequency() {
    return paymentFrequency;
  }

  public void setPaymentFrequency(String paymentFrequency) {
    this.paymentFrequency = paymentFrequency;
  }

  @InstanceName
  public String instanceName() {
    return policyNo;
  }
}
