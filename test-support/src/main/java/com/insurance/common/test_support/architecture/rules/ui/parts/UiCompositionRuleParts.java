package com.insurance.common.test_support.architecture.rules.ui.parts;

import static com.tngtech.archunit.lang.conditions.ArchConditions.onlyHaveDependenciesWhere;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.insurance.common.test_support.architecture.scan.SourceBoundaryReferences;
import com.insurance.common.test_support.architecture.scan.SourceBoundaryReferences.ForeignClassStringReference;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ClassesTransformer;
import java.util.Optional;

public final class UiCompositionRuleParts {

  private UiCompositionRuleParts() {}

  public static ClassesTransformer<ForeignClassStringReference> domainUiXmlFiles() {
    return ArchitectureConditions.transformClasses(
        "domain UI XML files", classes -> SourceBoundaryReferences.foreignUiXmlClassReferences());
  }

  public static DescribedPredicate<JavaClass> domainUiClasses() {
    return DescribedPredicate.describe(
        "domain UI classes", javaClass -> domainUiOwner(javaClass).isPresent());
  }

  public static ArchCondition<JavaClass> notDependOnForeignUiImplementationClasses() {
    return onlyHaveDependenciesWhere(
        DescribedPredicate.describe(
            "not depend on foreign UI implementation classes",
            dependency -> {
              JavaClass sourceClass = dependency.getOriginClass();
              return domainUiOwner(sourceClass)
                  .map(sourceModule -> !targetsForeignUiImplementation(sourceModule, dependency))
                  .orElse(true);
            }));
  }

  private static Optional<ArchitectureSlice> domainUiOwner(JavaClass javaClass) {
    return ArchitectureSlice.domainSlices().stream()
        .filter(slice -> ArchitectureProject.isDomainUiPackage(javaClass.getPackageName(), slice))
        .findFirst();
  }

  private static boolean targetsForeignUiImplementation(
      ArchitectureSlice sourceSlice, Dependency dependency) {
    String targetPackage = dependency.getTargetClass().getPackageName();
    return ArchitectureSlice.domainSlices().stream()
        .filter(slice -> slice != sourceSlice)
        .anyMatch(
            slice -> ArchitectureProject.isDomainUiImplementationPackage(targetPackage, slice));
  }

  public static DescribedPredicate<JavaClass> domainCoreOrUiImplementationClasses() {
    return DescribedPredicate.describe(
        "domain core or domain UI implementation classes",
        javaClass -> {
          String packageName = javaClass.getPackageName();
          return ArchitectureProject.isDomainCorePackage(packageName)
              || ArchitectureProject.isDomainUiImplementationPackage(packageName);
        });
  }
}
