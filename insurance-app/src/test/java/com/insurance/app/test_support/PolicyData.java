package com.insurance.app.test_support;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyData(
        String partnerNo,
        String insuranceProductId,
        LocalDate effectiveDate,
        BigDecimal premium,
        String paymentFrequencyId
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String partnerNo = "PT-TEST";
        private String insuranceProductId = "HOME_CONTENT_BASIC_2024_01";
        private LocalDate effectiveDate = LocalDate.of(2025, 1, 1);
        private BigDecimal premium = new BigDecimal("240.00");
        private String paymentFrequencyId = "YEARLY";

        public Builder partnerNo(String v)           { this.partnerNo = v; return this; }
        public Builder insuranceProductId(String v)  { this.insuranceProductId = v; return this; }
        public Builder effectiveDate(LocalDate v)     { this.effectiveDate = v; return this; }
        public Builder premium(BigDecimal v)          { this.premium = v; return this; }
        public Builder paymentFrequencyId(String v)  { this.paymentFrequencyId = v; return this; }

        public PolicyData build() {
            return new PolicyData(partnerNo, insuranceProductId, effectiveDate, premium, paymentFrequencyId);
        }
    }
}
