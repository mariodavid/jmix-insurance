package com.insurance.policy.core.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.InstanceName;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.InsuranceProduct;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "POLICY_POLICY")
@Entity(name = "policy_Policy")
public class Policy {

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

    @Column(name = "PARTNER_NO", nullable = false)
    @NotNull
    private String partnerNo;

    @Column(name = "INSURANCE_PRODUCT", nullable = false)
    @NotNull
    private String insuranceProduct;

    @Column(name = "POLICY_NO", nullable = false, unique = true)
    @NotNull
    private String policyNo;

    @Column(name = "COVERAGE_START", nullable = false)
    @NotNull
    private LocalDate coverageStart;

    @Column(name = "COVERAGE_END")
    private LocalDate coverageEnd;

    @Column(name = "PREMIUM", nullable = false, precision = 19, scale = 2)
    @NotNull
    private BigDecimal premium;

    @Column(name = "PAYMENT_FREQUENCY", nullable = false)
    @NotNull
    private String paymentFrequency;

    public String getPartnerNo() {
        return partnerNo;
    }

    public void setPartnerNo(String partnerNo) {
        this.partnerNo = partnerNo;
    }

    public PaymentFrequency getPaymentFrequency() {
        return paymentFrequency == null ? null : PaymentFrequency.fromId(paymentFrequency);
    }

    public void setPaymentFrequency(PaymentFrequency paymentFrequency) {
        this.paymentFrequency = paymentFrequency == null ? null : paymentFrequency.getId();
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public InsuranceProduct getInsuranceProduct() {
        return insuranceProduct == null ? null : InsuranceProduct.fromId(insuranceProduct);
    }

    public void setInsuranceProduct(InsuranceProduct insuranceProduct) {
        this.insuranceProduct = insuranceProduct == null ? null : insuranceProduct.getId();
    }

    public LocalDate getCoverageEnd() {
        return coverageEnd;
    }

    public void setCoverageEnd(LocalDate coverageEnd) {
        this.coverageEnd = coverageEnd;
    }

    public LocalDate getCoverageStart() {
        return coverageStart;
    }

    public void setCoverageStart(LocalDate coverageStart) {
        this.coverageStart = coverageStart;
    }

    @InstanceName
    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
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
