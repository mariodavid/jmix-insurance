package com.insurance.quote.core.entity;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@JmixEntity
@Embeddable
public class QuotePolicyReference {

  @Column(name = "CREATED_POLICY_ID")
  private UUID policyId;

  @Column(name = "CREATED_POLICY_NO")
  private String policyNo;

  public UUID getPolicyId() {
    return policyId;
  }

  public void setPolicyId(UUID policyId) {
    this.policyId = policyId;
  }

  public String getPolicyNo() {
    return policyNo;
  }

  public void setPolicyNo(String policyNo) {
    this.policyNo = policyNo;
  }

  @InstanceName
  @DependsOnProperties("policyNo")
  public String instanceName() {
    return policyNo;
  }
}
