package com.insurance.common.test_support.architecture.rules.ui;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.insurance.common.test_support.architecture.project.ArchitectureLayer;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.nio.file.Path;

/**
 * Enforces guardrails for UI Section Contributions to ensure cross-module UI composition is
 * deterministic, ordered, and adheres to module boundaries.
 */
public class UiSectionContributionRules {

  private static final String VIEW_SECTION_CLASS_NAME = "com.insurance.ui.section.ViewSection";

  @ArchTest
  public static final ArchRule sectionInterfacesLiveInUiApiModules =
      classes()
          .that(areAssignableToViewSection())
          .and()
          .areInterfaces()
          .and(notViewSectionInterface())
          .should()
          .resideInAPackage(
              ArchitectureLayer.UI.anyInsuranceSubpackage(ArchitectureLayer.API.id() + ".."))
          .as("concrete section interfaces should live in UI API modules");

  @ArchTest
  public static final ArchRule sectionImplementationsAreOrderedSpringBeans =
      classes()
          .that(sectionImplementations())
          .should(beSpringComponents())
          .andShould(beOrdered())
          .andShould(haveValidMessageKeys())
          .as("section implementations should be ordered Spring beans");

  @ArchTest
  public static final ArchRule sectionImplementationsOnlyUseHostUiContracts =
      classes()
          .that(sectionImplementations())
          .should(notDependOnHostUiImplementations())
          .as("section implementations should only depend on host UI contracts");

  private static DescribedPredicate<JavaClass> areAssignableToViewSection() {
    return DescribedPredicate.describe(
        "assignable to ViewSection",
        javaClass -> javaClass.isAssignableTo(VIEW_SECTION_CLASS_NAME));
  }

  private static DescribedPredicate<JavaClass> notViewSectionInterface() {
    return DescribedPredicate.describe(
        "not ViewSection interface",
        javaClass -> !javaClass.getName().equals(VIEW_SECTION_CLASS_NAME));
  }

  public static DescribedPredicate<JavaClass> sectionImplementations() {
    return DescribedPredicate.describe(
        "section implementations",
        javaClass ->
            javaClass.isAssignableTo(VIEW_SECTION_CLASS_NAME)
                && !javaClass.isInterface()
                && !javaClass
                    .getModifiers()
                    .contains(com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT));
  }

  public static ArchCondition<JavaClass> beSpringComponents() {
    return ArchitectureConditions.checkCondition(
        "be Spring components",
        (javaClass, events) -> {
          boolean isComponent =
              javaClass.isAnnotatedWith(org.springframework.stereotype.Component.class);
          if (!isComponent) {
            String message =
                String.format("Class %s is not annotated with @Component", javaClass.getName());
            events.add(SimpleConditionEvent.violated(javaClass, message));
          }
        });
  }

  public static ArchCondition<JavaClass> beOrdered() {
    return ArchitectureConditions.checkCondition(
        "be ordered",
        (javaClass, events) -> {
          boolean hasOrder =
              javaClass.isAnnotatedWith(org.springframework.core.annotation.Order.class);
          if (!hasOrder) {
            String message =
                String.format("Class %s is not annotated with @Order", javaClass.getName());
            events.add(SimpleConditionEvent.violated(javaClass, message));
          }
        });
  }

  public static ArchCondition<JavaClass> notDependOnHostUiImplementations() {
    return ArchitectureConditions.checkCondition(
        "not depend on host UI implementations",
        (javaClass, events) -> {
          for (JavaType type : javaClass.getInterfaces()) {
            JavaClass intf = type.toErasure();
            if (intf.isAssignableTo(VIEW_SECTION_CLASS_NAME)) {
              java.util.regex.Matcher matcher =
                  java.util.regex.Pattern.compile(
                          "^"
                              + java.util.regex.Pattern.quote(ArchitectureProject.basePackage())
                              + "\\.([^.]+)\\.ui\\.api(\\..*)?$")
                      .matcher(intf.getPackageName());
              if (matcher.find()) {
                ArchitectureSlice hostSlice =
                    ArchitectureSlice.fromId(matcher.group(1))
                        .orElseThrow(
                            () ->
                                new IllegalStateException(
                                    "Unknown architecture slice " + matcher.group(1)));
                String allowedPrefix = ArchitectureProject.uiApiPackagePrefixOf(hostSlice);
                String forbiddenPrefix =
                    ArchitectureProject.moduleLayerPackagePrefix(hostSlice, ArchitectureLayer.UI);

                for (com.tngtech.archunit.core.domain.Dependency dep :
                    javaClass.getDirectDependenciesFromSelf()) {
                  String targetPackage = dep.getTargetClass().getPackageName();
                  if (targetPackage.startsWith(forbiddenPrefix)
                      && !targetPackage.startsWith(allowedPrefix)) {
                    String message =
                        String.format(
                            "Class %s depends on host UI implementation %s (must only use UI API contract)",
                            javaClass.getName(), dep.getTargetClass().getName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                  }
                }
              }
            }
          }
        });
  }

  public static ArchCondition<JavaClass> haveValidMessageKeys() {
    return ArchitectureConditions.checkCondition(
        "have valid message keys",
        (javaClass, events) -> {
          Path sourceFile = findSourceFile(javaClass);
          if (sourceFile == null) {
            return;
          }
          String key = extractTitleMessageKey(sourceFile);
          if (key == null) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "Class %s does not return a literal string from titleMessageKey()",
                        javaClass.getName())));
            return;
          }
          Path propsFile = findMessagesProperties(javaClass);
          if (propsFile == null) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "Could not find messages.properties for class %s", javaClass.getName())));
            return;
          }
          String fullKey = javaClass.getPackageName() + "/" + key;
          if (!hasMessageKey(propsFile, fullKey)) {
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format(
                        "Message key %s not found in %s for class %s",
                        fullKey, propsFile.getFileName(), javaClass.getName())));
          }
        });
  }

  private static Path findSourceFile(JavaClass javaClass) {
    String classPath = javaClass.getName().replace('.', '/') + ".java";
    java.util.List<String> modules = ArchitectureProject.projectRootMarkers();
    for (String module : modules) {
      Path path1 =
          ArchitectureProject.projectRoot()
              .resolve(module)
              .resolve(module + "-ui/src/main/java")
              .resolve(classPath);
      if (java.nio.file.Files.exists(path1)) return path1;
      Path path2 =
          ArchitectureProject.projectRoot()
              .resolve(module)
              .resolve(module + "-core/src/main/java")
              .resolve(classPath);
      if (java.nio.file.Files.exists(path2)) return path2;
      Path path3 =
          ArchitectureProject.projectRoot()
              .resolve(module)
              .resolve(module + "-api/src/main/java")
              .resolve(classPath);
      if (java.nio.file.Files.exists(path3)) return path3;
      Path path4 =
          ArchitectureProject.projectRoot()
              .resolve(module)
              .resolve("src/main/java")
              .resolve(classPath);
      if (java.nio.file.Files.exists(path4)) return path4;
    }
    return null;
  }

  private static String extractTitleMessageKey(Path sourceFile) {
    try {
      String content = java.nio.file.Files.readString(sourceFile);
      java.util.regex.Matcher matcher =
          java.util.regex.Pattern.compile(
                  "public\\s+String\\s+titleMessageKey\\(\\)[\\s\\S]*?return\\s+\"([^\"]+)\"\\s*;")
              .matcher(content);
      if (matcher.find()) {
        return matcher.group(1);
      }
    } catch (Exception e) {
      // Ignore
    }
    return null;
  }

  private static Path findMessagesProperties(JavaClass javaClass) {
    String packageName = javaClass.getPackageName();
    java.util.regex.Matcher matcher =
        java.util.regex.Pattern.compile(
                "^"
                    + java.util.regex.Pattern.quote(ArchitectureProject.basePackage())
                    + "\\.([^.]+)\\.ui")
            .matcher(packageName);
    if (matcher.find()) {
      String module = matcher.group(1);
      String resourcePath =
          ArchitectureProject.basePackage().replace('.', '/')
              + "/"
              + module
              + "/ui/messages.properties";
      Path path =
          ArchitectureProject.projectRoot()
              .resolve(module)
              .resolve(module + "-ui/src/main/resources")
              .resolve(resourcePath);
      if (java.nio.file.Files.exists(path)) {
        return path;
      }
    }
    return null;
  }

  private static boolean hasMessageKey(Path propertiesFile, String fullKey) {
    try {
      java.util.Properties props = new java.util.Properties();
      try (java.io.Reader reader =
          java.nio.file.Files.newBufferedReader(
              propertiesFile, java.nio.charset.StandardCharsets.UTF_8)) {
        props.load(reader);
      }
      return props.containsKey(fullKey);
    } catch (Exception e) {
      return false;
    }
  }
}
