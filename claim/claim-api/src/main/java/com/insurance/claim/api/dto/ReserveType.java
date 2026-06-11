package com.insurance.claim.api.dto;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ReserveType implements EnumClass<String> {
  COST("COST"),
  RECEIVABLE("RECEIVABLE"),
  INDEMNITY("INDEMNITY");

  private final String id;

  ReserveType(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Nullable
  public static ReserveType fromId(String id) {
    for (ReserveType value : ReserveType.values()) {
      if (value.getId().equals(id)) {
        return value;
      }
    }
    return null;
  }
}
