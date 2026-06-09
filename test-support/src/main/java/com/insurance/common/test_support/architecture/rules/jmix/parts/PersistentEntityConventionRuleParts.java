package com.insurance.common.test_support.architecture.rules.jmix.parts;

import static com.insurance.common.test_support.architecture.scan.PersistentEntityIndex.*;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.insurance.common.test_support.architecture.project.ArchitectureFiles;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.insurance.common.test_support.architecture.scan.PersistentEntityIndex.PersistentEmbeddable;
import com.insurance.common.test_support.architecture.scan.PersistentEntityIndex.PersistentEntity;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PersistentEntityConventionRuleParts {

  private static final Pattern PACKAGE_DECLARATION_PATTERN =
      Pattern.compile("(?m)^\\s*package\\s+([A-Za-z0-9_.]+)\\s*;");

  private PersistentEntityConventionRuleParts() {}

  public static DescribedPredicate<JavaConstructorCall> jmixEntityConstructorCalls() {
    return DescribedPredicate.describe(
        "target is a foreign Jmix entity class",
        target -> {
          boolean isJmixEntity =
              target
                  .getTargetOwner()
                  .isAnnotatedWith(io.jmix.core.metamodel.annotation.JmixEntity.class);
          boolean isInheritanceCall =
              target.getOriginOwner().isAssignableTo(target.getTargetOwner().getName());
          return isJmixEntity && !isInheritanceCall;
        });
  }

  @SuppressWarnings("unchecked")
  public static DescribedPredicate<JavaAnnotation<?>> lombokAnnotations() {
    return (DescribedPredicate<JavaAnnotation<?>>)
        (DescribedPredicate<?>)
            com.tngtech.archunit.core.domain.properties.HasType.Functions.GET_RAW_TYPE
                .is(
                    com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage(
                        "lombok.."))
                .as("Lombok annotation");
  }

  public static ClassesTransformer<PersistentEntity> persistentEntities() {
    return ArchitectureConditions.transformClasses(
        "persistent entities",
        classes ->
            ArchitectureSlice.entityBoundarySlices().stream()
                .flatMap(slice -> persistentEntitiesIn(slice).stream())
                .toList());
  }

  public static ClassesTransformer<PersistentEmbeddable> persistentEmbeddables() {
    return ArchitectureConditions.transformClasses(
        "persistent embeddables",
        classes ->
            ArchitectureSlice.entityBoundarySlices().stream()
                .flatMap(slice -> persistentEmbeddablesIn(slice).stream())
                .toList());
  }

  public static ArchCondition<PersistentEntity> haveModulePrefixedJmixNames() {
    return ArchitectureConditions.checkCondition(
        "have module-prefixed Jmix names",
        (entity, events) -> {
          String expectedPrefix = ArchitectureProject.coreEntityPrefix(entity.slice());
          if (entity.entityName() == null
              || !entity.entityName().startsWith(expectedPrefix + "_")) {
            events.add(
                SimpleConditionEvent.violated(
                    entity, entityNamingViolation(entity, expectedPrefix)));
          }
        });
  }

  public static ArchCondition<PersistentEntity> resideInOwningCoreEntityPackage() {
    return ArchitectureConditions.checkCondition(
        "reside in the owning core entity package",
        (entity, events) -> {
          String expectedPackage = ArchitectureProject.entityPackagePrefixOf(entity.slice());
          String actualPackage = packageName(entity.path());
          if (!ArchitectureProject.isEntityPackage(actualPackage, entity.slice())) {
            events.add(
                SimpleConditionEvent.violated(
                    entity, entityPackageViolation(entity, actualPackage, expectedPackage)));
          }
        });
  }

  public static ArchCondition<PersistentEmbeddable> resideInOwningCoreEntityPackageAsEmbeddables() {
    return ArchitectureConditions.checkCondition(
        "reside in the owning core entity package",
        (embeddable, events) -> {
          String expectedPackage = ArchitectureProject.entityPackagePrefixOf(embeddable.slice());
          String actualPackage = packageName(embeddable.path());
          if (!ArchitectureProject.isEntityPackage(actualPackage, embeddable.slice())) {
            events.add(
                SimpleConditionEvent.violated(
                    embeddable,
                    embeddablePackageViolation(embeddable, actualPackage, expectedPackage)));
          }
        });
  }

  private static String packageName(Path path) {
    Matcher packageMatcher = PACKAGE_DECLARATION_PATTERN.matcher(ArchitectureFiles.read(path));
    return packageMatcher.find() ? packageMatcher.group(1) : null;
  }

  private static String entityNamingViolation(PersistentEntity entity, String expectedPrefix) {
    if (entity.entityName() == null) {
      return relativePath(entity.path())
          + " in module "
          + entity.slice().id()
          + " must declare @Entity(name = \""
          + expectedPrefix
          + "_...\")";
    }

    return relativePath(entity.path())
        + " in module "
        + entity.slice().id()
        + " declares entity name "
        + entity.entityName()
        + ", but expected prefix "
        + expectedPrefix
        + "_ from "
        + entity.slice().id()
        + "/build.gradle jmix.projectId.";
  }

  private static String entityPackageViolation(
      PersistentEntity entity, String actualPackage, String expectedPackage) {
    return relativePath(entity.path())
        + " in module "
        + entity.slice().id()
        + " declares package "
        + actualPackage
        + ", but persistent core entities must reside in "
        + expectedPackage
        + " or one of its subpackages.";
  }

  private static String embeddablePackageViolation(
      PersistentEmbeddable embeddable, String actualPackage, String expectedPackage) {
    return relativePath(embeddable.path())
        + " in module "
        + embeddable.slice().id()
        + " declares package "
        + actualPackage
        + ", but persistent core embeddables must reside in "
        + expectedPackage
        + " or one of its subpackages.";
  }

  private static String relativePath(Path path) {
    return ArchitectureProject.projectRoot()
        .relativize(path.toAbsolutePath().normalize())
        .toString();
  }
}
