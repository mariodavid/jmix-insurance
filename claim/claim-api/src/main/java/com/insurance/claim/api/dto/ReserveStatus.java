package com.insurance.claim.api.dto;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ReserveStatus implements EnumClass<String> {
  PENDING("PENDING"),
  APPROVED("APPROVED");

  private final String id;

  ReserveStatus(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Nullable
  public static ReserveStatus fromId(String id) {
    for (ReserveStatus value : ReserveStatus.values()) {
      if (value.getId().equals(id)) {
        return value;
      }
    }
    return null;
  }
}
