package com.insurance.common.test_support.architecture.rules.entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Enforces conventions for consumer-owned embedded references used to reference foreign aggregates
 * across module boundaries.
 */
public class EmbeddedReferenceConventionRules {

  @ArchTest
  public static final ArchRule embeddedReferencesStayLocalToConsumerModules =
      classes()
          .that(embeddedReferenceTypes())
          .should(resideInConsumerCoreEntityPackage())
          .andShould(beEmbeddableJmixEntities())
          .andShould(notDependOnForeignPersistentEntities())
          .as("embedded references should stay local to the consuming module");

  public static DescribedPredicate<JavaClass> embeddedReferenceTypes() {
    return DescribedPredicate.describe(
        "embedded reference types",
        javaClass ->
            javaClass.getSimpleName().endsWith("Reference")
                && !javaClass.isInterface()
                && !javaClass
                    .getModifiers()
                    .contains(com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT)
                && !javaClass
                    .getPackageName()
                    .startsWith(ArchitectureProject.testSupportPackage()));
  }

  public static ArchCondition<JavaClass> resideInConsumerCoreEntityPackage() {
    return ArchitectureConditions.checkCondition(
        "reside in consumer core entity package",
        (javaClass, events) -> {
          String packageName = javaClass.getPackageName();
          java.util.regex.Matcher matcher =
              java.util.regex.Pattern.compile(
                      "^"
                          + java.util.regex.Pattern.quote(ArchitectureProject.basePackage())
                          + "\\.[^.]+\\.core\\.entity(\\..*)?$")
                  .matcher(packageName);
          if (!matcher.find()) {
            String message =
                String.format(
                    "Class %s does not reside in a consumer core entity package",
                    javaClass.getName());
            events.add(SimpleConditionEvent.violated(javaClass, message));
          }
        });
  }

  public static ArchCondition<JavaClass> beEmbeddableJmixEntities() {
    return ArchitectureConditions.checkCondition(
        "be @Embeddable and @JmixEntity, but not @Entity",
        (javaClass, events) -> {
          boolean isEmbeddable = javaClass.isAnnotatedWith(jakarta.persistence.Embeddable.class);
          boolean isJmixEntity =
              javaClass.isAnnotatedWith(io.jmix.core.metamodel.annotation.JmixEntity.class);
          boolean isEntity = javaClass.isAnnotatedWith(jakarta.persistence.Entity.class);

          if (!isEmbeddable) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "Class %s is not annotated with @Embeddable", javaClass.getName())));
          }
          if (!isJmixEntity) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "Class %s is not annotated with @JmixEntity", javaClass.getName())));
          }
          if (isEntity) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "Class %s is annotated with @Entity (it must not be)",
                        javaClass.getName())));
          }

          boolean hasInstanceName = false;
          for (com.tngtech.archunit.core.domain.JavaField field : javaClass.getFields()) {
            if (field.isAnnotatedWith(io.jmix.core.metamodel.annotation.InstanceName.class)) {
              hasInstanceName = true;
              break;
            }
          }
          if (!hasInstanceName) {
            for (com.tngtech.archunit.core.domain.JavaMethod method : javaClass.getMethods()) {
              if (method.isAnnotatedWith(io.jmix.core.metamodel.annotation.InstanceName.class)) {
                hasInstanceName = true;
                break;
              }
            }
          }
          if (!hasInstanceName) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format("Class %s does not have an @InstanceName", javaClass.getName())));
          }
        });
  }

  public static ArchCondition<JavaClass> notDependOnForeignPersistentEntities() {
    return ArchitectureConditions.checkCondition(
        "not depend on foreign persistent entities",
        (javaClass, events) -> {
          String packageName = javaClass.getPackageName();
          java.util.regex.Matcher matcher =
              java.util.regex.Pattern.compile(
                      "^"
                          + java.util.regex.Pattern.quote(ArchitectureProject.basePackage())
                          + "\\.([^.]+)\\.core\\.entity")
                  .matcher(packageName);
          if (matcher.find()) {
            String localModule = matcher.group(1);
            String foreignPrefix = ArchitectureProject.basePackage() + ".";
            String localPrefix = ArchitectureProject.basePackage() + "." + localModule + ".";

            for (com.tngtech.archunit.core.domain.Dependency dep :
                javaClass.getDirectDependenciesFromSelf()) {
              String targetPackage = dep.getTargetClass().getPackageName();
              if (targetPackage.startsWith(foreignPrefix)
                  && !targetPackage.startsWith(localPrefix)
                  && targetPackage.contains(".core.entity")) {
                String message =
                    String.format(
                        "Embedded reference class %s depends on foreign persistent entity type %s",
                        javaClass.getName(), dep.getTargetClass().getName());
                events.add(SimpleConditionEvent.violated(javaClass, message));
              }
            }
          }
        });
  }
}
