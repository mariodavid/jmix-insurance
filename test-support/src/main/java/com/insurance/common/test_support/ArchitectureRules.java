package com.insurance.common.test_support;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Shared ArchUnit rules for enforcing Jmix clean modular architecture, Jmix lifecycle, and
 * dependency boundaries.
 */
public class ArchitectureRules {

  public static final List<String> MODULES =
      List.of("account", "partner", "policy", "product", "quote");

  @ArchTest
  public static final ArchRule apiModulesOnlyExposeApiContracts =
      noClasses()
          .that()
          .resideInAPackage(anyInsuranceApiPackage())
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              anyInsuranceCorePackage(), anyInsuranceUiPackage(), anyFlowUiPackage());

  @ArchTest
  public static void coreModulesOnlyUseTheirOwnImplementationAndForeignApis(JavaClasses classes) {
    MODULES.forEach(
        module ->
            noClasses()
                .that()
                .resideInAPackage(corePackageOf(module))
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(foreignCoreOrUiPackagesOf(module))
                .allowEmptyShould(true)
                .check(classes));
  }

  @ArchTest
  public static final ArchRule coreClassesDoNotUseFlowUiApis =
      noClasses()
          .that()
          .resideInAPackage(anyInsuranceCorePackage())
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(anyFlowUiPackage());

  @ArchTest
  public static final ArchRule coreServicesMustNotDependOnUiClasses =
      noClasses()
          .that()
          .resideInAPackage(anyInsuranceCorePackage())
          .and()
          .areAnnotatedWith(org.springframework.stereotype.Service.class)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(anyInsuranceUiPackage(), anyFlowUiPackage(), "com.vaadin.flow..");

  @ArchTest
  public static final ArchRule jmixEntitiesAreNotInstantiatedViaConstructor =
      noClasses()
          .should()
          .callConstructorWhere(
              new DescribedPredicate<JavaConstructorCall>("target is a foreign Jmix entity class") {
                @Override
                public boolean test(JavaConstructorCall target) {
                  boolean isJmixEntity =
                      target
                          .getTargetOwner()
                          .isAnnotatedWith(io.jmix.core.metamodel.annotation.JmixEntity.class);
                  boolean isInheritanceCall =
                      target.getOriginOwner().isAssignableTo(target.getTargetOwner().getName());
                  return isJmixEntity && !isInheritanceCall;
                }
              });

  @ArchTest
  public static final ArchRule persistentJmixEntitiesMustNotUseLombok =
      noClasses()
          .that()
          .areAnnotatedWith(jakarta.persistence.Entity.class)
          .should()
          .beAnnotatedWith(
              new DescribedPredicate<JavaAnnotation<?>>("Lombok annotation") {
                @Override
                public boolean test(JavaAnnotation<?> annotation) {
                  return annotation.getRawType().getName().startsWith("lombok.");
                }
              });

  @ArchTest
  public static void uiModulesDoNotUseForeignCoreImplementations(JavaClasses classes) {
    MODULES.forEach(
        module ->
            noClasses()
                .that()
                .resideInAPackage(uiPackageOf(module))
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(foreignCorePackagesOf(module))
                .allowEmptyShould(true)
                .check(classes));
  }

  @ArchTest
  public static final ArchRule productClassesDoNotDependOnPartnerClasses =
      noClasses()
          .that()
          .resideInAPackage(productPackage())
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(partnerPackage());

  // --- Package Helpers ---

  public static String anyInsuranceApiPackage() {
    return "com.insurance..api..";
  }

  public static String anyInsuranceCorePackage() {
    return "com.insurance..core..";
  }

  public static String anyInsuranceUiPackage() {
    return "com.insurance..ui..";
  }

  public static String anyFlowUiPackage() {
    return "io.jmix.flowui..";
  }

  public static String productPackage() {
    return "com.insurance.product..";
  }

  public static String partnerPackage() {
    return "com.insurance.partner..";
  }

  public static String corePackageOf(String module) {
    return moduleLayerPackage(module, "core");
  }

  public static String uiPackageOf(String module) {
    return moduleLayerPackage(module, "ui");
  }

  public static String[] foreignCoreOrUiPackagesOf(String module) {
    return foreignLayerPackagesOf(module, "core", "ui");
  }

  public static String[] foreignCorePackagesOf(String module) {
    return foreignLayerPackagesOf(module, "core");
  }

  public static String[] foreignLayerPackagesOf(String module, String... layers) {
    return MODULES.stream()
        .filter(otherModule -> !otherModule.equals(module))
        .flatMap(
            otherModule -> Stream.of(layers).map(layer -> moduleLayerPackage(otherModule, layer)))
        .toArray(String[]::new);
  }

  public static String moduleLayerPackage(String module, String layer) {
    return "com.insurance." + module + "." + layer + "..";
  }

  // --- File System Checks ---

  public static Path projectRoot() {
    Path userDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    Path fileName = userDir.getFileName();
    if (fileName != null
        && List.of(
                "webapp",
                "quote",
                "partner",
                "policy",
                "account",
                "product",
                "security",
                "test-support")
            .contains(fileName.toString())) {
      Path parent = userDir.getParent();
      if (parent == null) {
        throw new IllegalStateException("Cannot determine project root from " + userDir);
      }
      return parent;
    }
    return userDir;
  }

  public static void assertCoreModuleDoesNotDeclareFlowUi(Path moduleRoot) {
    assertGradleFileDoesNotDeclareFlowUi(moduleRoot);
    assertMainResourcesDoNotContainFlowUiViews(moduleRoot);
  }

  private static void assertGradleFileDoesNotDeclareFlowUi(Path moduleRoot) {
    Path gradleFile = moduleRoot.resolve(moduleRoot.getFileName() + ".gradle");

    assertThat(read(gradleFile))
        .as("%s must stay independent from Flow UI", projectRoot().relativize(gradleFile))
        .doesNotContain("jmix-flowui");
  }

  private static void assertMainResourcesDoNotContainFlowUiViews(Path moduleRoot) {
    Path resources = moduleRoot.resolve(Path.of("src", "main", "resources"));
    if (!Files.exists(resources)) {
      return;
    }

    assertThat(walk(resources))
        .allSatisfy(path -> assertResourceDoesNotDeclareFlowUiView(resources, path));
  }

  private static void assertResourceDoesNotDeclareFlowUiView(Path resources, Path path) {
    String relativePath = resources.relativize(path).toString().replace('\\', '/');

    assertThat(relativePath)
        .as("%s must not contain Flow UI view resources", projectRoot().relativize(path))
        .doesNotContain("/view/");
    assertThat(read(path))
        .as("%s must not use Flow UI XML schema", projectRoot().relativize(path))
        .doesNotContain("jmix.io/schema/flowui");
  }

  private static List<Path> walk(Path root) {
    try (Stream<Path> paths = Files.walk(root)) {
      return paths.filter(Files::isRegularFile).toList();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String read(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
