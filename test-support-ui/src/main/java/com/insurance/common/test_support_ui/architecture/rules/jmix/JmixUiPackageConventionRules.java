package com.insurance.common.test_support_ui.architecture.rules.jmix;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewController;

/**
 * Package conventions for Jmix Flow UI artifacts.
 *
 * <p>These rules live in {@code test-support-ui} because they use Jmix Flow UI annotation types as
 * compile-time dependencies. The generic module package conventions stay in {@code test-support}
 * and cover non-UI Spring/Jmix artifacts.
 */
public class JmixUiPackageConventionRules {

  @ArchTest
  public static final ArchRule viewControllersResideInUiViewPackage =
      classes()
          .that()
          .areAnnotatedWith(ViewController.class)
          .should()
          .resideInAnyPackage(ArchitectureProject.allUiViewPackages())
          .as("ViewController classes should reside in ui.view packages");

  @ArchTest
  public static final ArchRule fragmentDescriptorsResideInUiViewPackage =
      classes()
          .that()
          .areAnnotatedWith(FragmentDescriptor.class)
          .should()
          .resideInAnyPackage(ArchitectureProject.allUiViewPackages())
          .as("FragmentDescriptor classes should reside in ui.view packages");
}
