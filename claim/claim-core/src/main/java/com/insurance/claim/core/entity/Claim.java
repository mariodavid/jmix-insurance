package com.insurance.claim.core.entity;

import com.insurance.claim.api.dto.ClaimStatus;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
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
@Table(name = "CLAIM_CLAIM")
@Entity(name = "claim_Claim")
public class Claim {

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

  @InstanceName
  @Column(name = "CLAIM_NO", nullable = false, unique = true)
  @NotNull
  private String claimNo;

  @EmbeddedParameters(nullAllowed = false)
  @Embedded
  @NotNull
  private ClaimPolicyReference policy;

  @Column(name = "DATE_OF_LOSS", nullable = false)
  @NotNull
  private LocalDate dateOfLoss;

  @Column(name = "DESCRIPTION")
  private String description;

  @Column(name = "ESTIMATED_AMOUNT", nullable = false, precision = 19, scale = 2)
  @NotNull
  @DecimalMin(value = "0.01", message = "Estimated amount must be greater than zero")
  private BigDecimal estimatedAmount;

  @Column(name = "STATUS", nullable = false)
  @NotNull
  private String status;

  @OrderBy("type ASC")
  @Composition
  @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Reserve> reserves;

  @OrderBy("paymentDate DESC")
  @Composition
  @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Payment> payments;

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

  public String getClaimNo() {
    return claimNo;
  }

  public void setClaimNo(String claimNo) {
    this.claimNo = claimNo;
  }

  public ClaimPolicyReference getPolicy() {
    return policy;
  }

  public void setPolicy(ClaimPolicyReference policy) {
    this.policy = policy;
  }

  public String getPolicyNo() {
    return policy != null ? policy.getPolicyNo() : null;
  }

  public String getPartnerNo() {
    return policy != null ? policy.getPartnerNo() : null;
  }

  public LocalDate getDateOfLoss() {
    return dateOfLoss;
  }

  public void setDateOfLoss(LocalDate dateOfLoss) {
    this.dateOfLoss = dateOfLoss;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getEstimatedAmount() {
    return estimatedAmount;
  }

  public void setEstimatedAmount(BigDecimal estimatedAmount) {
    this.estimatedAmount = estimatedAmount;
  }

  public ClaimStatus getStatus() {
    return status == null ? null : ClaimStatus.fromId(status);
  }

  @SuppressWarnings("PMD.NullAssignment")
  public void setStatus(ClaimStatus status) {
    this.status = status == null ? null : status.getId();
  }

  public List<Reserve> getReserves() {
    return reserves;
  }

  public void setReserves(List<Reserve> reserves) {
    this.reserves = reserves;
  }

  public List<Payment> getPayments() {
    return payments;
  }

  public void setPayments(List<Payment> payments) {
    this.payments = payments;
  }
}
