package com.insurance.common.test_support_ui.architecture.rules.jmix;

import static com.insurance.common.test_support_ui.architecture.ui.JmixViewDescriptorRuleParts.jmixViewOrFragmentControllers;
import static com.insurance.common.test_support_ui.architecture.ui.JmixViewDescriptorRuleParts.referenceExistingDescriptorFiles;
import static com.insurance.common.test_support_ui.architecture.ui.JmixViewDescriptorRuleParts.referenceExistingDescriptorTargets;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Guardrails for Jmix Flow UI controllers that are backed by XML descriptors.
 *
 * <p>The rules intentionally live in {@code test-support-ui}: they use compile-time references to
 * Jmix Flow UI annotations, while the generic {@code test-support} module stays independent from UI
 * framework dependencies.
 */
public class JmixViewDescriptorIntegrityRules {

  @ArchTest
  public static final ArchRule viewDescriptorsExist =
      classes()
          .that(jmixViewOrFragmentControllers())
          .should(referenceExistingDescriptorFiles())
          .as("Jmix view and fragment descriptors should exist");

  @ArchTest
  public static final ArchRule viewControllerAnnotationsReferenceDescriptorIds =
      classes()
          .that(jmixViewOrFragmentControllers())
          .should(referenceExistingDescriptorTargets())
          .as("Jmix view controllers should reference existing descriptor ids");
}
