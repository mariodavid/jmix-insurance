package com.insurance.account.api.dto;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import java.math.BigDecimal;
import java.util.UUID;

@JmixEntity(name = "account_api_PartnerAccountSummaryDto")
public class PartnerAccountSummaryDto {

  @JmixGeneratedValue @JmixId private UUID id;

  private String accountNo;

  private BigDecimal accountBalance;

  public String getAccountNo() {
    return accountNo;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setAccountNo(String accountNo) {
    this.accountNo = accountNo;
  }

  public BigDecimal getAccountBalance() {
    return accountBalance;
  }

  public void setAccountBalance(BigDecimal accountBalance) {
    this.accountBalance = accountBalance;
  }

  @InstanceName
  public String instanceName() {
    return accountNo;
  }
}
