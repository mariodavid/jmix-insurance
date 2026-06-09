package com.insurance.common.test_support.architecture.rules.layer.parts;

import static com.insurance.common.test_support.architecture.project.ArchitectureProject.*;
import static com.tngtech.archunit.lang.conditions.ArchConditions.onlyHaveDependenciesWhere;

import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import java.util.Optional;

public final class ModuleLayerRuleParts {

  private ModuleLayerRuleParts() {}

  public static DescribedPredicate<JavaClass> domainUiClasses() {
    return DescribedPredicate.describe(
        "domain UI classes", javaClass -> domainUiOwner(javaClass).isPresent());
  }

  public static ArchCondition<JavaClass> notUseForeignCoreImplementations() {
    return onlyHaveDependenciesWhere(
        DescribedPredicate.describe(
            "not use foreign core implementations",
            dependency ->
                domainUiOwner(dependency.getOriginClass())
                    .map(sourceModule -> !targetsForeignCore(sourceModule, dependency))
                    .orElse(true)));
  }

  private static Optional<ArchitectureSlice> domainUiOwner(JavaClass javaClass) {
    return ArchitectureSlice.domainSlices().stream()
        .filter(slice -> isDomainUiPackage(javaClass.getPackageName(), slice))
        .findFirst();
  }

  private static boolean targetsForeignCore(ArchitectureSlice sourceSlice, Dependency dependency) {
    return ArchitectureSlice.domainSlices().stream()
        .filter(slice -> slice != sourceSlice)
        .anyMatch(
            slice -> isDomainCorePackage(dependency.getTargetClass().getPackageName(), slice));
  }
}
