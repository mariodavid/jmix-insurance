package com.insurance.account.core.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum DocumentType implements EnumClass<String> {
  CREDIT("CREDIT"),
  DEBIT("DEBIT");

  private final String id;

  DocumentType(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Nullable
  public static DocumentType fromId(String id) {
    for (DocumentType at : DocumentType.values()) {
      if (at.getId().equals(id)) {
        return at;
      }
    }
    return null;
  }
}
