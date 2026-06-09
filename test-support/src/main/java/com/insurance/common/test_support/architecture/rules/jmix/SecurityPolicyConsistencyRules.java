package com.insurance.common.test_support.architecture.rules.jmix;

import static com.insurance.common.test_support.architecture.lang.ArchitectureConditions.notExist;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.SecurityPolicyRuleParts.unknownMenuPolicyReferences;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.SecurityPolicyRuleParts.unknownViewPolicyReferences;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.SecurityPolicyRuleParts.userFacingViewsWithoutConcreteViewPolicy;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces consistency between Jmix security role annotations and actual UI/menu artifacts.
 *
 * <p>Roles are part of the app architecture: an agent should not grant access to a non-existing
 * view/menu id, and every user-facing view should have an explicit view policy. These checks keep
 * role composition deterministic as modules add views.
 */
public class SecurityPolicyConsistencyRules {

  /** View policies must reference concrete {@code @ViewController} ids that exist in the app. */
  @ArchTest
  public static final ArchRule viewPoliciesReferenceExistingViews =
      all(unknownViewPolicyReferences())
          .should(notExist())
          .as("view policies should reference existing views")
          .because("security roles should not grant access to non-existing Jmix views")
          .allowEmptyShould(true);

  /** Menu policies must reference menu item ids or view-backed menu items declared in XML. */
  @ArchTest
  public static final ArchRule menuPoliciesReferenceExistingMenuItems =
      all(unknownMenuPolicyReferences())
          .should(notExist())
          .as("menu policies should reference existing menu items")
          .because("security roles should not grant access to non-existing menu entries")
          .allowEmptyShould(true);

  /**
   * Every user-facing view controller must be covered by a concrete view policy, avoiding
   * accidental public or unreachable views.
   */
  @ArchTest
  public static final ArchRule userFacingViewsHaveConcreteViewPolicies =
      all(userFacingViewsWithoutConcreteViewPolicy())
          .should(notExist())
          .as("user-facing views should have concrete view policies")
          .because("new views should not become accidentally public or unreachable")
          .allowEmptyShould(true);
}
