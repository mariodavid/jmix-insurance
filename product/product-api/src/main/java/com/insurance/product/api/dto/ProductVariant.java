package com.insurance.product.api.dto;

import io.jmix.core.metamodel.datatype.EnumClass;
import java.math.BigDecimal;
import org.springframework.lang.Nullable;

public enum ProductVariant implements EnumClass<String> {
  SMALL("SMALL", new BigDecimal("100.00"), new BigDecimal("2.00")),
  MEDIUM("MEDIUM", new BigDecimal("150.00"), new BigDecimal("2.50")),
  LARGE("LARGE", new BigDecimal("200.00"), new BigDecimal("3.00"));

  private final String id;
  private final BigDecimal basePremium;
  private final BigDecimal factor;

  ProductVariant(String id, BigDecimal basePremium, BigDecimal factor) {
    this.id = id;
    this.basePremium = basePremium;
    this.factor = factor;
  }

  public String getId() {
    return id;
  }

  @Nullable
  public static ProductVariant fromId(String id) {
    for (ProductVariant at : ProductVariant.values()) {
      if (at.getId().equals(id)) {
        return at;
      }
    }
    return null;
  }

  public BigDecimal getBasePremium() {
    return basePremium;
  }

  public BigDecimal getFactor() {
    return factor;
  }
}
