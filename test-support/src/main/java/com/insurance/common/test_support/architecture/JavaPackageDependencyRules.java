package com.insurance.common.test_support.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

public class JavaPackageDependencyRules {

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

  @ArchTest
  public static void coreModulesOnlyUseTheirOwnImplementationAndForeignApis(JavaClasses classes) {
    ArchitectureProject.domainModules()
        .forEach(
            module ->
                noClasses()
                    .that()
                    .resideInAPackage(ArchitectureProject.corePackageOf(module))
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(ArchitectureProject.foreignCoreOrUiPackagesOf(module))
                    .allowEmptyShould(true)
                    .check(classes));
  }

  @ArchTest
  public static final ArchRule coreClassesDoNotUseFlowUiApis =
      noClasses()
          .that()
          .resideInAPackage(ArchitectureProject.anyInsuranceCorePackage())
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(ArchitectureProject.anyFlowUiPackage());

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

  @ArchTest
  public static void uiModulesDoNotUseForeignCoreImplementations(JavaClasses classes) {
    ArchitectureProject.domainModules()
        .forEach(
            module ->
                noClasses()
                    .that()
                    .resideInAPackage(ArchitectureProject.uiPackageOf(module))
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(ArchitectureProject.foreignCorePackagesOf(module))
                    .allowEmptyShould(true)
                    .check(classes));
  }

  @ArchTest
  public static final ArchRule productClassesDoNotDependOnPartnerClasses =
      noClasses()
          .that()
          .resideInAPackage(ArchitectureProject.productPackage())
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(ArchitectureProject.partnerPackage());
}
