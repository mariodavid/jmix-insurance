package com.insurance.common.test_support.architecture.scan;

import com.insurance.common.test_support.architecture.project.ArchitectureFiles;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.insurance.common.test_support.architecture.scan.PersistentEntityIndex.PersistentEntity;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LiquibaseSchemaDrift {

  private static final Pattern TABLE_NAME_PATTERN =
      Pattern.compile("@Table\\s*\\((?:[^)]*?)name\\s*=\\s*\"([^\"]+)\"", Pattern.DOTALL);

  private static final Pattern COLUMN_FIELD_PATTERN =
      Pattern.compile(
          "@Column\\s*\\(([^)]*)\\)\\s*(?:@[A-Za-z0-9_.]+(?:\\([^)]*\\))?\\s*)*"
              + "private\\s+[^;=]+\\s+(\\w+)\\s*;",
          Pattern.DOTALL);

  private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile("name\\s*=\\s*\"([^\"]+)\"");

  private LiquibaseSchemaDrift() {}

  public static List<RequiredUniqueColumn> requiredUniqueColumns() {
    return ArchitectureSlice.entityBoundarySlices().stream()
        .flatMap(slice -> requiredUniqueColumns(slice).stream())
        .toList();
  }

  private static List<RequiredUniqueColumn> requiredUniqueColumns(ArchitectureSlice slice) {
    String changelogContent = changelogContent(slice);
    return PersistentEntityIndex.persistentEntitiesIn(slice).stream()
        .flatMap(entity -> requiredUniqueColumns(entity, changelogContent).stream())
        .toList();
  }

  private static List<RequiredUniqueColumn> requiredUniqueColumns(
      PersistentEntity entity, String changelogContent) {
    String entityContent = ArchitectureFiles.read(entity.path());
    String tableName = tableName(entityContent);
    if (tableName == null) {
      return List.of();
    }

    return requiredUniqueColumns(entityContent).stream()
        .map(
            uniqueColumn ->
                new RequiredUniqueColumn(
                    entity.path(),
                    tableName,
                    uniqueColumn.fieldName(),
                    uniqueColumn.columnName(),
                    liquibaseDeclaresUniqueConstraint(changelogContent, tableName, uniqueColumn)))
        .toList();
  }

  private static String changelogContent(ArchitectureSlice slice) {
    Path changelogRoot =
        ArchitectureProject.coreLayerRoot(slice)
            .resolve(Path.of("src", "main", "resources", "com", "insurance", slice.id()));
    return ArchitectureFiles.walkIfExists(changelogRoot).stream()
        .filter(path -> path.toString().endsWith(".xml"))
        .map(ArchitectureFiles::read)
        .reduce("", (left, right) -> left + "\n" + right);
  }

  private static String tableName(String entityContent) {
    Matcher matcher = TABLE_NAME_PATTERN.matcher(entityContent);
    return matcher.find() ? matcher.group(1) : null;
  }

  private static List<UniqueColumn> requiredUniqueColumns(String entityContent) {
    return COLUMN_FIELD_PATTERN
        .matcher(entityContent)
        .results()
        .filter(match -> isRequiredUniqueColumn(match.group(1)))
        .map(match -> new UniqueColumn(match.group(2), columnName(match.group(1))))
        .filter(uniqueColumn -> uniqueColumn.columnName() != null)
        .toList();
  }

  private static boolean isRequiredUniqueColumn(String columnAttributes) {
    return columnAttributes.contains("unique = true")
        && columnAttributes.contains("nullable = false");
  }

  private static String columnName(String columnAttributes) {
    Matcher matcher = COLUMN_NAME_PATTERN.matcher(columnAttributes);
    return matcher.find() ? matcher.group(1) : null;
  }

  private static boolean liquibaseDeclaresUniqueConstraint(
      String changelogContent, String tableName, UniqueColumn uniqueColumn) {
    return inlineCreateTableUniqueConstraint(changelogContent, tableName, uniqueColumn.columnName())
        || addUniqueConstraint(changelogContent, tableName, uniqueColumn.columnName());
  }

  private static boolean inlineCreateTableUniqueConstraint(
      String changelogContent, String tableName, String columnName) {
    String pattern =
        "<createTable[^>]*tableName=\""
            + Pattern.quote(tableName)
            + "\"[\\s\\S]*?<column[^>]*name=\""
            + Pattern.quote(columnName)
            + "\"[\\s\\S]*?<constraints[^>]*unique=\"true\"";
    return Pattern.compile(pattern).matcher(changelogContent).find();
  }

  private static boolean addUniqueConstraint(
      String changelogContent, String tableName, String columnName) {
    String tableBeforeColumn =
        "<addUniqueConstraint[^>]*tableName=\""
            + Pattern.quote(tableName)
            + "\"[^>]*columnNames=\""
            + Pattern.quote(columnName)
            + "\"";
    String columnBeforeTable =
        "<addUniqueConstraint[^>]*columnNames=\""
            + Pattern.quote(columnName)
            + "\"[^>]*tableName=\""
            + Pattern.quote(tableName)
            + "\"";
    return Pattern.compile(tableBeforeColumn).matcher(changelogContent).find()
        || Pattern.compile(columnBeforeTable).matcher(changelogContent).find();
  }

  private record UniqueColumn(String fieldName, String columnName) {}

  public record RequiredUniqueColumn(
      Path path,
      String tableName,
      String fieldName,
      String columnName,
      boolean liquibaseUniqueConstraintDeclared) {

    public String missingConstraintDescription() {
      return relativePath()
          + " declares unique required column "
          + tableName
          + "."
          + columnName
          + " for field "
          + fieldName
          + ", but the module Liquibase changelog does not declare a matching unique constraint.";
    }

    @Override
    public String toString() {
      return relativePath() + " " + tableName + "." + columnName;
    }

    private Path relativePath() {
      return ArchitectureProject.projectRoot().relativize(path.toAbsolutePath().normalize());
    }
  }
}
