package com.insurance.account.core.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.insurance.common.entity.CommonEntity;

import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity
@Table(name = "ACCOUNT_ACCOUNT", indexes = {
        @Index(name = "IDX_ACCOUNT_ACCOUNT", columnList = "ACCOUNT_NO")
})
@Entity(name = "account_Account")
public class Account extends CommonEntity {

    @Column(name = "POLICY_ID", nullable = false)
    @NotNull
    private UUID policyId;

    @InstanceName
    @Column(name = "ACCOUNT_NO", nullable = false)
    @NotNull
    private String accountNo;

    @Column(name = "ACCOUNT_BALANCE", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal accountBalance;

    @OrderBy("documentDate ASC")
    @Composition
    @OneToMany(mappedBy = "account")
    private List<AccountDocument> documents;

    public UUID getPolicyId() {
        return policyId;
    }

    public void setPolicyId(UUID policyId) {
        this.policyId = policyId;
    }

    public List<AccountDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<AccountDocument> documents) {
        this.documents = documents;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
}
