package com.insurance.common.test_support.architecture.rules.security;

import static com.insurance.common.test_support.architecture.rules.security.parts.SecurityRoleLayerRuleParts.haveRoleCodeMatchingConvention;
import static com.insurance.common.test_support.architecture.rules.security.parts.SecurityRoleLayerRuleParts.onlyDeclareCoreSecurityPolicies;
import static com.insurance.common.test_support.architecture.rules.security.parts.SecurityRoleLayerRuleParts.onlyDeclareUiSecurityPolicies;
import static com.insurance.common.test_support.architecture.rules.security.parts.SecurityRoleLayerRuleParts.useWildcardsOnlyInAllowedRoles;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.insurance.common.test_support.architecture.project.ArchitectureLayer;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Guardrails to enforce security role layering, wildcard restrictions, and role code prefixes.
 *
 * <p>Enforces that:
 *
 * <ul>
 *   <li>Core security roles (under core.security) only declare entity and attribute policies.
 *   <li>UI security roles (under ui.security) only declare view and menu policies.
 *   <li>Wildcard policies (entityName = "*", viewIds = "*", etc.) are restricted to FullAccessRole.
 *   <li>Core/UI role codes are prefixed with "<module>-core-" or "<module>-ui-" respectively.
 * </ul>
 */
public class SecurityRoleLayerRules {

  @ArchTest
  public static final ArchRule coreRolesOnlyDeclareEntityPolicies =
      classes()
          .that()
          .resideInAPackage(ArchitectureLayer.CORE.anyInsuranceSubpackage("security.."))
          .should(onlyDeclareCoreSecurityPolicies())
          .andShould(haveRoleCodeMatchingConvention())
          .as("core security roles should only declare entity and attribute policies");

  @ArchTest
  public static final ArchRule uiRolesOnlyDeclareUiPolicies =
      classes()
          .that()
          .resideInAPackage(ArchitectureLayer.UI.anyInsuranceSubpackage("security.."))
          .should(onlyDeclareUiSecurityPolicies())
          .andShould(haveRoleCodeMatchingConvention())
          .as("UI security roles should only declare view and menu policies");

  @ArchTest
  public static final ArchRule wildcardPoliciesAreOnlyUsedByExplicitFullAccessRoles =
      classes()
          .that()
          .areAnnotatedWith("io.jmix.security.role.annotation.ResourceRole")
          .should(useWildcardsOnlyInAllowedRoles())
          .as("wildcard security policies should be restricted to explicit full-access roles");
}
