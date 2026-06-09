package com.insurance.common.test_support.architecture.rules.slice;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.insurance.common.test_support.architecture.project.ArchitectureLayer;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Package structure conventions for non-UI Jmix and Spring artifacts.
 *
 * <p>Ensures services, listeners, security roles, and configurations are placed in their designated
 * architectural packages to preserve modular monotonicity. Flow UI-specific conventions live in
 * {@code test-support-ui}, where compile-time dependencies on Jmix Flow UI annotations are
 * legitimate.
 */
public class ModulePackageConventionRules {

  private static DescribedPredicate<JavaClass> haveMethodAnnotatedWithEventListener() {
    return DescribedPredicate.describe(
        "have a method annotated with @EventListener",
        input ->
            input.getMethods().stream()
                .anyMatch(
                    method ->
                        method.isAnnotatedWith("org.springframework.context.event.EventListener")));
  }

  @ArchTest
  public static final ArchRule servicesResideInCoreServicePackage =
      classes()
          .that()
          .areAnnotatedWith("org.springframework.stereotype.Service")
          .should()
          .resideInAnyPackage(
              ArchitectureProject.anySliceLayerSubpackage(ArchitectureLayer.CORE, "service.."))
          .as("Service classes should reside in core.service packages");

  @ArchTest
  public static final ArchRule entityEventListenersResideInCoreListenerPackage =
      classes()
          .that()
          .areAnnotatedWith("org.springframework.stereotype.Component")
          .and(haveMethodAnnotatedWithEventListener())
          .should()
          .resideInAnyPackage(
              ArchitectureProject.anySliceLayerSubpackage(ArchitectureLayer.CORE, "listener.."))
          .as("Entity event listener classes should reside in core.listener packages");

  @ArchTest
  public static final ArchRule resourceRolesResideInCorrectSecurityPackages =
      classes()
          .that()
          .areAnnotatedWith("io.jmix.security.role.annotation.ResourceRole")
          .should()
          .resideInAnyPackage(
              ArchitectureProject.anySliceLayerSubpackage(ArchitectureLayer.CORE, "security.."),
              ArchitectureProject.anySliceLayerSubpackage(ArchitectureLayer.UI, "security.."),
              ArchitectureProject.moduleLayerPackage(
                  ArchitectureSlice.SECURITY, ArchitectureLayer.API, "security.."),
              ArchitectureProject.appSubpackage("security.."))
          .as("ResourceRole classes should reside in designated security packages");

  @ArchTest
  public static final ArchRule configurationClassesAreAtLayerRoot =
      classes()
          .that()
          .haveSimpleNameEndingWith("Configuration")
          .and()
          .areAnnotatedWith("org.springframework.context.annotation.Configuration")
          .should()
          .resideInAnyPackage(
              ArchitectureProject.anySliceLayerPackage(ArchitectureLayer.API),
              ArchitectureProject.anySliceLayerPackage(ArchitectureLayer.CORE),
              ArchitectureProject.anySliceLayerPackage(ArchitectureLayer.UI),
              ArchitectureProject.appPackage(),
              ArchitectureProject.testSupportPackagePattern())
          .as("Configuration classes should reside at layer root package");
}
