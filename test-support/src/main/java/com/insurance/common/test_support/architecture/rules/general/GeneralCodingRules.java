package com.insurance.common.test_support.architecture.rules.general;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import org.slf4j.Logger;

/**
 * Standard coding rules and conventions for Java code quality, including logger configuration,
 * exception handling, and dependency injection patterns.
 */
public class GeneralCodingRules {

  @ArchTest
  public static final ArchRule loggersShouldBePrivateStaticFinal =
      fields()
          .that()
          .haveRawType(Logger.class)
          .should()
          .bePrivate()
          .andShould()
          .beStatic()
          .andShould()
          .beFinal()
          .as("loggers should be private static final")
          .because("we want consistent logging declarations across all services and components");

  @ArchTest
  public static final ArchRule noFieldInjection =
      noFields()
          .that()
          .areNotAnnotatedWith("io.jmix.flowui.view.ViewComponent")
          .should()
          .beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
          .orShould()
          .beAnnotatedWith("org.springframework.beans.factory.annotation.Value")
          .orShould()
          .beAnnotatedWith("jakarta.inject.Inject")
          .orShould()
          .beAnnotatedWith("javax.inject.Inject")
          .orShould()
          .beAnnotatedWith("jakarta.annotation.Resource")
          .orShould()
          .beAnnotatedWith("javax.annotation.Resource")
          .as("no fields should use field injection")
          .because(
              "constructor injection is preferred and Jmix @ViewComponent is the only allowed field injection annotation.");

  @ArchTest
  public static final ArchRule codingStandardConventions =
      CompositeArchRule.of(NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS)
          .and(NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS)
          .and(noFieldInjection)
          .as("Coding and dependency standards");
}
