package com.insurance.common.test_support.architecture.rules.ui;

import static com.insurance.common.test_support.architecture.lang.ArchitectureConditions.notExist;
import static com.insurance.common.test_support.architecture.rules.ui.parts.UiCompositionRuleParts.domainCoreOrUiImplementationClasses;
import static com.insurance.common.test_support.architecture.rules.ui.parts.UiCompositionRuleParts.domainUiClasses;
import static com.insurance.common.test_support.architecture.rules.ui.parts.UiCompositionRuleParts.domainUiXmlFiles;
import static com.insurance.common.test_support.architecture.rules.ui.parts.UiCompositionRuleParts.notDependOnForeignUiImplementationClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces UI module composition boundaries.
 *
 * <p>Domain UI modules may expose small UI contracts through {@code *-ui-api}, but they must not
 * depend on or instantiate foreign UI implementation modules directly. Rich composition across
 * multiple modules should happen in webapp or through explicit contribution contracts.
 */
public class UiCompositionBoundaryRules {

  /**
   * UI XML views and resources must not reference foreign core or UI implementation classes or
   * packages. Use domain APIs, {@code *-ui-api}, shared UI sections, or compose starters in webapp.
   */
  @ArchTest
  public static final ArchRule domainUiXmlsDoNotUseForeignClassesOrPackages =
      all(domainUiXmlFiles())
          .should(notExist())
          .as("domain UI XML files should not use foreign core or UI implementations")
          .because(
              "cross-module UI composition should use *-ui-api contracts, shared ui-sections, "
                  + "or webapp composition")
          .allowEmptyShould(true);

  /**
   * UI Java classes must not directly depend on foreign UI implementation packages. This catches
   * code-level coupling even when the build file dependency exists indirectly.
   */
  @ArchTest
  public static final ArchRule domainUiClassesDoNotUseForeignUiImplementations =
      classes()
          .that(domainUiClasses())
          .should(notDependOnForeignUiImplementationClasses())
          .as("domain UI classes should not use foreign UI implementations")
          .because(
              "UI modules compose foreign UI through explicit contracts instead of controllers")
          .allowEmptyShould(true);

  /**
   * UI API packages should expose UI contracts only. They must not leak domain core classes or UI
   * implementation classes, otherwise consumers would inherit implementation coupling.
   */
  @ArchTest
  public static final ArchRule uiApiPackagesOnlyExposeUiContracts =
      noClasses()
          .that()
          .resideInAnyPackage(ArchitectureProject.domainUiApiPackages())
          .should()
          .dependOnClassesThat(domainCoreOrUiImplementationClasses())
          .as("UI API packages should only expose UI contracts")
          .because("consumers of *-ui-api must not inherit core or UI implementation coupling")
          .allowEmptyShould(true);
}
