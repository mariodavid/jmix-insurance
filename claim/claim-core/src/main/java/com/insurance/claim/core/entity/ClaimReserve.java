package com.insurance.claim.core.entity;

import com.insurance.claim.api.dto.ReserveType;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@SuppressWarnings("PMD.NullAssignment")
@JmixEntity
@Table(
    name = "CLAIM_CLAIM_RESERVE",
    indexes = {@Index(name = "IDX_CLAIM_CLAIM_RESERVE_CLAIM", columnList = "CLAIM_ID")},
    uniqueConstraints =
        @UniqueConstraint(
            name = "UNQ_CLAIM_RESERVE_TYPE",
            columnNames = {"CLAIM_ID", "RESERVE_TYPE"}))
@Entity(name = "claim_ClaimReserve")
public class ClaimReserve {

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

  @JoinColumn(name = "CLAIM_ID", nullable = false)
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Claim claim;

  @Column(name = "RESERVE_TYPE", nullable = false)
  @NotNull
  private String reserveType;

  @Column(name = "RESERVE_AMOUNT", nullable = false, precision = 19, scale = 2)
  @NotNull
  @DecimalMin(value = "0", inclusive = false)
  private BigDecimal reserveAmount;

  @InstanceName
  @Column(name = "REASON", nullable = false)
  @NotNull
  private String reason;

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

  public Claim getClaim() {
    return claim;
  }

  public void setClaim(Claim claim) {
    this.claim = claim;
  }

  public ReserveType getReserveType() {
    return reserveType == null ? null : ReserveType.fromId(reserveType);
  }

  public void setReserveType(ReserveType reserveType) {
    this.reserveType = reserveType == null ? null : reserveType.getId();
  }

  public BigDecimal getReserveAmount() {
    return reserveAmount;
  }

  public void setReserveAmount(BigDecimal reserveAmount) {
    this.reserveAmount = reserveAmount;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
