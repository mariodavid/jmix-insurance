package com.insurance.common.test_support.architecture.rules.entity;

import static com.insurance.common.test_support.architecture.rules.entity.parts.PersistentEntityDependencyRuleParts.notDependOnForeignEntityPackages;
import static com.insurance.common.test_support.architecture.rules.entity.parts.PersistentEntityDependencyRuleParts.persistentDomainModels;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the persistent domain model boundary between modules.
 *
 * <p>Persistent entities and embeddables live below {@code com.insurance.<module>.core.entity}.
 * Once that package convention is enforced, this rule prevents cross-domain JPA associations by
 * checking type dependencies between those entity packages. Consumer-owned embedded references
 * remain allowed because they live in the consuming module's own entity package. Foreign API enums
 * remain allowed because they are not core entity types.
 */
public class PersistentEntityDependencyRules {

  /**
   * Persistent entities and embeddables must not depend on another module's core entity package.
   * This blocks fields such as {@code private Policy policy;} in Account while allowing local value
   * objects such as {@code AccountPolicyReference}.
   */
  @ArchTest
  public static final ArchRule persistentDomainModelsDoNotReferenceForeignEntityPackages =
      classes()
          .that(persistentDomainModels())
          .should(notDependOnForeignEntityPackages())
          .as("persistent domain models should not reference foreign entity packages")
          .because(
              "persistent domain models keep cross-module references as local embedded values; "
                  + "foreign API enums stay allowed because they are outside core.entity packages")
          .allowEmptyShould(true);
}
