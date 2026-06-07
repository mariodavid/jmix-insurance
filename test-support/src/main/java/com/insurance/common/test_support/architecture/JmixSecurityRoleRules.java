package com.insurance.common.test_support.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.ArchTest;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class JmixSecurityRoleRules {

  private static final Pattern VIEW_CONTROLLER_PATTERN =
      Pattern.compile("@ViewController\\s*\\(\\s*id\\s*=\\s*\"([^\"]+)\"");

  private static final Pattern VIEW_POLICY_PATTERN =
      Pattern.compile("@ViewPolicy\\s*\\(([^)]*)\\)", Pattern.DOTALL);

  private static final Pattern MENU_POLICY_PATTERN =
      Pattern.compile("@MenuPolicy\\s*\\(([^)]*)\\)", Pattern.DOTALL);

  private static final Pattern XML_ITEM_PATTERN = Pattern.compile("<item\\b([^>]*)>");

  private static final Pattern XML_ATTRIBUTE_PATTERN =
      Pattern.compile("\\b(id|view)\\s*=\\s*\"([^\"]+)\"");

  private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("\"([^\"]+)\"");

  @ArchTest
  public static void viewPoliciesReferenceExistingViews(JavaClasses classes) {
    Set<String> viewIds = viewControllerIds();
    List<String> violations = new ArrayList<>();

    for (Path path : productionJavaFiles()) {
      String content = ArchitectureFiles.read(path);
      VIEW_POLICY_PATTERN
          .matcher(content)
          .results()
          .forEach(
              matchResult ->
                  stringLiterals(matchResult.group(1)).stream()
                      .filter(viewId -> !"*".equals(viewId))
                      .filter(viewId -> !viewIds.contains(viewId))
                      .forEach(
                          viewId ->
                              violations.add(
                                  relative(path)
                                      + " references unknown @ViewPolicy view id "
                                      + viewId)));
    }

    assertThat(violations).as("@ViewPolicy values must reference existing view ids").isEmpty();
  }

  @ArchTest
  public static void menuPoliciesReferenceExistingMenuItems(JavaClasses classes) {
    Set<String> menuItemIds = menuItemIds();
    List<String> violations = new ArrayList<>();

    for (Path path : productionJavaFiles()) {
      String content = ArchitectureFiles.read(path);
      MENU_POLICY_PATTERN
          .matcher(content)
          .results()
          .forEach(
              matchResult ->
                  stringLiterals(matchResult.group(1)).stream()
                      .filter(menuId -> !"*".equals(menuId))
                      .filter(menuId -> !menuItemIds.contains(menuId))
                      .forEach(
                          menuId ->
                              violations.add(
                                  relative(path)
                                      + " references unknown @MenuPolicy menu id "
                                      + menuId)));
    }

    assertThat(violations).as("@MenuPolicy values must reference concrete menu item ids").isEmpty();
  }

  @ArchTest
  public static void userFacingViewsHaveConcreteViewPolicies(JavaClasses classes) {
    Set<String> viewIds = viewControllerIds();
    Set<String> concreteViewPolicies = concreteViewPolicyIds();
    List<String> violations =
        viewIds.stream()
            .filter(viewId -> !concreteViewPolicies.contains(viewId))
            .map(viewId -> viewId + " has no concrete @ViewPolicy")
            .toList();

    assertThat(violations)
        .as("Every user-facing @ViewController id must have a concrete @ViewPolicy")
        .isEmpty();
  }

  private static Set<String> viewControllerIds() {
    Set<String> viewIds = new LinkedHashSet<>();
    for (Path path : productionJavaFiles()) {
      VIEW_CONTROLLER_PATTERN
          .matcher(ArchitectureFiles.read(path))
          .results()
          .forEach(matchResult -> viewIds.add(matchResult.group(1)));
    }
    return viewIds;
  }

  private static Set<String> concreteViewPolicyIds() {
    Set<String> viewIds = new LinkedHashSet<>();
    for (Path path : productionJavaFiles()) {
      VIEW_POLICY_PATTERN
          .matcher(ArchitectureFiles.read(path))
          .results()
          .forEach(
              matchResult ->
                  stringLiterals(matchResult.group(1)).stream()
                      .filter(viewId -> !"*".equals(viewId))
                      .forEach(viewIds::add));
    }
    return viewIds;
  }

  private static Set<String> menuItemIds() {
    Set<String> menuItemIds = new LinkedHashSet<>();
    for (Path path : productionXmlFiles()) {
      XML_ITEM_PATTERN
          .matcher(ArchitectureFiles.read(path))
          .results()
          .forEach(
              itemMatch ->
                  XML_ATTRIBUTE_PATTERN
                      .matcher(itemMatch.group(1))
                      .results()
                      .forEach(attributeMatch -> menuItemIds.add(attributeMatch.group(2))));
    }
    return menuItemIds;
  }

  private static List<String> stringLiterals(String content) {
    return STRING_LITERAL_PATTERN.matcher(content).results().map(match -> match.group(1)).toList();
  }

  private static List<Path> productionJavaFiles() {
    return productionRoots().stream()
        .flatMap(
            root ->
                ArchitectureFiles.walkIfExists(root.resolve(Path.of("src", "main", "java")))
                    .stream())
        .filter(path -> path.toString().endsWith(".java"))
        .toList();
  }

  private static List<Path> productionXmlFiles() {
    return productionRoots().stream()
        .flatMap(
            root ->
                ArchitectureFiles.walkIfExists(root.resolve(Path.of("src", "main", "resources")))
                    .stream())
        .filter(path -> path.toString().endsWith(".xml"))
        .toList();
  }

  private static List<Path> productionRoots() {
    List<Path> roots = new ArrayList<>();
    for (String module : ArchitectureProject.entityBoundaryModules()) {
      roots.add(ArchitectureProject.layerRoot(module, "core"));
      roots.add(ArchitectureProject.layerRoot(module, "ui"));
    }
    roots.add(ArchitectureProject.projectRoot().resolve("webapp"));
    return roots;
  }

  private static String relative(Path path) {
    return ArchitectureProject.projectRoot()
        .relativize(path.toAbsolutePath().normalize())
        .toString();
  }
}
