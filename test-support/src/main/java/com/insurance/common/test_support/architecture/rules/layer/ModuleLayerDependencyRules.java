package com.insurance.common.test_support.architecture.rules.layer;

import static com.insurance.common.test_support.architecture.rules.layer.parts.ModuleLayerRuleParts.domainUiClasses;
import static com.insurance.common.test_support.architecture.rules.layer.parts.ModuleLayerRuleParts.notUseForeignCoreImplementations;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.insurance.common.test_support.architecture.project.ArchitectureLayer;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the coarse Java package layering between API, core, and UI modules.
 *
 * <p>API packages are contracts only, core packages contain domain logic without Flow UI coupling,
 * and UI packages must not reach into foreign core implementations. Cross-domain collaboration
 * should happen through API contracts or explicit UI contracts, not through implementation
 * packages.
 */
public class ModuleLayerDependencyRules {

  /** Enforces general internal package layering constraints (API, Core, UI). */
  @ArchTest
  public static final ArchRule layeredArchitectureRespected =
      layeredArchitecture()
          .consideringOnlyDependenciesInAnyPackage(
              ArchitectureProject.domainLayerPackages()[0],
              java.util.Arrays.copyOfRange(
                  ArchitectureProject.domainLayerPackages(),
                  1,
                  ArchitectureProject.domainLayerPackages().length))
          .layer(ArchitectureLayer.API.name())
          .definedBy(ArchitectureProject.anyInsuranceApiPackage())
          .layer(ArchitectureLayer.CORE.name())
          .definedBy(ArchitectureProject.anyInsuranceCorePackage())
          .layer(ArchitectureLayer.UI.name())
          .definedBy(ArchitectureProject.anyInsuranceUiPackage())
          .whereLayer(ArchitectureLayer.API.name())
          .mayNotAccessAnyLayer()
          .whereLayer(ArchitectureLayer.CORE.name())
          .mayOnlyAccessLayers(ArchitectureLayer.API.name())
          .whereLayer(ArchitectureLayer.UI.name())
          .mayOnlyAccessLayers(ArchitectureLayer.API.name(), ArchitectureLayer.CORE.name())
          .as("insurance internal layers (API, Core, UI) should be respected")
          .because(
              "we want clear separation: Core depends only on APIs, and UI depends on Core and APIs.");

  /**
   * API modules must stay implementation-free so other modules can depend on them without pulling
   * in domain internals, UI controllers, or Vaadin/Jmix Flow UI classes.
   */
  @ArchTest
  public static final ArchRule apiModulesOnlyExposeApiContracts =
      noClasses()
          .that()
          .resideInAnyPackage(ArchitectureProject.domainApiPackages())
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              ArchitectureProject.anyInsuranceCorePackage(),
              ArchitectureProject.anyInsuranceUiPackage(),
              ArchitectureProject.anyFlowUiPackage());

  /**
   * Core modules must be isolated from each other. They may only communicate via API contracts, not
   * direct references to foreign core implementation classes.
   */
  @ArchTest
  public static final ArchRule coreModulesDoNotDependOnEachOther =
      slices()
          .matching("com.insurance.(*).core..")
          .should()
          .notDependOnEachOther()
          .as("core modules should not depend on each other's core implementations")
          .because("cross-module core collaboration must always go through API contracts");

  /** Core code must not depend on Flow UI APIs; UI behavior belongs in UI modules. */
  @ArchTest
  public static final ArchRule coreClassesDoNotUseFlowUiApis =
      noClasses()
          .that()
          .resideInAPackage(ArchitectureProject.anyInsuranceCorePackage())
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(ArchitectureProject.anyFlowUiPackage());

  /**
   * Core services must stay UI-agnostic so business operations can run from tests, listeners, jobs,
   * APIs, and UI flows without depending on Vaadin/Jmix screen classes.
   */
  @ArchTest
  public static final ArchRule coreServicesMustNotDependOnUiClasses =
      noClasses()
          .that()
          .resideInAPackage(ArchitectureProject.anyInsuranceCorePackage())
          .and()
          .areAnnotatedWith(org.springframework.stereotype.Service.class)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              ArchitectureProject.anyInsuranceUiPackage(),
              ArchitectureProject.anyFlowUiPackage(),
              "com.vaadin.flow..");

  /**
   * UI modules can use their own core model, but must not bind directly to foreign core
   * implementations. Foreign data should be exposed through API/read services or UI contracts.
   */
  @ArchTest
  public static final ArchRule uiModulesDoNotUseForeignCoreImplementations =
      classes()
          .that(domainUiClasses())
          .should(notUseForeignCoreImplementations())
          .as("UI modules should not use foreign core implementations")
          .because("foreign domain data should be exposed through APIs or UI contracts")
          .allowEmptyShould(true);
}
