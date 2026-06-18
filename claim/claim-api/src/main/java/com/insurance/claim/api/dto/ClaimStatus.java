package com.insurance.claim.api.dto;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ClaimStatus implements EnumClass<String> {
  OPEN("OPEN"),
  IN_PROGRESS("IN_PROGRESS"),
  CLOSED("CLOSED");

  private final String id;

  ClaimStatus(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Nullable
  public static ClaimStatus fromId(String id) {
    for (ClaimStatus at : values()) {
      if (at.getId().equals(id)) {
        return at;
      }
    }
    return null;
  }
}
