package com.insurance.common.test_support.architecture.rules.jmix;

import static com.insurance.common.test_support.architecture.lang.ArchitectureConditions.notExist;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.PersistentEntityNameRuleParts.foreignPersistentEntityNameReferences;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces Jmix persistent entity-name boundaries in Java and XML resources.
 *
 * <p>Some Jmix coupling is string-based, for example JPQL snippets, XML loaders, and
 * metadata/entity-name lookups. Those references do not always create Java type dependencies, so
 * the package dependency rules cannot see them. This rule scans production Java and XML files for
 * foreign entity names such as {@code policy_Policy} and forces modules to use the owning module's
 * API instead.
 */
public class PersistentEntityNameBoundaryRules {

  /**
   * Domain modules must not mention persistent entity names owned by another module. This protects
   * against cross-domain JPQL/XML joins and direct metadata lookups that would bypass API
   * contracts.
   */
  @ArchTest
  public static final ArchRule domainJavaAndXmlDoNotUseForeignPersistentEntityNames =
      all(foreignPersistentEntityNameReferences())
          .should(notExist())
          .as("domain Java and XML files should not use foreign persistent entity names")
          .because(
              "Jmix entity-name strings in JPQL, XML loaders, and metadata lookups bypass Java "
                  + "package dependency checks")
          .allowEmptyShould(true);
}
