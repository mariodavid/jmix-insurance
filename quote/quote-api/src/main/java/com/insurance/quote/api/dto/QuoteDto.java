package com.insurance.quote.api.dto;

import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.ProductType;
import com.insurance.product.api.dto.ProductVariant;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyFields", "PMD.NullAssignment"})
@JmixEntity(name = "quote_api_QuoteDto")
public class QuoteDto {

  @JmixGeneratedValue @JmixId private UUID id;

  private String partnerNo;

  private String quoteNo;

  private String status;

  private String productType;

  private String productVariant;

  private String paymentFrequency;

  private String insuranceProduct;

  private LocalDate effectiveDate;

  private Integer squareMeters;

  private BigDecimal calculatedPremium;

  private LocalDate validFrom;

  private LocalDate validUntil;

  private String createdPolicyNo;

  private UUID createdPolicyId;

  private LocalDateTime acceptedAt;

  private LocalDateTime rejectedAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getPartnerNo() {
    return partnerNo;
  }

  public void setPartnerNo(String partnerNo) {
    this.partnerNo = partnerNo;
  }

  public String getQuoteNo() {
    return quoteNo;
  }

  public void setQuoteNo(String quoteNo) {
    this.quoteNo = quoteNo;
  }

  public QuoteStatus getStatus() {
    return status == null ? null : QuoteStatus.fromId(status);
  }

  public void setStatus(QuoteStatus status) {
    this.status = status == null ? null : status.getId();
  }

  public ProductType getProductType() {
    return productType == null ? null : ProductType.fromId(productType);
  }

  public void setProductType(ProductType productType) {
    this.productType = productType == null ? null : productType.getId();
  }

  public ProductVariant getProductVariant() {
    return productVariant == null ? null : ProductVariant.fromId(productVariant);
  }

  public void setProductVariant(ProductVariant productVariant) {
    this.productVariant = productVariant == null ? null : productVariant.getId();
  }

  public PaymentFrequency getPaymentFrequency() {
    return paymentFrequency == null ? null : PaymentFrequency.fromId(paymentFrequency);
  }

  public void setPaymentFrequency(PaymentFrequency paymentFrequency) {
    this.paymentFrequency = paymentFrequency == null ? null : paymentFrequency.getId();
  }

  public InsuranceProduct getInsuranceProduct() {
    return insuranceProduct == null ? null : InsuranceProduct.fromId(insuranceProduct);
  }

  public void setInsuranceProduct(InsuranceProduct insuranceProduct) {
    this.insuranceProduct = insuranceProduct == null ? null : insuranceProduct.getId();
  }

  public LocalDate getEffectiveDate() {
    return effectiveDate;
  }

  public void setEffectiveDate(LocalDate effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public Integer getSquareMeters() {
    return squareMeters;
  }

  public void setSquareMeters(Integer squareMeters) {
    this.squareMeters = squareMeters;
  }

  public BigDecimal getCalculatedPremium() {
    return calculatedPremium;
  }

  public void setCalculatedPremium(BigDecimal calculatedPremium) {
    this.calculatedPremium = calculatedPremium;
  }

  public LocalDate getValidFrom() {
    return validFrom;
  }

  public void setValidFrom(LocalDate validFrom) {
    this.validFrom = validFrom;
  }

  public LocalDate getValidUntil() {
    return validUntil;
  }

  public void setValidUntil(LocalDate validUntil) {
    this.validUntil = validUntil;
  }

  public String getCreatedPolicyNo() {
    return createdPolicyNo;
  }

  public void setCreatedPolicyNo(String createdPolicyNo) {
    this.createdPolicyNo = createdPolicyNo;
  }

  public UUID getCreatedPolicyId() {
    return createdPolicyId;
  }

  public void setCreatedPolicyId(UUID createdPolicyId) {
    this.createdPolicyId = createdPolicyId;
  }

  public LocalDateTime getAcceptedAt() {
    return acceptedAt;
  }

  public void setAcceptedAt(LocalDateTime acceptedAt) {
    this.acceptedAt = acceptedAt;
  }

  public LocalDateTime getRejectedAt() {
    return rejectedAt;
  }

  public void setRejectedAt(LocalDateTime rejectedAt) {
    this.rejectedAt = rejectedAt;
  }

  @InstanceName
  public String instanceName() {
    return quoteNo;
  }
}
