package com.insurance.common.test_support.architecture.rules.layer.parts;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.insurance.common.test_support.architecture.project.ArchitectureFiles;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CoreModuleRuleParts {

  private CoreModuleRuleParts() {}

  public static ClassesTransformer<CoreModuleRoot> coreModuleRoots() {
    return ArchitectureConditions.transformClasses(
        "core module roots",
        classes ->
            ArchitectureSlice.entityBoundarySlices().stream()
                .map(
                    slice ->
                        new CoreModuleRoot(slice.id(), ArchitectureProject.coreLayerRoot(slice)))
                .toList());
  }

  public static ArchCondition<CoreModuleRoot> beFreeOfFlowUi() {
    return ArchitectureConditions.checkCondition(
        "be free of Flow UI dependencies and view resources",
        (moduleRoot, events) ->
            flowUiViolations(moduleRoot).forEach(violation -> events.add(violation.asEvent())));
  }

  private static java.util.List<CoreModuleViolation> flowUiViolations(CoreModuleRoot moduleRoot) {
    java.util.List<CoreModuleViolation> violations = new java.util.ArrayList<>();
    collectGradleFlowUiViolation(moduleRoot, violations);
    collectFlowUiResourceViolations(moduleRoot, violations);
    return violations;
  }

  private static void collectGradleFlowUiViolation(
      CoreModuleRoot moduleRoot, java.util.List<CoreModuleViolation> violations) {
    Path gradleFile = moduleRoot.path().resolve(moduleRoot.path().getFileName() + ".gradle");
    if (ArchitectureFiles.read(gradleFile).contains("jmix-flowui")) {
      violations.add(
          new CoreModuleViolation(
              moduleRoot,
              gradleFile,
              "must stay independent from Flow UI and not declare jmix-flowui dependencies"));
    }
  }

  private static void collectFlowUiResourceViolations(
      CoreModuleRoot moduleRoot, java.util.List<CoreModuleViolation> violations) {
    Path resources = moduleRoot.path().resolve(Path.of("src", "main", "resources"));
    if (!Files.exists(resources)) {
      return;
    }

    for (Path path : ArchitectureFiles.walk(resources)) {
      String relativePath = resources.relativize(path).toString().replace('\\', '/');
      if (relativePath.contains("/view/")) {
        violations.add(
            new CoreModuleViolation(moduleRoot, path, "must not contain Flow UI view resources"));
      }
      if (ArchitectureFiles.read(path).contains("jmix.io/schema/flowui")) {
        violations.add(
            new CoreModuleViolation(moduleRoot, path, "must not use Flow UI XML schema"));
      }
    }
  }

  public record CoreModuleRoot(String module, Path path) {}

  private record CoreModuleViolation(CoreModuleRoot moduleRoot, Path path, String message) {

    private ConditionEvent asEvent() {
      return SimpleConditionEvent.violated(moduleRoot, toString());
    }

    @Override
    public String toString() {
      return ArchitectureProject.projectRoot().relativize(path.toAbsolutePath().normalize())
          + " in "
          + moduleRoot.module()
          + " "
          + message;
    }
  }
}
