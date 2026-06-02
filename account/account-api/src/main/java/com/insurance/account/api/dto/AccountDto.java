package com.insurance.account.api.dto;

import java.math.BigDecimal;
import java.util.UUID;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity(name = "account_api_AccountDto")
public class AccountDto {

    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private String policyNo;

    private BigDecimal balance;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @InstanceName
    public String instanceName() {
        return policyNo;
    }
}
