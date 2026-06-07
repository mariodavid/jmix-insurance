package com.insurance.common.test_support.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CoreModuleFileRules {

  private CoreModuleFileRules() {}

  public static void assertCoreModuleDoesNotDeclareFlowUi(Path moduleRoot) {
    assertGradleFileDoesNotDeclareFlowUi(moduleRoot);
    assertMainResourcesDoNotContainFlowUiViews(moduleRoot);
  }

  private static void assertGradleFileDoesNotDeclareFlowUi(Path moduleRoot) {
    Path gradleFile = moduleRoot.resolve(moduleRoot.getFileName() + ".gradle");

    assertThat(ArchitectureFiles.read(gradleFile))
        .as(
            "%s must stay independent from Flow UI",
            ArchitectureProject.projectRoot().relativize(gradleFile))
        .doesNotContain("jmix-flowui");
  }

  private static void assertMainResourcesDoNotContainFlowUiViews(Path moduleRoot) {
    Path resources = moduleRoot.resolve(Path.of("src", "main", "resources"));
    if (!Files.exists(resources)) {
      return;
    }

    assertThat(ArchitectureFiles.walk(resources))
        .allSatisfy(path -> assertResourceDoesNotDeclareFlowUiView(resources, path));
  }

  private static void assertResourceDoesNotDeclareFlowUiView(Path resources, Path path) {
    String relativePath = resources.relativize(path).toString().replace('\\', '/');

    assertThat(relativePath)
        .as(
            "%s must not contain Flow UI view resources",
            ArchitectureProject.projectRoot().relativize(path))
        .doesNotContain("/view/");
    assertThat(ArchitectureFiles.read(path))
        .as(
            "%s must not use Flow UI XML schema",
            ArchitectureProject.projectRoot().relativize(path))
        .doesNotContain("jmix.io/schema/flowui");
  }
}
