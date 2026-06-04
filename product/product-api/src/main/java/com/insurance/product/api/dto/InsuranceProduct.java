package com.insurance.product.api.dto;

import io.jmix.core.metamodel.datatype.EnumClass;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.lang.Nullable;

public enum InsuranceProduct implements EnumClass<String> {
  HOME_CONTENT_DELUXE_2024_01(
      "HOME_CONTENT_DELUXE_2024_01",
      ProductType.HOME_CONTENT,
      ProductVariant.LARGE,
      LocalDate.of(2024, 1, 1),
      LocalDate.of(2024, 12, 31)),
  HOME_CONTENT_DELUXE_2025_01(
      "HOME_CONTENT_DELUXE_2025_01",
      ProductType.HOME_CONTENT,
      ProductVariant.LARGE,
      LocalDate.of(2025, 1, 1),
      null),
  HOME_CONTENT_STANDARD_2024_01(
      "HOME_CONTENT_STANDARD_2024_01",
      ProductType.HOME_CONTENT,
      ProductVariant.MEDIUM,
      LocalDate.of(2023, 6, 1),
      null),
  HOME_CONTENT_BASIC_2024_01(
      "HOME_CONTENT_BASIC_2024_01",
      ProductType.HOME_CONTENT,
      ProductVariant.SMALL,
      LocalDate.of(2022, 1, 1),
      null);

  private final String id;
  private final ProductType productType;
  private final ProductVariant variant;
  private final LocalDate validFrom;
  private final LocalDate validUntil;

  InsuranceProduct(
      String id,
      ProductType productType,
      ProductVariant variant,
      LocalDate validFrom,
      LocalDate validUntil) {
    this.id = id;
    this.productType = productType;
    this.variant = variant;
    this.validFrom = validFrom;
    this.validUntil = validUntil;
  }

  public String getId() {
    return id;
  }

  public ProductType getProductType() {
    return productType;
  }

  public ProductVariant getVariant() {
    return variant;
  }

  public LocalDate getValidFrom() {
    return validFrom;
  }

  public LocalDate getValidUntil() {
    return validUntil;
  }

  /**
   * Find the first matching insurance product based on product type, variant, and a given
   * effectiveDate.
   *
   * @param productType The type of insurance (e.g., HOME_CONTENT).
   * @param variant The product variant (e.g., SMALL, MEDIUM, LARGE).
   * @param effectiveDate The effectiveDate for which the product should be valid.
   * @return An Optional containing the first matching InsuranceProduct, or an empty Optional if
   *     none found.
   */
  public static Optional<InsuranceProduct> findFirstMatchingProduct(
      ProductType productType, ProductVariant variant, LocalDate effectiveDate) {
    return Arrays.stream(InsuranceProduct.values())
        .filter(product -> product.getProductType() == productType)
        .filter(product -> product.getVariant() == variant)
        .filter(product -> !effectiveDate.isBefore(product.getValidFrom()))
        .filter(
            product ->
                product.getValidUntil() == null
                    || !effectiveDate.isAfter(
                        product.getValidUntil())) // validUntil == null → unlimited validity
        .findFirst();
  }

  public BigDecimal calculatePremium(BigDecimal squareMeters) {
    return variant.getBasePremium().add(squareMeters.multiply(variant.getFactor()));
  }

  @Nullable
  public static InsuranceProduct fromId(String id) {
    for (InsuranceProduct product : InsuranceProduct.values()) {
      if (product.getId().equals(id)) {
        return product;
      }
    }
    return null;
  }
}
