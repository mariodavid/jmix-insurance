package com.insurance.common.test_support.architecture.rules.entity.parts;

import static com.tngtech.archunit.lang.conditions.ArchConditions.onlyHaveDependenciesWhere;

import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import java.util.Optional;

public final class PersistentEntityDependencyRuleParts {

  private PersistentEntityDependencyRuleParts() {}

  public static DescribedPredicate<JavaClass> persistentDomainModels() {
    return DescribedPredicate.describe(
        "persistent domain models in core entity packages",
        javaClass ->
            entityPackageOwner(javaClass).isPresent() && isPersistentDomainModel(javaClass));
  }

  public static ArchCondition<JavaClass> notDependOnForeignEntityPackages() {
    return onlyHaveDependenciesWhere(
        DescribedPredicate.describe(
            "not depend on foreign entity packages",
            dependency ->
                entityPackageOwner(dependency.getOriginClass())
                    .map(sourceModule -> !targetsForeignEntityPackage(sourceModule, dependency))
                    .orElse(true)));
  }

  private static boolean isPersistentDomainModel(JavaClass javaClass) {
    return javaClass.isAnnotatedWith(jakarta.persistence.Entity.class)
        || javaClass.isAnnotatedWith(jakarta.persistence.Embeddable.class);
  }

  private static Optional<ArchitectureSlice> entityPackageOwner(JavaClass javaClass) {
    return ArchitectureSlice.entityBoundarySlices().stream()
        .filter(slice -> ArchitectureProject.isEntityPackage(javaClass.getPackageName(), slice))
        .findFirst();
  }

  private static boolean targetsForeignEntityPackage(
      ArchitectureSlice sourceSlice, Dependency dependency) {
    String targetPackage = dependency.getTargetClass().getPackageName();
    return ArchitectureSlice.entityBoundarySlices().stream()
        .filter(slice -> slice != sourceSlice)
        .anyMatch(slice -> ArchitectureProject.isEntityPackage(targetPackage, slice));
  }
}
