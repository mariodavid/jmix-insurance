package com.insurance.common.test_support.architecture.rules.slice;

import static com.insurance.common.test_support.architecture.rules.slice.parts.DomainBuildRuleParts.domainBuildFiles;
import static com.insurance.common.test_support.architecture.rules.slice.parts.DomainBuildRuleParts.useSharedDomainBuildConvention;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces shared Gradle build conventions for domain build roots.
 *
 * <p>This is not a cross-domain entity dependency rule. Its boundary is build-system consistency:
 * each domain build root declares its Jmix project id and delegates common quality/Jmix setup to
 * the shared convention script instead of copying configuration into individual builds.
 */
public class DomainBuildConventionRules {

  /**
   * Domain build roots must apply the shared convention and avoid local copies of static analysis,
   * Liquibase duplicate-file handling, and JaCoCo configuration.
   */
  @ArchTest
  public static final ArchRule domainBuildsUseSharedConventionScript =
      all(domainBuildFiles())
          .should(useSharedDomainBuildConvention())
          .as("domain build roots should use the shared Jmix convention script")
          .because(
              "module-local build logic makes agent changes less deterministic and harder to guard")
          .allowEmptyShould(true);
}
