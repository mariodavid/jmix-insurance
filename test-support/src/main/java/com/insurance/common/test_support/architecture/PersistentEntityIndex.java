package com.insurance.common.test_support.architecture;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PersistentEntityIndex {

  private static final Pattern PERSISTENT_ENTITY_ANNOTATION_PATTERN =
      Pattern.compile("@(?:jakarta\\.persistence\\.)?Entity(?![A-Za-z0-9_])(?:\\s*\\(([^)]*)\\))?");

  private static final Pattern PERSISTENT_EMBEDDABLE_ANNOTATION_PATTERN =
      Pattern.compile("@(?:jakarta\\.persistence\\.)?Embeddable(?![A-Za-z0-9_])");

  private static final Pattern ENTITY_NAME_ATTRIBUTE_PATTERN =
      Pattern.compile("name\\s*=\\s*\"([^\"]+)\"");

  private PersistentEntityIndex() {}

  public static List<PersistentEntity> persistentEntitiesIn(String module) {
    List<PersistentEntity> entities = new ArrayList<>();
    Path javaRoot =
        ArchitectureProject.layerRoot(module, "core").resolve(Path.of("src", "main", "java"));

    for (Path path : ArchitectureFiles.javaAndXmlFiles(javaRoot)) {
      String content = ArchitectureFiles.read(path);
      if (declaresJakartaEntity(content)) {
        entities.add(new PersistentEntity(path, module, entityName(content)));
      }
    }

    return entities;
  }

  public static List<PersistentEmbeddable> persistentEmbeddablesIn(String module) {
    List<PersistentEmbeddable> embeddables = new ArrayList<>();
    Path javaRoot =
        ArchitectureProject.layerRoot(module, "core").resolve(Path.of("src", "main", "java"));

    for (Path path : ArchitectureFiles.javaAndXmlFiles(javaRoot)) {
      String content = ArchitectureFiles.read(path);
      if (declaresJakartaEmbeddable(content)) {
        embeddables.add(new PersistentEmbeddable(path, module));
      }
    }

    return embeddables;
  }

  public static Map<String, String> persistentEntityOwners() {
    Map<String, String> owners = new LinkedHashMap<>();

    for (String module : ArchitectureProject.entityBoundaryModules()) {
      for (PersistentEntity entity : persistentEntitiesIn(module)) {
        if (entity.entityName() != null) {
          owners.put(entity.entityName(), module);
        }
      }
    }

    return owners;
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

  public record PersistentEntity(Path path, String module, String entityName) {}

  public record PersistentEmbeddable(Path path, String module) {}
}
