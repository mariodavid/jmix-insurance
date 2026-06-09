package com.insurance.common.test_support.architecture.scan;

import com.insurance.common.test_support.architecture.project.ArchitectureFiles;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class PersistentEntityIndex {

  private static final Pattern PERSISTENT_ENTITY_ANNOTATION_PATTERN =
      Pattern.compile("@(?:jakarta\\.persistence\\.)?Entity(?![A-Za-z0-9_])(?:\\s*\\(([^)]*)\\))?");

  private static final Pattern PERSISTENT_EMBEDDABLE_ANNOTATION_PATTERN =
      Pattern.compile("@(?:jakarta\\.persistence\\.)?Embeddable(?![A-Za-z0-9_])");

  private static final Pattern ENTITY_NAME_ATTRIBUTE_PATTERN =
      Pattern.compile("name\\s*=\\s*\"([^\"]+)\"");

  private PersistentEntityIndex() {}

  public static List<PersistentEntity> persistentEntitiesIn(ArchitectureSlice slice) {
    Path javaRoot =
        ArchitectureProject.coreLayerRoot(slice).resolve(Path.of("src", "main", "java"));
    return ArchitectureFiles.javaAndXmlFiles(javaRoot).stream()
        .map(path -> new SourceFile(path, ArchitectureFiles.read(path)))
        .filter(source -> declaresJakartaEntity(source.content()))
        .map(source -> new PersistentEntity(source.path(), slice, entityName(source.content())))
        .toList();
  }

  public static List<PersistentEmbeddable> persistentEmbeddablesIn(ArchitectureSlice slice) {
    Path javaRoot =
        ArchitectureProject.coreLayerRoot(slice).resolve(Path.of("src", "main", "java"));
    return ArchitectureFiles.javaAndXmlFiles(javaRoot).stream()
        .map(path -> new SourceFile(path, ArchitectureFiles.read(path)))
        .filter(source -> declaresJakartaEmbeddable(source.content()))
        .map(source -> new PersistentEmbeddable(source.path(), slice))
        .toList();
  }

  public static Map<String, ArchitectureSlice> persistentEntityOwners() {
    return ArchitectureSlice.entityBoundarySlices().stream()
        .flatMap(slice -> persistentEntitiesIn(slice).stream())
        .filter(entity -> entity.entityName() != null)
        .collect(
            Collectors.toMap(
                PersistentEntity::entityName,
                PersistentEntity::slice,
                (left, right) -> left,
                LinkedHashMap::new));
  }

  public static boolean containsEntityName(String content, String entityName) {
    String pattern = "(?<![A-Za-z0-9_])" + Pattern.quote(entityName) + "(?![A-Za-z0-9_])";
    return Pattern.compile(pattern).matcher(content).find();
  }

  private static boolean declaresJakartaEntity(String content) {
    return content.contains("jakarta.persistence.Entity")
        && PERSISTENT_ENTITY_ANNOTATION_PATTERN.matcher(content).find();
  }

  private static boolean declaresJakartaEmbeddable(String content) {
    return content.contains("jakarta.persistence.Embeddable")
        && PERSISTENT_EMBEDDABLE_ANNOTATION_PATTERN.matcher(content).find();
  }

  private static String entityName(String content) {
    Matcher entityMatcher = PERSISTENT_ENTITY_ANNOTATION_PATTERN.matcher(content);
    if (!entityMatcher.find()) {
      return null;
    }

    String attributes = entityMatcher.group(1);
    if (attributes == null) {
      return null;
    }

    Matcher nameMatcher = ENTITY_NAME_ATTRIBUTE_PATTERN.matcher(attributes);
    if (!nameMatcher.find()) {
      return null;
    }

    return nameMatcher.group(1);
  }

  public record PersistentEntity(Path path, ArchitectureSlice slice, String entityName) {}

  public record PersistentEmbeddable(Path path, ArchitectureSlice slice) {}

  private record SourceFile(Path path, String content) {}
}
