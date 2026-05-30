package com.insurance.app.test_support;

import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.ProductType;
import com.insurance.product.api.dto.ProductVariant;
import com.insurance.quote.api.dto.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteData(
        String partnerNo,
        QuoteStatus status,
        ProductType productType,
        ProductVariant productVariant,
        PaymentFrequency paymentFrequency,
        InsuranceProduct insuranceProduct,
        LocalDate effectiveDate,
        Integer squareMeters,
        BigDecimal calculatedPremium,
        LocalDate validFrom,
        LocalDate validUntil
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String partnerNo = "PT-00001";
        private QuoteStatus status = QuoteStatus.PENDING;
        private ProductType productType = ProductType.HOME_CONTENT;
        private ProductVariant productVariant = ProductVariant.SMALL;
        private PaymentFrequency paymentFrequency = PaymentFrequency.YEARLY;
        private InsuranceProduct insuranceProduct = InsuranceProduct.HOME_CONTENT_BASIC_2024_01;
        private LocalDate effectiveDate = LocalDate.of(2025, 1, 1);
        private Integer squareMeters = 60;
        private BigDecimal calculatedPremium = new BigDecimal("220.00");
        private LocalDate validFrom = LocalDate.of(2025, 1, 1);
        private LocalDate validUntil = LocalDate.of(2025, 12, 31);

        public Builder partnerNo(String v)               { this.partnerNo = v; return this; }
        public Builder status(QuoteStatus v)             { this.status = v; return this; }
        public Builder productType(ProductType v)        { this.productType = v; return this; }
        public Builder productVariant(ProductVariant v)  { this.productVariant = v; return this; }
        public Builder paymentFrequency(PaymentFrequency v) { this.paymentFrequency = v; return this; }
        public Builder insuranceProduct(InsuranceProduct v) { this.insuranceProduct = v; return this; }
        public Builder effectiveDate(LocalDate v)        { this.effectiveDate = v; return this; }
        public Builder squareMeters(Integer v)           { this.squareMeters = v; return this; }
        public Builder calculatedPremium(BigDecimal v)   { this.calculatedPremium = v; return this; }
        public Builder validFrom(LocalDate v)            { this.validFrom = v; return this; }
        public Builder validUntil(LocalDate v)           { this.validUntil = v; return this; }

        public QuoteData build() {
            return new QuoteData(partnerNo, status, productType, productVariant, paymentFrequency,
                    insuranceProduct, effectiveDate, squareMeters, calculatedPremium, validFrom, validUntil);
        }
    }
}
