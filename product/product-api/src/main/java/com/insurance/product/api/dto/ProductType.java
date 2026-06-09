package com.insurance.product.api.dto;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ProductType implements EnumClass<String> {
  HOME_CONTENT("HOME_CONTENT", "HC");

  private final String id;
  private final String name;

  ProductType(String id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public String getId() {
    return id;
  }

  @Nullable
  public static ProductType fromId(String id) {
    for (ProductType at : values()) {
      if (at.getId().equals(id)) {
        return at;
      }
    }
    return null;
  }

  public String getName() {
    return name;
  }
}
