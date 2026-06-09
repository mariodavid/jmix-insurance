package com.insurance.common.test_support.architecture.rules.jmix;

import static com.insurance.common.test_support.architecture.rules.jmix.parts.JmixDtoEntityRuleParts.apiDtoEntities;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.JmixDtoEntityRuleParts.haveApiDtoEntityName;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.JmixDtoEntityRuleParts.haveExactlyOneJmixId;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.JmixDtoEntityRuleParts.notUseJpaPersistenceAnnotations;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Guardrails to enforce standard conventions on Jmix API DTO entities.
 *
 * <p>DTO entities exposed via API modules are not persistent and must:
 *
 * <ul>
 *   <li>Reside in the com.insurance.<module>.api.dto package.
 *   <li>Have a stable Jmix entity name in format "<module>_api_<ClassName>".
 *   <li>Have exactly one @JmixId field or method.
 *   <li>If @JmixGeneratedValue is used, it must be placed on the @JmixId member.
 *   <li>Not use persistent JPA annotations (@Entity, @Table, @Embeddable, @MappedSuperclass).
 * </ul>
 */
public class JmixDtoEntityConventionRules {

  @ArchTest
  public static final ArchRule apiDtoEntitiesHaveStableJmixMetadata =
      classes()
          .that(apiDtoEntities())
          .should(haveApiDtoEntityName())
          .andShould(haveExactlyOneJmixId())
          .andShould(notUseJpaPersistenceAnnotations())
          .as("API DTO entities should have stable Jmix metadata");
}
