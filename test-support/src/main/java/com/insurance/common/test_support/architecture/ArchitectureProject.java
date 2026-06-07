package com.insurance.common.test_support.architecture;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ArchitectureProject {

  private static final List<String> DOMAIN_MODULES =
      List.of("account", "partner", "policy", "product", "quote");

  private static final List<String> ENTITY_BOUNDARY_MODULES =
      List.of("account", "partner", "policy", "product", "quote", "security");

  private static final List<String> DOMAIN_BUILD_MODULES = ENTITY_BOUNDARY_MODULES;

  private static final List<String> PROJECT_ROOT_MARKERS =
      List.of(
          "webapp", "quote", "partner", "policy", "account", "product", "security", "test-support");

  private static final Pattern PROJECT_ID_ASSIGNMENT_PATTERN =
      Pattern.compile("(?m)^\\s*projectId\\s*=\\s*(.+)$");

  private static final Pattern DOMAIN_PROJECT_ID_ASSIGNMENT_PATTERN =
      Pattern.compile("(?m)^\\s*ext\\.jmixDomainProjectId\\s*=\\s*['\"]([^'\"]+)['\"]");

  private static final Pattern QUOTED_GRADLE_VALUE_PATTERN = Pattern.compile("['\"]([^'\"]+)['\"]");

  private ArchitectureProject() {}

  public static List<String> domainModules() {
    return DOMAIN_MODULES;
  }

  public static List<String> entityBoundaryModules() {
    return ENTITY_BOUNDARY_MODULES;
  }

  public static List<String> domainBuildModules() {
    return DOMAIN_BUILD_MODULES;
  }

  public static Path projectRoot() {
    Path userDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    Path fileName = userDir.getFileName();
    if (fileName != null && PROJECT_ROOT_MARKERS.contains(fileName.toString())) {
      Path parent = userDir.getParent();
      if (parent == null) {
        throw new IllegalStateException("Cannot determine project root from " + userDir);
      }
      return parent;
    }
    return userDir;
  }

  public static Path moduleRoot(String module) {
    return projectRoot().resolve(module);
  }

  public static Path layerRoot(String module, String layer) {
    return moduleRoot(module).resolve(module + "-" + layer);
  }

  public static String anyInsuranceApiPackage() {
    return "com.insurance..api..";
  }

  public static String[] domainApiPackages() {
    return ENTITY_BOUNDARY_MODULES.stream()
        .map(module -> moduleLayerPackage(module, "api"))
        .toArray(String[]::new);
  }

  public static String anyInsuranceCorePackage() {
    return "com.insurance..core..";
  }

  public static String anyInsuranceUiPackage() {
    return "com.insurance..ui..";
  }

  public static String anyFlowUiPackage() {
    return "io.jmix.flowui..";
  }

  public static String productPackage() {
    return "com.insurance.product..";
  }

  public static String partnerPackage() {
    return "com.insurance.partner..";
  }

  public static String corePackageOf(String module) {
    return moduleLayerPackage(module, "core");
  }

  public static String uiPackageOf(String module) {
    return moduleLayerPackage(module, "ui");
  }

  public static String uiApiPackageOf(String module) {
    return "com.insurance." + module + ".ui.api..";
  }

  public static String[] domainUiApiPackages() {
    return DOMAIN_MODULES.stream().map(ArchitectureProject::uiApiPackageOf).toArray(String[]::new);
  }

  public static boolean isDomainCorePackage(String packageName) {
    return DOMAIN_MODULES.stream()
        .anyMatch(module -> isPackage(packageName, corePackagePrefix(module)));
  }

  public static boolean isDomainUiImplementationPackage(String packageName) {
    return DOMAIN_MODULES.stream()
        .anyMatch(module -> isDomainUiImplementationPackage(packageName, module));
  }

  public static boolean isDomainUiImplementationPackage(String packageName, String module) {
    return isPackage(packageName, uiPackagePrefix(module))
        && !isPackage(packageName, uiApiPackagePrefix(module));
  }

  public static String[] foreignCoreOrUiPackagesOf(String module) {
    return foreignLayerPackagesOf(module, "core", "ui");
  }

  public static String[] foreignCorePackagesOf(String module) {
    return foreignLayerPackagesOf(module, "core");
  }

  public static String[] foreignLayerPackagesOf(String module, String... layers) {
    return DOMAIN_MODULES.stream()
        .filter(otherModule -> !otherModule.equals(module))
        .flatMap(
            otherModule -> Stream.of(layers).map(layer -> moduleLayerPackage(otherModule, layer)))
        .toArray(String[]::new);
  }

  public static String moduleLayerPackage(String module, String layer) {
    return "com.insurance." + module + "." + layer + "..";
  }

  private static String corePackagePrefix(String module) {
    return "com.insurance." + module + ".core";
  }

  private static String uiPackagePrefix(String module) {
    return "com.insurance." + module + ".ui";
  }

  private static String uiApiPackagePrefix(String module) {
    return "com.insurance." + module + ".ui.api";
  }

  private static boolean isPackage(String packageName, String packagePrefix) {
    return packageName.equals(packagePrefix) || packageName.startsWith(packagePrefix + ".");
  }

  public static String coreEntityPrefix(String module) {
    Path buildFile = moduleRoot(module).resolve("build.gradle");
    String buildFileContent = ArchitectureFiles.read(buildFile);

    Matcher domainProjectIdMatcher = DOMAIN_PROJECT_ID_ASSIGNMENT_PATTERN.matcher(buildFileContent);
    if (domainProjectIdMatcher.find()) {
      return domainProjectIdMatcher.group(1);
    }

    Matcher projectIdMatcher = PROJECT_ID_ASSIGNMENT_PATTERN.matcher(buildFileContent);
    if (projectIdMatcher.find()) {
      List<String> quotedValues =
          QUOTED_GRADLE_VALUE_PATTERN
              .matcher(projectIdMatcher.group(1))
              .results()
              .map(matchResult -> matchResult.group(1))
              .toList();
      if (!quotedValues.isEmpty()) {
        return quotedValues.get(quotedValues.size() - 1);
      }
    }

    throw new IllegalStateException("Cannot determine Jmix projectId for " + module);
  }

  public static List<Path> productionJavaAndXmlFiles(String module) {
    List<Path> files = new ArrayList<>();

    for (String layer : List.of("core", "ui")) {
      Path moduleLayerRoot = layerRoot(module, layer);
      files.addAll(
          ArchitectureFiles.javaAndXmlFiles(
              moduleLayerRoot.resolve(Path.of("src", "main", "java"))));
      files.addAll(
          ArchitectureFiles.javaAndXmlFiles(
              moduleLayerRoot.resolve(Path.of("src", "main", "resources"))));
    }

    return files;
  }
}
