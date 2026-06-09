package com.insurance.quote.core.service;

import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.ProductType;
import com.insurance.product.api.dto.ProductVariant;
import com.insurance.quote.core.entity.Quote;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service("quote_QuotePremiumService")
public class QuotePremiumService {

  /**
   * Finds the matching insurance product and sets it plus the calculated premium on the quote. Does
   * not persist – the caller is responsible for saving the entity.
   *
   * @throws IllegalStateException if required fields are missing or no matching product exists
   */
  public BigDecimal calculateAndApply(Quote quote) {
    ProductType productType = quote.getProductType();
    ProductVariant productVariant = quote.getProductVariant();
    LocalDate effectiveDate = quote.getEffectiveDate();

    if (productType == null || productVariant == null || effectiveDate == null) {
      throw new IllegalStateException(
          "Quote " + quote.getQuoteNo() + " is missing product type, variant or effective date");
    }

    InsuranceProduct product =
        InsuranceProduct.findFirstMatchingProduct(productType, productVariant, effectiveDate)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No matching product found for quote " + quote.getQuoteNo()));

    BigDecimal premium = product.calculatePremium(BigDecimal.valueOf(quote.getSquareMeters()));
    quote.setInsuranceProduct(product);
    quote.setCalculatedPremium(premium);
    return premium;
  }
}
