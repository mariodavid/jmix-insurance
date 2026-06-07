package com.insurance.policy.api.dto;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import java.time.LocalDate;
import java.util.UUID;

@JmixEntity(name = "policy_api_PartnerPolicySummaryDto")
public class PartnerPolicySummaryDto {

  @JmixGeneratedValue @JmixId private UUID id;

  private String policyNo;

  private LocalDate coverageEnd;

  public String getPolicyNo() {
    return policyNo;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setPolicyNo(String policyNo) {
    this.policyNo = policyNo;
  }

  public LocalDate getCoverageEnd() {
    return coverageEnd;
  }

  public void setCoverageEnd(LocalDate coverageEnd) {
    this.coverageEnd = coverageEnd;
  }

  @InstanceName
  public String instanceName() {
    return policyNo;
  }
}
