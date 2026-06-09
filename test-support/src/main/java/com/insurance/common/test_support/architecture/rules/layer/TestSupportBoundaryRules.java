package com.insurance.common.test_support.architecture.rules.layer;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Production and Test Support isolation rules.
 *
 * <p>Validates that main application production code (excluding the test support code itself) does
 * not depend on any test support helper classes from the {@code test-support} module.
 */
public class TestSupportBoundaryRules {

  public static DescribedPredicate<JavaClass> productionApplicationClasses() {
    return DescribedPredicate.describe(
        "production application classes",
        input ->
            !input.getPackageName().startsWith(ArchitectureProject.testSupportPackage())
                && !input.getPackageName().contains(".test_support"));
  }

  @ArchTest
  public static final ArchRule productionCodeDoesNotUseTestSupport =
      noClasses()
          .that(productionApplicationClasses())
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(ArchitectureProject.testSupportPackagePattern())
          .as("production code should not depend on test support");
}
