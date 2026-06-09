package com.insurance.common.test_support.architecture.rules.jmix;

import static com.insurance.common.test_support.architecture.rules.jmix.parts.JmixInternalApiRuleParts.applicationCodeOutsideAllowedBootstrapping;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.JmixInternalApiRuleParts.notDependOnPackagesAnnotatedWithJmixInternal;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Guardrails to prevent production application code from depending on Jmix internal APIs.
 *
 * <p>Jmix internal APIs are marked with {@code io.jmix.core.annotation.Internal} (at type or
 * package level) or reside in {@code io.jmix..impl..} packages. These are internal implementation
 * details subject to change. Allowed bootstrapping code (Application/Configuration classes) are
 * exempt.
 */
public class JmixInternalApiRules {

  @ArchTest
  public static final ArchRule applicationCodeDoesNotUseJmixInternalApis =
      noClasses()
          .that(applicationCodeOutsideAllowedBootstrapping())
          .should()
          .dependOnClassesThat()
          .areAnnotatedWith("io.jmix.core.annotation.Internal")
          .as("application code should not depend on Jmix @Internal types");

  @ArchTest
  public static final ArchRule applicationCodeDoesNotUseJmixInternalPackages =
      classes()
          .that(applicationCodeOutsideAllowedBootstrapping())
          .should(notDependOnPackagesAnnotatedWithJmixInternal())
          .as("application code should not depend on Jmix @Internal packages");

  @ArchTest
  public static final ArchRule applicationCodeOnlyUsesAllowedJmixImplPackages =
      noClasses()
          .that(applicationCodeOutsideAllowedBootstrapping())
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("io.jmix..impl..")
          .as("application code should not depend on Jmix impl packages unless explicitly allowed");
}
