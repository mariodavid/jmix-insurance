package com.insurance.account.core.entity;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@JmixEntity
@Table(
    name = "ACCOUNT_ACCOUNT",
    indexes = {
      @Index(name = "IDX_ACCOUNT_ACCOUNT_POLICY_NO", columnList = "POLICY_NO"),
      @Index(name = "IDX_ACCOUNT_ACCOUNT_PARTNER_NO", columnList = "PARTNER_NO")
    })
@Entity(name = "account_Account")
public class Account {

  @JmixGeneratedValue
  @Column(name = "ID", nullable = false)
  @Id
  private UUID id;

  @Column(name = "VERSION", nullable = false)
  @Version
  private Integer version;

  @CreatedBy
  @Column(name = "CREATED_BY")
  private String createdBy;

  @CreatedDate
  @Column(name = "CREATED_DATE")
  private OffsetDateTime createdDate;

  @LastModifiedBy
  @Column(name = "LAST_MODIFIED_BY")
  private String lastModifiedBy;

  @LastModifiedDate
  @Column(name = "LAST_MODIFIED_DATE")
  private OffsetDateTime lastModifiedDate;

  @DeletedBy
  @Column(name = "DELETED_BY")
  private String deletedBy;

  @DeletedDate
  @Column(name = "DELETED_DATE")
  private OffsetDateTime deletedDate;

  @EmbeddedParameters(nullAllowed = false)
  @Embedded
  @NotNull
  private AccountPolicyReference policy;

  @Column(name = "ACCOUNTING_PERIOD_START", nullable = false)
  @NotNull
  private LocalDate accountingPeriodStart;

  @Column(name = "ACCOUNTING_PERIOD_END", nullable = false)
  @NotNull
  private LocalDate accountingPeriodEnd;

  @Column(name = "ACCOUNT_BALANCE", nullable = false, precision = 19, scale = 2)
  @NotNull
  private BigDecimal accountBalance;

  @OrderBy("documentDate ASC")
  @Composition
  @OneToMany(mappedBy = "account")
  private List<AccountDocument> documents;

  public AccountPolicyReference getPolicy() {
    return policy;
  }

  public void setPolicy(AccountPolicyReference policy) {
    this.policy = policy;
  }

  public LocalDate getAccountingPeriodStart() {
    return accountingPeriodStart;
  }

  public void setAccountingPeriodStart(LocalDate accountingPeriodStart) {
    this.accountingPeriodStart = accountingPeriodStart;
  }

  public LocalDate getAccountingPeriodEnd() {
    return accountingPeriodEnd;
  }

  public void setAccountingPeriodEnd(LocalDate accountingPeriodEnd) {
    this.accountingPeriodEnd = accountingPeriodEnd;
  }

  public UUID getPolicyId() {
    return policy != null ? policy.getPolicyId() : null;
  }

  @InstanceName
  public String getAccountNo() {
    return policy != null ? policy.getPolicyNo() : null;
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

  public String getPartnerNo() {
    return policy != null ? policy.getPartnerNo() : null;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public OffsetDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(OffsetDateTime createdDate) {
    this.createdDate = createdDate;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public OffsetDateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public String getDeletedBy() {
    return deletedBy;
  }

  public void setDeletedBy(String deletedBy) {
    this.deletedBy = deletedBy;
  }

  public OffsetDateTime getDeletedDate() {
    return deletedDate;
  }

  public void setDeletedDate(OffsetDateTime deletedDate) {
    this.deletedDate = deletedDate;
  }
}
