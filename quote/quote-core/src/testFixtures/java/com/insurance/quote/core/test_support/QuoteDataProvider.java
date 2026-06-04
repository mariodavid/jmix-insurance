package com.insurance.quote.core.test_support;

import com.insurance.common.test_support.TestDataProvider;
import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.ProductType;
import com.insurance.product.api.dto.ProductVariant;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.core.entity.Quote;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class QuoteDataProvider implements TestDataProvider<Quote> {

  @Override
  public Class<Quote> getEntityClass() {
    return Quote.class;
  }

  @Override
  public void accept(Quote quote) {
    LocalDate effectiveDate = LocalDate.of(2025, 1, 1);
    quote.setPartnerNo("PT-" + UUID.randomUUID().toString().substring(0, 5));
    quote.setQuoteNo("QT-" + UUID.randomUUID().toString().substring(0, 8));
    quote.setStatus(QuoteStatus.PENDING);
    quote.setProductType(ProductType.HOME_CONTENT);
    quote.setProductVariant(ProductVariant.SMALL);
    quote.setPaymentFrequency(PaymentFrequency.YEARLY);
    quote.setInsuranceProduct(InsuranceProduct.HOME_CONTENT_BASIC_2024_01);
    quote.setEffectiveDate(effectiveDate);
    quote.setSquareMeters(60);
    quote.setCalculatedPremium(new BigDecimal("220.00"));
    quote.setValidFrom(effectiveDate);
    quote.setValidUntil(effectiveDate.plusDays(14));
  }
}
