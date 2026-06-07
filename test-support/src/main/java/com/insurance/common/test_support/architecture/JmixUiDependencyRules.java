package com.insurance.common.test_support.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.ArchTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JmixUiDependencyRules {

  private static final List<String> UI_ARTIFACT_LAYERS = List.of("ui", "ui-starter");

  @ArchTest
  public static void domainUiBuildFilesDoNotDependOnForeignUiImplementations(JavaClasses classes) {
    List<ForeignUiDependency> violations = new ArrayList<>();

    for (String sourceModule : ArchitectureProject.domainModules()) {
      for (String layer : UI_ARTIFACT_LAYERS) {
        Path buildFile = buildFile(sourceModule, layer);
        if (!Files.exists(buildFile)) {
          continue;
        }

        String content = removeGradleComments(ArchitectureFiles.read(buildFile));
        for (String targetModule : ArchitectureProject.domainModules()) {
          if (!sourceModule.equals(targetModule)) {
            findForbiddenDependencies(buildFile, content, sourceModule, targetModule, violations);
          }
        }
      }
    }

    assertThat(violations)
        .as(
            "Domain UI modules must not depend on foreign UI implementation artifacts. "
                + "Use foreign *-api, foreign *-ui-api, shared ui-sections, or compose modules in webapp.")
        .isEmpty();
  }

  @ArchTest
  public static void domainUiClassesDoNotUseForeignUiImplementations(JavaClasses classes) {
    ArchitectureProject.domainModules()
        .forEach(
            module ->
                noClasses()
                    .that()
                    .resideInAPackage(ArchitectureProject.uiPackageOf(module))
                    .should()
                    .dependOnClassesThat(foreignUiImplementationClasses(module))
                    .allowEmptyShould(true)
                    .check(classes));
  }

  @ArchTest
  public static void uiApiPackagesOnlyExposeUiContracts(JavaClasses classes) {
    noClasses()
        .that()
        .resideInAnyPackage(ArchitectureProject.domainUiApiPackages())
        .should()
        .dependOnClassesThat(domainCoreOrUiImplementationClasses())
        .allowEmptyShould(true)
        .check(classes);
  }

  private static void findForbiddenDependencies(
      Path buildFile,
      String content,
      String sourceModule,
      String targetModule,
      List<ForeignUiDependency> violations) {
    for (String layer : UI_ARTIFACT_LAYERS) {
      String artifact = targetModule + "-" + layer;

      if (projectDependencyPattern(artifact).matcher(content).find()
          || moduleCoordinatePattern(targetModule, artifact).matcher(content).find()) {
        violations.add(new ForeignUiDependency(buildFile, sourceModule, artifact));
      }
    }
  }

  private static Path buildFile(String module, String layer) {
    Path root = ArchitectureProject.layerRoot(module, layer);
    return root.resolve(root.getFileName() + ".gradle");
  }

  private static Pattern projectDependencyPattern(String artifact) {
    return Pattern.compile("project\\s*\\(\\s*['\"][^'\"]*:" + Pattern.quote(artifact) + "['\"]");
  }

  private static Pattern moduleCoordinatePattern(String targetModule, String artifact) {
    return Pattern.compile(
        "['\"]com\\.insurance(?:\\."
            + Pattern.quote(targetModule)
            + ")?"
            + ":"
            + Pattern.quote(artifact)
            + "(?:[:'\"])");
  }

  private static String removeGradleComments(String content) {
    return content.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", "");
  }

  private static DescribedPredicate<JavaClass> foreignUiImplementationClasses(String sourceModule) {
    return new DescribedPredicate<>(
        "foreign domain UI implementation classes outside *.ui.api for " + sourceModule) {
      @Override
      public boolean test(JavaClass javaClass) {
        return ArchitectureProject.domainModules().stream()
            .filter(targetModule -> !sourceModule.equals(targetModule))
            .anyMatch(
                targetModule ->
                    ArchitectureProject.isDomainUiImplementationPackage(
                        javaClass.getPackageName(), targetModule));
      }
    };
  }

  private static DescribedPredicate<JavaClass> domainCoreOrUiImplementationClasses() {
    return new DescribedPredicate<>("domain core or domain UI implementation classes") {
      @Override
      public boolean test(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return ArchitectureProject.isDomainCorePackage(packageName)
            || ArchitectureProject.isDomainUiImplementationPackage(packageName);
      }
    };
  }

  private record ForeignUiDependency(Path buildFile, String sourceModule, String artifact) {

    @Override
    public String toString() {
      return ArchitectureProject.projectRoot()
              .relativize(buildFile.toAbsolutePath().normalize())
              .toString()
          + " in "
          + sourceModule
          + " declares forbidden dependency on "
          + artifact
          + ". Use the target module's domain API, a *-ui-api contract, or compose UI starters in webapp.";
    }
  }
}
