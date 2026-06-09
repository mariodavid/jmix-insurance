package com.insurance.common.test_support.architecture.rules.jmix.parts;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public final class JmixInternalApiRuleParts {

  private JmixInternalApiRuleParts() {}

  public static DescribedPredicate<JavaClass> applicationCodeOutsideAllowedBootstrapping() {
    return DescribedPredicate.describe(
        "application code outside allowed bootstrapping",
        input -> {
          // Exclude bootstrapping/configuration and test-support code
          if (input.getSimpleName().endsWith("Configuration")
              || input.getSimpleName().endsWith("Application")
              || input.getPackageName().startsWith("com.insurance.common.test_support")
              || input.getPackageName().contains(".test_support")) {
            return false;
          }
          // Exclude Jmix entities and DTOs (which are bytecode enhanced by Jmix)
          if (input.isAnnotatedWith("io.jmix.core.metamodel.annotation.JmixEntity")
              || input.isAnnotatedWith("jakarta.persistence.Entity")
              || input.isAnnotatedWith("jakarta.persistence.Embeddable")
              || input.getSimpleName().contains("$")
              || input.getName().contains("JmixEntityEntry")
              || input.getName().contains("JmixSettersEnhanced")) {
            return false;
          }
          return true;
        });
  }

  public static ArchCondition<JavaClass> notDependOnPackagesAnnotatedWithJmixInternal() {
    return ArchitectureConditions.checkCondition(
        "not depend on packages annotated with @Internal",
        (clazz, events) -> {
          for (Dependency dependency : clazz.getDirectDependenciesFromSelf()) {
            JavaClass targetClass = dependency.getTargetClass();
            JavaPackage targetPackage = targetClass.getPackage();
            if (targetPackage != null
                && targetPackage.isAnnotatedWith("io.jmix.core.annotation.Internal")) {
              String message =
                  String.format(
                      "Class %s depends on %s in @Internal package %s",
                      clazz.getName(), targetClass.getName(), targetPackage.getName());
              events.add(SimpleConditionEvent.violated(clazz, message));
            }
          }
        });
  }
}
