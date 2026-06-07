package com.insurance.policy.core.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@JmixEntity
@Embeddable
public class PolicyPartnerReference {

  @Column(name = "PARTNER_ID")
  private UUID partnerId;

  @Column(name = "PARTNER_NO", nullable = false)
  @NotNull
  private String partnerNo;

  public UUID getPartnerId() {
    return partnerId;
  }

  public void setPartnerId(UUID partnerId) {
    this.partnerId = partnerId;
  }

  public String getPartnerNo() {
    return partnerNo;
  }

  public void setPartnerNo(String partnerNo) {
    this.partnerNo = partnerNo;
  }

  @InstanceName
  @DependsOnProperties("partnerNo")
  public String instanceName() {
    return partnerNo;
  }
}
