package com.insurance.common.test_support.architecture.rules.jmix.parts;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.insurance.common.test_support.architecture.scan.SourceBoundaryReferences;
import com.insurance.common.test_support.architecture.scan.SourceBoundaryReferences.ForeignEntityNameReference;
import com.tngtech.archunit.lang.ClassesTransformer;

public final class PersistentEntityNameRuleParts {

  private PersistentEntityNameRuleParts() {}

  public static ClassesTransformer<ForeignEntityNameReference>
      foreignPersistentEntityNameReferences() {
    return ArchitectureConditions.transformClasses(
        "foreign persistent entity-name references",
        classes -> SourceBoundaryReferences.foreignPersistentEntityNameReferences());
  }
}
