package com.insurance.quote.core.entity;

import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.ProductType;
import com.insurance.product.api.dto.ProductVariant;
import com.insurance.quote.api.dto.QuoteStatus;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@SuppressWarnings({
  "PMD.GodClass",
  "PMD.TooManyFields",
  "PMD.ExcessivePublicCount",
  "PMD.NullAssignment"
})
@JmixEntity
@Table(name = "QUOTE_QUOTE")
@Entity(name = "quote_Quote")
public class Quote {

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
  private QuotePartnerReference partner;

  @Column(name = "QUOTE_NO", nullable = false, unique = true)
  @NotNull
  private String quoteNo;

  @Column(name = "STATUS", nullable = false)
  @NotNull
  private String status;

  @Column(name = "PRODUCT_TYPE", nullable = false)
  @NotNull
  private String productType;

  @Column(name = "PRODUCT_VARIANT", nullable = false)
  @NotNull
  private String productVariant;

  @Column(name = "PAYMENT_FREQUENCY", nullable = false)
  @NotNull
  private String paymentFrequency;

  @Column(name = "ACCEPTED_AT")
  private LocalDateTime acceptedAt;

  @Column(name = "REJECTED_AT")
  private LocalDateTime rejectedAt;

  @EmbeddedParameters(nullAllowed = false)
  @Embedded
  private QuotePolicyReference createdPolicy;

  @Column(name = "INSURANCE_PRODUCT", nullable = false)
  @NotNull
  private String insuranceProduct;

  @Column(name = "EFFECTIVE_DATE", nullable = false)
  @NotNull
  private LocalDate effectiveDate;

  @Column(name = "SQUARE_METERS", nullable = false)
  @NotNull
  private Integer squareMeters;

  @Column(name = "CALCULATED_PREMIUM", nullable = false, precision = 19, scale = 2)
  @NotNull
  private BigDecimal calculatedPremium;

  @Column(name = "VALID_FROM", nullable = false)
  @NotNull
  private LocalDate validFrom;

  @Column(name = "VALID_UNTIL", nullable = false)
  @NotNull
  private LocalDate validUntil;

  public QuotePartnerReference getPartner() {
    return partner;
  }

  public void setPartner(QuotePartnerReference partner) {
    this.partner = partner;
  }

  public QuotePolicyReference getCreatedPolicy() {
    return createdPolicy;
  }

  public void setCreatedPolicy(QuotePolicyReference createdPolicy) {
    this.createdPolicy = createdPolicy;
  }

  public String getPartnerNo() {
    return partner != null ? partner.getPartnerNo() : null;
  }

  public void setPartnerNo(String partnerNo) {
    if (partner != null) {
      partner.setPartnerNo(partnerNo);
    }
  }

  public UUID getPartnerId() {
    return partner != null ? partner.getPartnerId() : null;
  }

  public void setPartnerId(UUID partnerId) {
    if (partner != null) {
      partner.setPartnerId(partnerId);
    }
  }

  public UUID getCreatedPolicyId() {
    return createdPolicy != null ? createdPolicy.getPolicyId() : null;
  }

  public void setCreatedPolicyId(UUID createdPolicyId) {
    if (createdPolicy != null) {
      createdPolicy.setPolicyId(createdPolicyId);
    }
  }

  public String getCreatedPolicyNo() {
    return createdPolicy != null ? createdPolicy.getPolicyNo() : null;
  }

  public void setCreatedPolicyNo(String createdPolicyNo) {
    if (createdPolicy != null) {
      createdPolicy.setPolicyNo(createdPolicyNo);
    }
  }

  public LocalDateTime getRejectedAt() {
    return rejectedAt;
  }

  public void setRejectedAt(LocalDateTime rejectedAt) {
    this.rejectedAt = rejectedAt;
  }

  public LocalDateTime getAcceptedAt() {
    return acceptedAt;
  }

  public void setAcceptedAt(LocalDateTime acceptedAt) {
    this.acceptedAt = acceptedAt;
  }

  public PaymentFrequency getPaymentFrequency() {
    return paymentFrequency == null ? null : PaymentFrequency.fromId(paymentFrequency);
  }

  public void setPaymentFrequency(PaymentFrequency paymentFrequency) {
    this.paymentFrequency = paymentFrequency == null ? null : paymentFrequency.getId();
  }

  public ProductVariant getProductVariant() {
    return productVariant == null ? null : ProductVariant.fromId(productVariant);
  }

  public void setProductVariant(ProductVariant productVariant) {
    this.productVariant = productVariant == null ? null : productVariant.getId();
  }

  public ProductType getProductType() {
    return productType == null ? null : ProductType.fromId(productType);
  }

  public void setProductType(ProductType productType) {
    this.productType = productType == null ? null : productType.getId();
  }

  public Integer getSquareMeters() {
    return squareMeters;
  }

  public void setSquareMeters(Integer squareMeters) {
    this.squareMeters = squareMeters;
  }

  public QuoteStatus getStatus() {
    return status == null ? null : QuoteStatus.fromId(status);
  }

  public void setStatus(QuoteStatus status) {
    this.status = status == null ? null : status.getId();
  }

  public BigDecimal getCalculatedPremium() {
    return calculatedPremium;
  }

  public void setCalculatedPremium(BigDecimal calculatedPremium) {
    this.calculatedPremium = calculatedPremium;
  }

  public LocalDate getEffectiveDate() {
    return effectiveDate;
  }

  public void setEffectiveDate(LocalDate effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public LocalDate getValidUntil() {
    return validUntil;
  }

  public void setValidUntil(LocalDate validUntil) {
    this.validUntil = validUntil;
  }

  public LocalDate getValidFrom() {
    return validFrom;
  }

  public void setValidFrom(LocalDate validFrom) {
    this.validFrom = validFrom;
  }

  public InsuranceProduct getInsuranceProduct() {
    return insuranceProduct == null ? null : InsuranceProduct.fromId(insuranceProduct);
  }

  public void setInsuranceProduct(InsuranceProduct insuranceProduct) {
    this.insuranceProduct = insuranceProduct == null ? null : insuranceProduct.getId();
  }

  public String getQuoteNo() {
    return quoteNo;
  }

  public void setQuoteNo(String quoteNo) {
    this.quoteNo = quoteNo;
  }

  @InstanceName
  public String instanceName() {
    return quoteNo;
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
