package com.insurance.claim.core.entity;

import com.insurance.claim.api.dto.ClaimStatus;
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

@SuppressWarnings("PMD.NullAssignment")
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

  @Column(name = "REPORT_DATE", nullable = false)
  @NotNull
  private LocalDate reportDate;

  @Column(name = "INCIDENT_DATE", nullable = false)
  @NotNull
  private LocalDate incidentDate;

  @Column(name = "DESCRIPTION", length = 1000)
  private String description;

  @Column(name = "CLAIM_STATUS", nullable = false)
  @NotNull
  private String claimStatus;

  @Column(name = "EXPECTED_CLAIM_AMOUNT", precision = 19, scale = 2)
  private BigDecimal expectedClaimAmount;

  @OrderBy("reserveType ASC")
  @Composition
  @OneToMany(mappedBy = "claim")
  private List<ClaimReserve> reserves;

  @OrderBy("paymentDate DESC")
  @Composition
  @OneToMany(mappedBy = "claim")
  private List<ClaimPayment> payments;

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

  public UUID getPolicyId() {
    return policy != null ? policy.getPolicyId() : null;
  }

  public String getPolicyNo() {
    return policy != null ? policy.getPolicyNo() : null;
  }

  public LocalDate getReportDate() {
    return reportDate;
  }

  public void setReportDate(LocalDate reportDate) {
    this.reportDate = reportDate;
  }

  public LocalDate getIncidentDate() {
    return incidentDate;
  }

  public void setIncidentDate(LocalDate incidentDate) {
    this.incidentDate = incidentDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ClaimStatus getClaimStatus() {
    return claimStatus == null ? null : ClaimStatus.fromId(claimStatus);
  }

  public void setClaimStatus(ClaimStatus claimStatus) {
    this.claimStatus = claimStatus == null ? null : claimStatus.getId();
  }

  public BigDecimal getExpectedClaimAmount() {
    return expectedClaimAmount;
  }

  public void setExpectedClaimAmount(BigDecimal expectedClaimAmount) {
    this.expectedClaimAmount = expectedClaimAmount;
  }

  public List<ClaimReserve> getReserves() {
    return reserves;
  }

  public void setReserves(List<ClaimReserve> reserves) {
    this.reserves = reserves;
  }

  public List<ClaimPayment> getPayments() {
    return payments;
  }

  public void setPayments(List<ClaimPayment> payments) {
    this.payments = payments;
  }
}
