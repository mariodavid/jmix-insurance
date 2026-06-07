package com.insurance.common.test_support.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.common.test_support.architecture.PersistentEntityIndex.PersistentEmbeddable;
import com.insurance.common.test_support.architecture.PersistentEntityIndex.PersistentEntity;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.ArchTest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmixDomainReferenceRules {

  private static final Pattern IMPORT_PATTERN =
      Pattern.compile("^\\s*import\\s+(?:static\\s+)?([^;]+);\\s*$");

  @ArchTest
  public static void persistentDomainModelsOnlyImportOwnDomainModelOrForeignApiEnums(
      JavaClasses classes) {
    List<ForeignEntityModelImport> violations = new ArrayList<>();

    for (String module : ArchitectureProject.entityBoundaryModules()) {
      for (PersistentEntity entity : PersistentEntityIndex.persistentEntitiesIn(module)) {
        violations.addAll(violationsIn(entity.path(), module, "entity"));
      }
      for (PersistentEmbeddable embeddable :
          PersistentEntityIndex.persistentEmbeddablesIn(module)) {
        violations.addAll(violationsIn(embeddable.path(), module, "embeddable"));
      }
    }

    assertThat(violations)
        .as(
            "Persistent core entities and embeddables must not import foreign domain/entity models. "
                + "Use local fields or consumer-owned embedded references; foreign API enums are "
                + "allowed as stable value contracts.")
        .isEmpty();
  }

  private static List<ForeignEntityModelImport> violationsIn(
      Path path, String module, String modelKind) {
    String[] lines = ArchitectureFiles.read(path).split("\\R", -1);
    List<ForeignEntityModelImport> violations = new ArrayList<>();

    for (int index = 0; index < lines.length; index++) {
      Matcher importMatcher = IMPORT_PATTERN.matcher(lines[index]);
      if (!importMatcher.matches()) {
        continue;
      }

      String importedType = importMatcher.group(1).trim();
      if (!isInsuranceImport(importedType) || isOwnModuleImport(importedType, module)) {
        continue;
      }

      if (isForeignApiEnum(importedType)) {
        continue;
      }

      violations.add(
          new ForeignEntityModelImport(path, module, modelKind, index + 1, importedType));
    }

    return violations;
  }

  private static boolean isInsuranceImport(String importedType) {
    return importedType.startsWith("com.insurance.");
  }

  private static boolean isOwnModuleImport(String importedType, String module) {
    return importedType.startsWith("com.insurance." + module + ".");
  }

  private static boolean isForeignApiEnum(String importedType) {
    ImportedSource importedSource = ImportedSource.resolve(importedType);
    return importedSource != null
        && importedSource.layer().equals("api")
        && importedSource.declaresEnum();
  }

  private record ImportedSource(String module, String layer, String className, Path sourceFile) {

    private static ImportedSource resolve(String importedType) {
      if (importedType.endsWith(".*")) {
        return null;
      }

      String candidate = importedType;
      while (candidate.startsWith("com.insurance.") && candidate.contains(".")) {
        ImportedSource source = sourceFor(candidate);
        if (source != null && Files.exists(source.sourceFile())) {
          return source;
        }

        int lastDot = candidate.lastIndexOf('.');
        if (lastDot < 0) {
          return null;
        }
        candidate = candidate.substring(0, lastDot);
      }

      return null;
    }

    private static ImportedSource sourceFor(String className) {
      String[] parts = className.split("\\.");
      if (parts.length < 5
          || !parts[0].equals("com")
          || !parts[1].equals("insurance")
          || !ArchitectureProject.entityBoundaryModules().contains(parts[2])) {
        return null;
      }

      String module = parts[2];
      String layer = parts[3];
      Path sourceFile =
          ArchitectureProject.layerRoot(module, layer)
              .resolve(Path.of("src", "main", "java"))
              .resolve(Path.of(className.replace('.', '/') + ".java"));

      return new ImportedSource(module, layer, parts[parts.length - 1], sourceFile);
    }

    private boolean declaresEnum() {
      String content = ArchitectureFiles.read(sourceFile);
      Pattern enumDeclarationPattern =
          Pattern.compile(
              "(?m)(?:^|\\s)(?:public\\s+)?enum\\s+" + Pattern.quote(className) + "\\b");
      return enumDeclarationPattern.matcher(content).find();
    }
  }

  private record ForeignEntityModelImport(
      Path path, String module, String modelKind, int lineNumber, String importedType) {

    @Override
    public String toString() {
      return ArchitectureProject.projectRoot().relativize(path.toAbsolutePath().normalize())
          + ":"
          + lineNumber
          + " in module "
          + module
          + " "
          + modelKind
          + " imports "
          + importedType
          + ". Persistent domain models may import their own module model and foreign API enums only. "
          + "Use scalar fields or a local embedded reference instead.";
    }
  }
}
