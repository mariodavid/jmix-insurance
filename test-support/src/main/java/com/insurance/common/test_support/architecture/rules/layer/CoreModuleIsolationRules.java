package com.insurance.common.test_support.architecture.rules.layer;

import static com.insurance.common.test_support.architecture.rules.layer.parts.CoreModuleRuleParts.beFreeOfFlowUi;
import static com.insurance.common.test_support.architecture.rules.layer.parts.CoreModuleRuleParts.coreModuleRoots;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces file-level separation between core modules and Flow UI.
 *
 * <p>Core modules may contain domain entities, services, listeners, security policies, Liquibase,
 * and messages, but not view descriptors or Flow UI dependencies. This complements Java dependency
 * checks by looking at Gradle files and resources that are not always represented as Java types.
 */
public class CoreModuleIsolationRules {

  /**
   * Checks that a core module does not declare Flow UI dependencies and does not ship Flow UI view
   * resources under main resources.
   */
  @ArchTest
  public static final ArchRule coreModulesDoNotDeclareFlowUiDependenciesOrViews =
      all(coreModuleRoots())
          .should(beFreeOfFlowUi())
          .as("core modules should not declare Flow UI dependencies or view resources")
          .because("core modules own domain behavior and must stay independent from Flow UI");
}
