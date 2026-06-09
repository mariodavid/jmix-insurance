package com.insurance.common.test_support.architecture.scan;

import com.insurance.common.test_support.architecture.project.ArchitectureFiles;
import com.insurance.common.test_support.architecture.project.ArchitectureLayer;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SourceBoundaryReferences {

  private SourceBoundaryReferences() {}

  public static List<ForeignEntityNameReference> foreignPersistentEntityNameReferences() {
    Map<String, ArchitectureSlice> entityOwners = PersistentEntityIndex.persistentEntityOwners();
    return ArchitectureSlice.entityBoundarySlices().stream()
        .flatMap(slice -> foreignEntityNameReferencesIn(slice, entityOwners).stream())
        .toList();
  }

  private static List<ForeignEntityNameReference> foreignEntityNameReferencesIn(
      ArchitectureSlice slice, Map<String, ArchitectureSlice> entityOwners) {
    return ArchitectureProject.productionJavaAndXmlFiles(slice).stream()
        .flatMap(path -> foreignEntityNameReferencesIn(path, slice, entityOwners).stream())
        .toList();
  }

  private static List<ForeignEntityNameReference> foreignEntityNameReferencesIn(
      Path path, ArchitectureSlice slice, Map<String, ArchitectureSlice> entityOwners) {
    String content = ArchitectureFiles.read(path);
    return entityOwners.entrySet().stream()
        .filter(entry -> entry.getValue() != slice)
        .filter(entry -> PersistentEntityIndex.containsEntityName(content, entry.getKey()))
        .map(entry -> new ForeignEntityNameReference(path, slice, entry.getKey(), entry.getValue()))
        .toList();
  }

  public static List<ForeignClassStringReference> foreignUiXmlClassReferences() {
    return ArchitectureSlice.domainSlices().stream()
        .flatMap(slice -> foreignUiXmlClassReferencesIn(slice).stream())
        .toList();
  }

  private static List<ForeignClassStringReference> foreignUiXmlClassReferencesIn(
      ArchitectureSlice slice) {
    Path uiResources =
        ArchitectureProject.layerRoot(slice, ArchitectureLayer.UI)
            .resolve(Path.of("src", "main", "resources"));
    return ArchitectureFiles.walkIfExists(uiResources).stream()
        .filter(path -> path.toString().endsWith(".xml"))
        .flatMap(path -> foreignUiXmlClassReferencesIn(path, slice).stream())
        .toList();
  }

  private static List<ForeignClassStringReference> foreignUiXmlClassReferencesIn(
      Path path, ArchitectureSlice slice) {
    String content = ArchitectureFiles.read(path);
    Pattern pattern =
        Pattern.compile(
            Pattern.quote(ArchitectureProject.basePackage())
                + "\\.([a-z0-9_]+)\\.(core|ui)(?!\\.api)\\b");
    Matcher matcher = pattern.matcher(content);
    return matcher
        .results()
        .filter(matchResult -> !matchResult.group(1).equals(slice.id()))
        .flatMap(
            matchResult ->
                ArchitectureSlice.fromId(matchResult.group(1))
                    .map(
                        targetSlice ->
                            new ForeignClassStringReference(
                                path, slice, matchResult.group(0), targetSlice))
                    .stream())
        .toList();
  }

  public record ForeignEntityNameReference(
      Path path,
      ArchitectureSlice referencingSlice,
      String entityName,
      ArchitectureSlice entityOwner) {

    @Override
    public String toString() {
      return ArchitectureProject.projectRoot().relativize(path.toAbsolutePath().normalize())
          + " in module "
          + referencingSlice.id()
          + " references foreign entity "
          + entityName
          + " owned by "
          + entityOwner.id()
          + ". Use "
          + entityOwner.id()
          + "-api instead of direct entity access.";
    }
  }

  public record ForeignClassStringReference(
      Path path,
      ArchitectureSlice referencingSlice,
      String classPrefix,
      ArchitectureSlice targetSlice) {

    @Override
    public String toString() {
      return ArchitectureProject.projectRoot().relativize(path.toAbsolutePath().normalize())
          + " in module "
          + referencingSlice.id()
          + " references forbidden foreign class/package "
          + classPrefix
          + " from "
          + targetSlice.id()
          + ". Use "
          + targetSlice.id()
          + "-api or a *-ui-api contract instead.";
    }
  }
}
