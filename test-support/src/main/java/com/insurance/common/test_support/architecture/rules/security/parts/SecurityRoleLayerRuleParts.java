package com.insurance.common.test_support.architecture.rules.security.parts;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.properties.HasAnnotations;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;

public final class SecurityRoleLayerRuleParts {

  private static final String RESOURCE_ROLE = "io.jmix.security.role.annotation.ResourceRole";
  private static final String FULL_ACCESS_ROLE =
      "com.insurance.security.api.security.FullAccessRole";

  private SecurityRoleLayerRuleParts() {}

  public static ArchCondition<JavaClass> onlyDeclareCoreSecurityPolicies() {
    return ArchitectureConditions.checkCondition(
        "only declare core security policies",
        (javaClass, events) -> {
          if (hasUiPolicy(javaClass)) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "Core security role %s declares UI policy at class level",
                        javaClass.getName())));
          }
          for (JavaMethod method : javaClass.getMethods()) {
            if (hasUiPolicy(method)) {
              events.add(
                  SimpleConditionEvent.violated(
                      javaClass,
                      String.format(
                          "Core security role %s: method %s declares UI policy",
                          javaClass.getName(), method.getName())));
            }
          }
        });
  }

  public static ArchCondition<JavaClass> onlyDeclareUiSecurityPolicies() {
    return ArchitectureConditions.checkCondition(
        "only declare UI security policies",
        (javaClass, events) -> {
          if (hasEntityPolicy(javaClass)) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "UI security role %s declares entity policy at class level",
                        javaClass.getName())));
          }
          for (JavaMethod method : javaClass.getMethods()) {
            if (hasEntityPolicy(method)) {
              events.add(
                  SimpleConditionEvent.violated(
                      javaClass,
                      String.format(
                          "UI security role %s: method %s declares entity policy",
                          javaClass.getName(), method.getName())));
            }
          }
        });
  }

  public static ArchCondition<JavaClass> useWildcardsOnlyInAllowedRoles() {
    return ArchitectureConditions.checkCondition(
        "only use wildcard policies in allowed roles",
        (javaClass, events) -> {
          if (javaClass.getName().equals(FULL_ACCESS_ROLE)) {
            return;
          }
          checkWildcards(javaClass, javaClass, events::add);
          for (JavaMethod method : javaClass.getMethods()) {
            checkWildcards(javaClass, method, events::add);
          }
        });
  }

  public static ArchCondition<JavaClass> haveRoleCodeMatchingConvention() {
    return ArchitectureConditions.checkCondition(
        "have role code matching conventions",
        (javaClass, events) -> {
          if (!javaClass.isAnnotatedWith(RESOURCE_ROLE)) {
            return;
          }

          JavaAnnotation<?> annotation = javaClass.getAnnotationOfType(RESOURCE_ROLE);
          Object code = annotation.getProperties().get("code");
          if (code == null) {
            return;
          }

          String codeValue = code.toString();
          String packageName = javaClass.getPackageName();
          String[] segments = packageName.split("\\.");
          if (segments.length < 3) {
            return;
          }

          String module = segments[2];
          if (packageName.contains(".core.security")) {
            requireRoleCodePrefix(javaClass, codeValue, module + "-core-", "Core", events::add);
          } else if (packageName.contains(".ui.security")) {
            requireRoleCodePrefix(javaClass, codeValue, module + "-ui-", "UI", events::add);
          }
        });
  }

  private static boolean hasUiPolicy(HasAnnotations<?> element) {
    return element.getAnnotations().stream()
        .map(annotation -> annotation.getRawType().getName())
        .anyMatch(
            typeName ->
                typeName.contains("ViewPolicy")
                    || typeName.contains("MenuPolicy")
                    || typeName.contains("SpecificPolicy"));
  }

  private static boolean hasEntityPolicy(HasAnnotations<?> element) {
    return element.getAnnotations().stream()
        .map(annotation -> annotation.getRawType().getName())
        .anyMatch(
            typeName ->
                typeName.contains("EntityPolicy") || typeName.contains("EntityAttributePolicy"));
  }

  private static void checkWildcards(
      JavaClass roleClass,
      HasAnnotations<?> element,
      java.util.function.Consumer<ConditionEvent> eventSink) {
    for (JavaAnnotation<?> annotation : element.getAnnotations()) {
      String typeName = annotation.getRawType().getName();
      if (typeName.contains("ViewPolicy")
          && isWildcard(annotation.getProperties().get("viewIds"))) {
        eventSink.accept(
            SimpleConditionEvent.violated(
                roleClass, String.format("Role %s uses wildcard viewIds", roleClass.getName())));
      }
      if (typeName.contains("MenuPolicy")
          && isWildcard(annotation.getProperties().get("menuIds"))) {
        eventSink.accept(
            SimpleConditionEvent.violated(
                roleClass, String.format("Role %s uses wildcard menuIds", roleClass.getName())));
      }
      if ((typeName.contains("EntityPolicy") || typeName.contains("EntityAttributePolicy"))
          && isWildcard(annotation.getProperties().get("entityName"))) {
        eventSink.accept(
            SimpleConditionEvent.violated(
                roleClass, String.format("Role %s uses wildcard entityName", roleClass.getName())));
      }
      if (typeName.contains("SpecificPolicy")
          && isWildcard(annotation.getProperties().get("resources"))) {
        eventSink.accept(
            SimpleConditionEvent.violated(
                roleClass, String.format("Role %s uses wildcard resources", roleClass.getName())));
      }
    }
  }

  private static boolean isWildcard(Object value) {
    if (value instanceof String stringValue) {
      return "*".equals(stringValue);
    }
    if (value instanceof String[] stringValues) {
      return java.util.Arrays.asList(stringValues).contains("*");
    }
    if (value instanceof List<?> values) {
      return values.stream().anyMatch(item -> item != null && "*".equals(item.toString()));
    }
    return false;
  }

  private static void requireRoleCodePrefix(
      JavaClass javaClass,
      String codeValue,
      String expectedPrefix,
      String roleLayer,
      java.util.function.Consumer<ConditionEvent> eventSink) {
    if (!codeValue.startsWith(expectedPrefix)) {
      eventSink.accept(
          SimpleConditionEvent.violated(
              javaClass,
              String.format(
                  "%s role %s code \"%s\" must start with \"%s\"",
                  roleLayer, javaClass.getName(), codeValue, expectedPrefix)));
    }
  }
}
