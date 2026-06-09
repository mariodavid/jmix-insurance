package com.insurance.common.test_support.architecture.scan;

import com.insurance.common.test_support.architecture.project.ArchitectureFiles;
import com.insurance.common.test_support.architecture.project.ArchitectureLayer;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class SecurityPolicyIndex {

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

  private SecurityPolicyIndex() {}

  public static List<SecurityPolicyViolation> unknownViewPolicyIds() {
    Set<String> viewIds = viewControllerIds();
    return productionJavaFiles().stream()
        .flatMap(path -> unknownViewPolicyIdsIn(path, viewIds))
        .map(SecurityPolicyViolation::new)
        .toList();
  }

  public static List<SecurityPolicyViolation> unknownMenuPolicyIds() {
    Set<String> menuItemIds = menuItemIds();
    return productionJavaFiles().stream()
        .flatMap(path -> unknownMenuPolicyIdsIn(path, menuItemIds))
        .map(SecurityPolicyViolation::new)
        .toList();
  }

  public static List<SecurityPolicyViolation> userFacingViewIdsWithoutConcreteViewPolicy() {
    Set<String> concreteViewPolicies = concreteViewPolicyIds();
    return viewControllerIds().stream()
        .filter(viewId -> !concreteViewPolicies.contains(viewId))
        .map(viewId -> viewId + " has no concrete @ViewPolicy")
        .map(SecurityPolicyViolation::new)
        .toList();
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

  private static Stream<String> unknownViewPolicyIdsIn(Path path, Set<String> viewIds) {
    return VIEW_POLICY_PATTERN
        .matcher(ArchitectureFiles.read(path))
        .results()
        .flatMap(matchResult -> stringLiterals(matchResult.group(1)).stream())
        .filter(viewId -> !"*".equals(viewId))
        .filter(viewId -> !viewIds.contains(viewId))
        .map(viewId -> relative(path) + " references unknown @ViewPolicy view id " + viewId);
  }

  private static Stream<String> unknownMenuPolicyIdsIn(Path path, Set<String> menuItemIds) {
    return MENU_POLICY_PATTERN
        .matcher(ArchitectureFiles.read(path))
        .results()
        .flatMap(matchResult -> stringLiterals(matchResult.group(1)).stream())
        .filter(menuId -> !"*".equals(menuId))
        .filter(menuId -> !menuItemIds.contains(menuId))
        .map(menuId -> relative(path) + " references unknown @MenuPolicy menu id " + menuId);
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
    return Stream.concat(
            ArchitectureSlice.entityBoundarySlices().stream()
                .flatMap(
                    slice ->
                        ArchitectureLayer.productionLayers().stream()
                            .map(layer -> ArchitectureProject.layerRoot(slice, layer))),
            Stream.of(ArchitectureProject.projectRoot().resolve("webapp")))
        .toList();
  }

  private static String relative(Path path) {
    return ArchitectureProject.projectRoot()
        .relativize(path.toAbsolutePath().normalize())
        .toString();
  }

  public record SecurityPolicyViolation(String message) {

    @Override
    public String toString() {
      return message;
    }
  }
}
