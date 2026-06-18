package com.insurance.claim.api.dto;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ReserveType implements EnumClass<String> {
  COMPENSATION("COMPENSATION"),
  COST("COST");

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
    for (ReserveType at : values()) {
      if (at.getId().equals(id)) {
        return at;
      }
    }
    return null;
  }
}
