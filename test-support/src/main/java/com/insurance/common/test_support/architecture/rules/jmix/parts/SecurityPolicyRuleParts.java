package com.insurance.common.test_support.architecture.rules.jmix.parts;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.insurance.common.test_support.architecture.scan.SecurityPolicyIndex;
import com.insurance.common.test_support.architecture.scan.SecurityPolicyIndex.SecurityPolicyViolation;
import com.tngtech.archunit.lang.ClassesTransformer;
import java.util.List;
import java.util.function.Supplier;

public final class SecurityPolicyRuleParts {

  private SecurityPolicyRuleParts() {}

  public static ClassesTransformer<SecurityPolicyViolation> unknownViewPolicyReferences() {
    return securityPolicyViolations(
        "unknown @ViewPolicy references", SecurityPolicyIndex::unknownViewPolicyIds);
  }

  public static ClassesTransformer<SecurityPolicyViolation> unknownMenuPolicyReferences() {
    return securityPolicyViolations(
        "unknown @MenuPolicy references", SecurityPolicyIndex::unknownMenuPolicyIds);
  }

  public static ClassesTransformer<SecurityPolicyViolation>
      userFacingViewsWithoutConcreteViewPolicy() {
    return securityPolicyViolations(
        "user-facing views without concrete @ViewPolicy",
        SecurityPolicyIndex::userFacingViewIdsWithoutConcreteViewPolicy);
  }

  private static ClassesTransformer<SecurityPolicyViolation> securityPolicyViolations(
      String description, Supplier<List<SecurityPolicyViolation>> violations) {
    return ArchitectureConditions.transformClasses(description, classes -> violations.get());
  }
}
