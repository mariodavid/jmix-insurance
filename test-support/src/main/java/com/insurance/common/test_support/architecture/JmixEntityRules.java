package com.insurance.common.test_support.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.common.test_support.architecture.PersistentEntityIndex.PersistentEmbeddable;
import com.insurance.common.test_support.architecture.PersistentEntityIndex.PersistentEntity;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmixEntityRules {

  private static final Pattern PACKAGE_DECLARATION_PATTERN =
      Pattern.compile("(?m)^\\s*package\\s+([A-Za-z0-9_.]+)\\s*;");

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
  public static void corePersistentEntitiesDeclareModulePrefixedEntityNames(JavaClasses classes) {
    List<EntityNamingViolation> violations = new ArrayList<>();

    for (String module : ArchitectureProject.entityBoundaryModules()) {
      String expectedPrefix = ArchitectureProject.coreEntityPrefix(module);
      for (PersistentEntity entity : PersistentEntityIndex.persistentEntitiesIn(module)) {
        if (entity.entityName() == null || !entity.entityName().startsWith(expectedPrefix + "_")) {
          violations.add(new EntityNamingViolation(entity, expectedPrefix));
        }
      }
    }

    assertThat(violations)
        .as(
            "Persistent Jmix entities in core modules must declare @Entity(name = ...) "
                + "with the owning module's jmix.projectId prefix.")
        .isEmpty();
  }

  @ArchTest
  public static void corePersistentEntitiesResideInModuleEntityPackages(JavaClasses classes) {
    List<EntityPackageViolation> violations = new ArrayList<>();

    for (String module : ArchitectureProject.entityBoundaryModules()) {
      String expectedPackage = "com.insurance." + module + ".core.entity";
      for (PersistentEntity entity : PersistentEntityIndex.persistentEntitiesIn(module)) {
        String actualPackage = packageName(entity);
        if (actualPackage == null
            || (!actualPackage.equals(expectedPackage)
                && !actualPackage.startsWith(expectedPackage + "."))) {
          violations.add(new EntityPackageViolation(entity, actualPackage, expectedPackage));
        }
      }
    }

    assertThat(violations)
        .as(
            "Persistent core entities must reside below com.insurance.<module>.core.entity "
                + "so architecture rules can derive module boundaries from package structure.")
        .isEmpty();
  }

  @ArchTest
  public static void corePersistentEmbeddablesResideInModuleEntityPackages(JavaClasses classes) {
    List<EmbeddablePackageViolation> violations = new ArrayList<>();

    for (String module : ArchitectureProject.entityBoundaryModules()) {
      String expectedPackage = "com.insurance." + module + ".core.entity";
      for (PersistentEmbeddable embeddable :
          PersistentEntityIndex.persistentEmbeddablesIn(module)) {
        String actualPackage = packageName(embeddable.path());
        if (actualPackage == null
            || (!actualPackage.equals(expectedPackage)
                && !actualPackage.startsWith(expectedPackage + "."))) {
          violations.add(
              new EmbeddablePackageViolation(embeddable, actualPackage, expectedPackage));
        }
      }
    }

    assertThat(violations)
        .as(
            "Persistent core embeddables must reside below com.insurance.<module>.core.entity "
                + "so embedded references stay owned by the consuming module.")
        .isEmpty();
  }

  private static String packageName(PersistentEntity entity) {
    return packageName(entity.path());
  }

  private static String packageName(java.nio.file.Path path) {
    Matcher packageMatcher = PACKAGE_DECLARATION_PATTERN.matcher(ArchitectureFiles.read(path));
    return packageMatcher.find() ? packageMatcher.group(1) : null;
  }

  private record EntityNamingViolation(PersistentEntity entity, String expectedPrefix) {

    @Override
    public String toString() {
      String relativePath =
          ArchitectureProject.projectRoot()
              .relativize(entity.path().toAbsolutePath().normalize())
              .toString();
      if (entity.entityName() == null) {
        return relativePath
            + " in module "
            + entity.module()
            + " must declare @Entity(name = \""
            + expectedPrefix
            + "_...\")";
      }

      return relativePath
          + " in module "
          + entity.module()
          + " declares entity name "
          + entity.entityName()
          + ", but expected prefix "
          + expectedPrefix
          + "_ from "
          + entity.module()
          + "/build.gradle jmix.projectId.";
    }
  }

  private record EntityPackageViolation(
      PersistentEntity entity, String actualPackage, String expectedPackage) {

    @Override
    public String toString() {
      String relativePath =
          ArchitectureProject.projectRoot()
              .relativize(entity.path().toAbsolutePath().normalize())
              .toString();
      return relativePath
          + " in module "
          + entity.module()
          + " declares package "
          + actualPackage
          + ", but persistent core entities must reside in "
          + expectedPackage
          + " or one of its subpackages.";
    }
  }

  private record EmbeddablePackageViolation(
      PersistentEmbeddable embeddable, String actualPackage, String expectedPackage) {

    @Override
    public String toString() {
      String relativePath =
          ArchitectureProject.projectRoot()
              .relativize(embeddable.path().toAbsolutePath().normalize())
              .toString();
      return relativePath
          + " in module "
          + embeddable.module()
          + " declares package "
          + actualPackage
          + ", but persistent core embeddables must reside in "
          + expectedPackage
          + " or one of its subpackages.";
    }
  }
}
