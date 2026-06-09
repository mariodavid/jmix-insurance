package com.insurance.common.test_support.architecture.rules.jmix.parts;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public final class JmixDtoEntityRuleParts {

  private static final String JMIX_ENTITY = "io.jmix.core.metamodel.annotation.JmixEntity";
  private static final String JMIX_ID = "io.jmix.core.entity.annotation.JmixId";
  private static final String JMIX_GENERATED_VALUE =
      "io.jmix.core.entity.annotation.JmixGeneratedValue";

  private JmixDtoEntityRuleParts() {}

  public static DescribedPredicate<JavaClass> apiDtoEntities() {
    return DescribedPredicate.describe(
        "API DTO entities",
        javaClass ->
            javaClass.getPackageName().contains(".api.dto")
                && javaClass.isAnnotatedWith(JMIX_ENTITY));
  }

  public static ArchCondition<JavaClass> haveApiDtoEntityName() {
    return ArchitectureConditions.checkCondition(
        "have Jmix entity name in format <module>_api_<ClassName>",
        (javaClass, events) -> {
          JavaAnnotation<?> annotation = javaClass.getAnnotationOfType(JMIX_ENTITY);
          Object name = annotation.getProperties().get("name");
          if (name == null || name.toString().isEmpty()) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format("API DTO %s has no JmixEntity name", javaClass.getName())));
            return;
          }

          String expectedName = moduleName(javaClass) + "_api_" + javaClass.getSimpleName();
          if (!name.toString().equals(expectedName)) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "API DTO %s JmixEntity name \"%s\" must be exactly \"%s\"",
                        javaClass.getName(), name, expectedName)));
          }
        });
  }

  public static ArchCondition<JavaClass> haveExactlyOneJmixId() {
    return ArchitectureConditions.checkCondition(
        "have exactly one @JmixId and match @JmixGeneratedValue member if used",
        (javaClass, events) -> {
          DtoIdMetadata idMetadata = DtoIdMetadata.from(javaClass);

          if (idMetadata.jmixIdCount() != 1) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "API DTO %s must have exactly one @JmixId but has %d",
                        javaClass.getName(), idMetadata.jmixIdCount())));
          }

          if (idMetadata.generatedValueMemberName() != null
              && (idMetadata.jmixIdMemberName() == null
                  || !idMetadata
                      .generatedValueMemberName()
                      .equals(idMetadata.jmixIdMemberName()))) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "API DTO %s: @JmixGeneratedValue on member \"%s\" must lie on the same member as @JmixId (\"%s\")",
                        javaClass.getName(),
                        idMetadata.generatedValueMemberName(),
                        idMetadata.jmixIdMemberName())));
          }
        });
  }

  public static ArchCondition<JavaClass> notUseJpaPersistenceAnnotations() {
    return ArchitectureConditions.checkCondition(
        "not use JPA persistence annotations",
        (javaClass, events) -> {
          if (usesJpaPersistenceAnnotations(javaClass)) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "API DTO %s declares persistent JPA annotations", javaClass.getName())));
          }
        });
  }

  private static String moduleName(JavaClass javaClass) {
    String[] segments = javaClass.getPackageName().split("\\.");
    return segments.length >= 3 ? segments[2] : "";
  }

  private static boolean usesJpaPersistenceAnnotations(JavaClass javaClass) {
    return javaClass.isAnnotatedWith("jakarta.persistence.Entity")
        || javaClass.isAnnotatedWith("javax.persistence.Entity")
        || javaClass.isAnnotatedWith("jakarta.persistence.Table")
        || javaClass.isAnnotatedWith("javax.persistence.Table")
        || javaClass.isAnnotatedWith("jakarta.persistence.Embeddable")
        || javaClass.isAnnotatedWith("javax.persistence.Embeddable")
        || javaClass.isAnnotatedWith("jakarta.persistence.MappedSuperclass")
        || javaClass.isAnnotatedWith("javax.persistence.MappedSuperclass");
  }

  private record DtoIdMetadata(
      int jmixIdCount, String jmixIdMemberName, String generatedValueMemberName) {

    static DtoIdMetadata from(JavaClass javaClass) {
      int jmixIdCount = 0;
      String jmixIdMemberName = null;
      String generatedValueMemberName = null;

      for (var field : javaClass.getFields()) {
        if (field.isAnnotatedWith(JMIX_ID)) {
          jmixIdCount++;
          jmixIdMemberName = field.getName();
        }
        if (field.isAnnotatedWith(JMIX_GENERATED_VALUE)) {
          generatedValueMemberName = field.getName();
        }
      }

      for (var method : javaClass.getMethods()) {
        if (method.isAnnotatedWith(JMIX_ID)) {
          jmixIdCount++;
          jmixIdMemberName = method.getName();
        }
        if (method.isAnnotatedWith(JMIX_GENERATED_VALUE)) {
          generatedValueMemberName = method.getName();
        }
      }

      return new DtoIdMetadata(jmixIdCount, jmixIdMemberName, generatedValueMemberName);
    }
  }
}
