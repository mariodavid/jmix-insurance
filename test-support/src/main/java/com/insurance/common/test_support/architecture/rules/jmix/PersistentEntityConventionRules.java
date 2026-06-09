package com.insurance.common.test_support.architecture.rules.jmix;

import static com.insurance.common.test_support.architecture.rules.jmix.parts.PersistentEntityConventionRuleParts.haveModulePrefixedJmixNames;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.PersistentEntityConventionRuleParts.jmixEntityConstructorCalls;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.PersistentEntityConventionRuleParts.lombokAnnotations;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.PersistentEntityConventionRuleParts.persistentEmbeddables;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.PersistentEntityConventionRuleParts.persistentEntities;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.PersistentEntityConventionRuleParts.resideInOwningCoreEntityPackage;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.PersistentEntityConventionRuleParts.resideInOwningCoreEntityPackageAsEmbeddables;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces Jmix-specific persistent entity conventions.
 *
 * <p>These rules make persistent models deterministic for both Jmix and the architecture tests:
 * entities are created through Jmix factories instead of constructors, remain Lombok-free, declare
 * stable module-prefixed JPA names, and live in the package shape used to derive module ownership.
 */
public class PersistentEntityConventionRules {

  /**
   * Jmix entities should be instantiated through Metadata, DataManager, or DataContext so Jmix can
   * apply metadata and enhancement behavior consistently.
   */
  @ArchTest
  public static final ArchRule jmixEntitiesAreCreatedThroughJmixFactories =
      noClasses().should().callConstructorWhere(jmixEntityConstructorCalls());

  /**
   * Persistent JPA entities stay Lombok-free because generated equals/hashCode/getters can conflict
   * with Jmix enhancement, lazy loading, and entity identity semantics.
   */
  @ArchTest
  public static final ArchRule persistentEntitiesDoNotUseLombok =
      noClasses()
          .that()
          .areAnnotatedWith(jakarta.persistence.Entity.class)
          .should()
          .beAnnotatedWith(lombokAnnotations());

  /**
   * Entity names must start with the owning module's Jmix project id, for example {@code
   * policy_Policy}. This makes JPQL/XML string references attributable to a module.
   */
  @ArchTest
  public static final ArchRule persistentEntitiesUseModulePrefixedJmixNames =
      all(persistentEntities())
          .should(haveModulePrefixedJmixNames())
          .as("persistent Jmix entities should use module-prefixed Jmix names")
          .because("entity names are the module ownership marker used by JPQL and XML guardrails")
          .allowEmptyShould(true);

  /**
   * Persistent entities must live below {@code com.insurance.<module>.core.entity}. This package is
   * the ownership marker used by the cross-domain entity dependency rules.
   */
  @ArchTest
  public static final ArchRule persistentEntitiesLiveInOwningCoreEntityPackage =
      all(persistentEntities())
          .should(resideInOwningCoreEntityPackage())
          .as("persistent entities should live in the owning core entity package")
          .because(
              "architecture rules derive domain ownership from "
                  + "com.insurance.<module>.core.entity")
          .allowEmptyShould(true);

  /**
   * Persistent embeddables follow the same package convention as entities. This keeps embedded
   * reference value objects owned by the consuming module.
   */
  @ArchTest
  public static final ArchRule persistentEmbeddablesLiveInOwningCoreEntityPackage =
      all(persistentEmbeddables())
          .should(resideInOwningCoreEntityPackageAsEmbeddables())
          .as("persistent embeddables should live in the owning core entity package")
          .because("embedded references stay owned by the consuming module")
          .allowEmptyShould(true);
}
