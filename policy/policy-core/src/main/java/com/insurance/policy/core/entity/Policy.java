package com.insurance.policy.core.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.insurance.common.entity.CommonEntity;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.InsuranceProduct;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.InstanceName;

@JmixEntity
@Table(name = "POLICY_POLICY")
@Entity(name = "policy_Policy")
public class Policy extends CommonEntity {

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
}
