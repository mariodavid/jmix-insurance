package com.insurance.app.arch;

import com.insurance.common.test_support.architecture.rules.entity.PersistentEntityDependencyRules;
import com.insurance.common.test_support.architecture.rules.insurance.BusinessModuleDependencyRules;
import com.insurance.common.test_support.architecture.rules.jmix.JmixDtoEntityConventionRules;
import com.insurance.common.test_support.architecture.rules.jmix.LiquibaseSchemaDriftRules;
import com.insurance.common.test_support.architecture.rules.jmix.PersistentEntityConventionRules;
import com.insurance.common.test_support.architecture.rules.jmix.PersistentEntityNameBoundaryRules;
import com.insurance.common.test_support.architecture.rules.jmix.SecurityPolicyConsistencyRules;
import com.insurance.common.test_support.architecture.rules.layer.CoreModuleIsolationRules;
import com.insurance.common.test_support.architecture.rules.layer.ModuleLayerDependencyRules;
import com.insurance.common.test_support.architecture.rules.security.SecurityRoleLayerRules;
import com.insurance.common.test_support.architecture.rules.slice.DomainBuildConventionRules;
import com.insurance.common.test_support.architecture.rules.ui.UiCompositionBoundaryRules;
import com.insurance.common.test_support_ui.architecture.rules.jmix.JmixUiPackageConventionRules;
import com.insurance.common.test_support_ui.architecture.rules.jmix.JmixViewDescriptorIntegrityRules;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import org.junit.jupiter.api.DisplayName;

/**
 * Global architecture test suite for the Insurance modular monolith.
 *
 * <p>This class is the single entry point for deterministic guardrails that coding agents should
 * not silently break: Java package boundaries, Jmix entity ownership, string-based Jmix entity-name
 * references, Liquibase/schema drift, UI composition boundaries, and security role consistency.
 * Most checks are ArchUnit rules; file-system checks cover Gradle/XML/resource boundaries that do
 * not always appear as Java type dependencies.
 */
@DisplayName("Global architecture rules")
@AnalyzeClasses(
    packages = "com.insurance",
    importOptions = {com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {

  @ArchTest
  static final ArchTests persistentEntityConventionRules =
      ArchTests.in(PersistentEntityConventionRules.class);

  @ArchTest
  static final ArchTests persistentEntityNameBoundaryRules =
      ArchTests.in(PersistentEntityNameBoundaryRules.class);

  @ArchTest
  static final ArchTests liquibaseSchemaDriftRules = ArchTests.in(LiquibaseSchemaDriftRules.class);

  @ArchTest
  static final ArchTests securityPolicyConsistencyRules =
      ArchTests.in(SecurityPolicyConsistencyRules.class);

  @ArchTest
  static final ArchTests jmixViewDescriptorIntegrityRules =
      ArchTests.in(JmixViewDescriptorIntegrityRules.class);

  @ArchTest
  static final ArchTests securityRoleLayerRules = ArchTests.in(SecurityRoleLayerRules.class);

  @ArchTest
  static final ArchTests jmixDtoEntityConventionRules =
      ArchTests.in(JmixDtoEntityConventionRules.class);

  @ArchTest
  static final ArchTests moduleLayerDependencyRules =
      ArchTests.in(ModuleLayerDependencyRules.class);

  @ArchTest
  static final ArchTests businessModuleDependencyRules =
      ArchTests.in(BusinessModuleDependencyRules.class);

  @ArchTest
  static final ArchTests coreModuleIsolationRules = ArchTests.in(CoreModuleIsolationRules.class);

  @ArchTest
  static final ArchTests domainBuildConventionRules =
      ArchTests.in(DomainBuildConventionRules.class);

  @ArchTest
  static final ArchTests persistentEntityDependencyRules =
      ArchTests.in(PersistentEntityDependencyRules.class);

  @ArchTest
  static final ArchTests uiCompositionBoundaryRules =
      ArchTests.in(UiCompositionBoundaryRules.class);

  @ArchTest
  static final ArchTests generalCodingRules =
      ArchTests.in(
          com.insurance.common.test_support.architecture.rules.general.GeneralCodingRules.class);

  @ArchTest
  static final ArchTests uiSectionContributionRules =
      ArchTests.in(
          com.insurance.common.test_support.architecture.rules.ui.UiSectionContributionRules.class);

  @ArchTest
  static final ArchTests embeddedReferenceConventionRules =
      ArchTests.in(
          com.insurance.common.test_support.architecture.rules.entity
              .EmbeddedReferenceConventionRules.class);

  @ArchTest
  static final ArchTests jmixInternalApiRules =
      ArchTests.in(
          com.insurance.common.test_support.architecture.rules.jmix.JmixInternalApiRules.class);

  @ArchTest
  static final ArchTests jmixEventListenerSafetyRules =
      ArchTests.in(
          com.insurance.common.test_support.architecture.rules.jmix.JmixEventListenerSafetyRules
              .class);

  @ArchTest
  static final ArchTests testSupportBoundaryRules =
      ArchTests.in(
          com.insurance.common.test_support.architecture.rules.layer.TestSupportBoundaryRules
              .class);

  @ArchTest
  static final ArchTests modulePackageConventionRules =
      ArchTests.in(
          com.insurance.common.test_support.architecture.rules.slice.ModulePackageConventionRules
              .class);

  @ArchTest
  static final ArchTests jmixUiPackageConventionRules =
      ArchTests.in(JmixUiPackageConventionRules.class);
}
