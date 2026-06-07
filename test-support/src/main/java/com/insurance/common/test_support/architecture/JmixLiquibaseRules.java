package com.insurance.common.test_support.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.common.test_support.architecture.PersistentEntityIndex.PersistentEntity;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.ArchTest;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmixLiquibaseRules {

  private static final Pattern TABLE_NAME_PATTERN =
      Pattern.compile("@Table\\s*\\((?:[^)]*?)name\\s*=\\s*\"([^\"]+)\"", Pattern.DOTALL);

  private static final Pattern COLUMN_FIELD_PATTERN =
      Pattern.compile(
          "@Column\\s*\\(([^)]*)\\)\\s*(?:@[A-Za-z0-9_.]+(?:\\([^)]*\\))?\\s*)*"
              + "private\\s+[^;=]+\\s+(\\w+)\\s*;",
          Pattern.DOTALL);

  private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile("name\\s*=\\s*\"([^\"]+)\"");

  @ArchTest
  public static void requiredUniqueEntityColumnsAreBackedByLiquibase(JavaClasses classes) {
    List<UniqueColumnViolation> violations = new ArrayList<>();

    for (String module : ArchitectureProject.entityBoundaryModules()) {
      String changelogContent = changelogContent(module);
      for (PersistentEntity entity : PersistentEntityIndex.persistentEntitiesIn(module)) {
        String entityContent = ArchitectureFiles.read(entity.path());
        String tableName = tableName(entityContent);
        if (tableName == null) {
          continue;
        }

        for (UniqueColumn uniqueColumn : requiredUniqueColumns(entityContent)) {
          if (!liquibaseDeclaresUniqueConstraint(changelogContent, tableName, uniqueColumn)) {
            violations.add(new UniqueColumnViolation(entity.path(), tableName, uniqueColumn));
          }
        }
      }
    }

    assertThat(violations)
        .as(
            "Persistent business keys declared with @Column(unique = true, nullable = false) "
                + "must be backed by a Liquibase unique constraint.")
        .isEmpty();
  }

  private static String changelogContent(String module) {
    Path changelogRoot =
        ArchitectureProject.layerRoot(module, "core")
            .resolve(Path.of("src", "main", "resources", "com", "insurance", module));
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
    List<UniqueColumn> uniqueColumns = new ArrayList<>();
    Matcher matcher = COLUMN_FIELD_PATTERN.matcher(entityContent);

    while (matcher.find()) {
      String columnAttributes = matcher.group(1);
      if (columnAttributes.contains("unique = true")
          && columnAttributes.contains("nullable = false")) {
        String columnName = columnName(columnAttributes);
        if (columnName != null) {
          uniqueColumns.add(new UniqueColumn(matcher.group(2), columnName));
        }
      }
    }

    return uniqueColumns;
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

  private record UniqueColumnViolation(Path path, String tableName, UniqueColumn uniqueColumn) {

    @Override
    public String toString() {
      return ArchitectureProject.projectRoot().relativize(path.toAbsolutePath().normalize())
          + " declares unique required column "
          + tableName
          + "."
          + uniqueColumn.columnName()
          + " for field "
          + uniqueColumn.fieldName()
          + ", but the module Liquibase changelog does not declare a matching unique constraint.";
    }
  }
}
