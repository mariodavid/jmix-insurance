package com.insurance.account.core.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.insurance.common.entity.CommonEntity;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity
@Table(name = "ACCOUNT_ACCOUNT_DOCUMENT", indexes = {
        @Index(name = "IDX_ACCOUNT_ACCOUNT_DOCUMENT_ACCOUNT", columnList = "ACCOUNT_ID")
})
@Entity(name = "account_AccountDocument")
public class AccountDocument extends CommonEntity {

    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Account account;

    @Column(name = "TYPE_", nullable = false)
    @NotNull
    private String type;

    @Column(name = "DOCUMENT_DATE", nullable = false)
    @NotNull
    private LocalDate documentDate;

    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal amount;

    @InstanceName
    @Column(name = "DESCRIPTION")
    private String description;

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public DocumentType getType() {
        return type == null ? null : DocumentType.fromId(type);
    }

    public void setType(DocumentType type) {
        this.type = type == null ? null : type.getId();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
