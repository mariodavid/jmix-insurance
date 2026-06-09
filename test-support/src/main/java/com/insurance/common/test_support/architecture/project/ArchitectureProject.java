package com.insurance.common.test_support.architecture.project;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ArchitectureProject {

  public static final String BASE_PACKAGE = "com.insurance";
  private static final String TEST_SUPPORT_PACKAGE = BASE_PACKAGE + ".common.test_support";

  private static final List<String> PROJECT_ROOT_MARKERS =
      Stream.concat(
              Arrays.stream(ArchitectureSlice.values()).map(ArchitectureSlice::directoryName),
              Stream.of("webapp", "test-support"))
          .toList();

  private static final Pattern PROJECT_ID_ASSIGNMENT_PATTERN =
      Pattern.compile("(?m)^\\s*projectId\\s*=\\s*(.+)$");

  private static final Pattern DOMAIN_PROJECT_ID_ASSIGNMENT_PATTERN =
      Pattern.compile("(?m)^\\s*ext\\.jmixDomainProjectId\\s*=\\s*['\"]([^'\"]+)['\"]");

  private static final Pattern QUOTED_GRADLE_VALUE_PATTERN = Pattern.compile("['\"]([^'\"]+)['\"]");

  private ArchitectureProject() {}

  public static String basePackage() {
    return BASE_PACKAGE;
  }

  public static String appPackage() {
    return BASE_PACKAGE + ".app..";
  }

  public static String appSubpackage(String subpackagePattern) {
    return BASE_PACKAGE + ".app." + subpackagePattern;
  }

  public static String testSupportPackage() {
    return TEST_SUPPORT_PACKAGE;
  }

  public static String testSupportPackagePattern() {
    return TEST_SUPPORT_PACKAGE + "..";
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

  public static List<String> projectRootMarkers() {
    return PROJECT_ROOT_MARKERS;
  }

  public static Path moduleRoot(ArchitectureSlice slice) {
    return projectRoot().resolve(slice.directoryName());
  }

  public static Path layerRoot(ArchitectureSlice slice, ArchitectureLayer layer) {
    return moduleRoot(slice).resolve(slice.directoryName() + "-" + layer.id());
  }

  public static Path coreLayerRoot(ArchitectureSlice slice) {
    return layerRoot(slice, ArchitectureLayer.CORE);
  }

  public static String anyInsuranceApiPackage() {
    return anyLayerPackage(ArchitectureLayer.API);
  }

  public static String[] domainApiPackages() {
    return ArchitectureSlice.entityBoundarySlices().stream()
        .map(slice -> moduleLayerPackage(slice, ArchitectureLayer.API))
        .toArray(String[]::new);
  }

  public static String[] domainLayerPackages() {
    return ArchitectureSlice.entityBoundarySlices().stream()
        .flatMap(
            slice ->
                ArchitectureLayer.domainLayers().stream()
                    .map(layer -> moduleLayerPackage(slice, layer)))
        .toArray(String[]::new);
  }

  public static String anyInsuranceCorePackage() {
    return anyLayerPackage(ArchitectureLayer.CORE);
  }

  public static String anyInsuranceUiPackage() {
    return anyLayerPackage(ArchitectureLayer.UI);
  }

  public static String anyFlowUiPackage() {
    return "io.jmix.flowui..";
  }

  public static String entityPackagePrefixOf(ArchitectureSlice slice) {
    return slice.entityPackagePrefix();
  }

  public static boolean isEntityPackage(String packageName, ArchitectureSlice slice) {
    return slice.isEntityPackage(packageName);
  }

  public static String uiViewPackageOf(ArchitectureSlice slice) {
    return slice.uiViewPackage();
  }

  public static String[] allUiViewPackages() {
    return Stream.concat(
            ArchitectureSlice.uiViewSlices().stream().map(ArchitectureProject::uiViewPackageOf),
            Stream.of(appSubpackage("ui.view..")))
        .toArray(String[]::new);
  }

  public static String uiApiPackageOf(ArchitectureSlice slice) {
    return slice.uiApiPackage();
  }

  public static String[] domainUiApiPackages() {
    return ArchitectureSlice.domainSlices().stream()
        .map(ArchitectureProject::uiApiPackageOf)
        .toArray(String[]::new);
  }

  public static boolean isDomainCorePackage(String packageName) {
    return ArchitectureSlice.domainSlices().stream()
        .anyMatch(slice -> isPackage(packageName, slice.corePackagePrefix()));
  }

  public static boolean isDomainCorePackage(String packageName, ArchitectureSlice slice) {
    return isPackage(packageName, slice.corePackagePrefix());
  }

  public static boolean isDomainUiPackage(String packageName, ArchitectureSlice slice) {
    return isPackage(packageName, slice.uiPackagePrefix());
  }

  public static boolean isDomainUiImplementationPackage(String packageName) {
    return ArchitectureSlice.domainSlices().stream()
        .anyMatch(slice -> isDomainUiImplementationPackage(packageName, slice));
  }

  public static boolean isDomainUiImplementationPackage(
      String packageName, ArchitectureSlice slice) {
    return isPackage(packageName, slice.uiPackagePrefix())
        && !isPackage(packageName, slice.uiApiPackagePrefix());
  }

  public static String moduleLayerPackage(ArchitectureSlice slice, ArchitectureLayer layer) {
    return slice.moduleLayerPackage(layer);
  }

  public static String moduleLayerPackage(
      ArchitectureSlice slice, ArchitectureLayer layer, String subpackagePattern) {
    return slice.moduleLayerPackage(layer, subpackagePattern);
  }

  public static String moduleLayerPackagePrefix(ArchitectureSlice slice, ArchitectureLayer layer) {
    return slice.moduleLayerPackagePrefix(layer);
  }

  public static String anyLayerPackage(ArchitectureLayer layer) {
    return layer.anyInsurancePackage();
  }

  public static String anySliceLayerPackage(ArchitectureLayer layer) {
    return layer.anySlicePackage();
  }

  public static String anySliceLayerSubpackage(ArchitectureLayer layer, String subpackagePattern) {
    return layer.anySliceSubpackage(subpackagePattern);
  }

  public static String uiApiPackagePrefixOf(ArchitectureSlice slice) {
    return slice.uiApiPackagePrefix();
  }

  static boolean isPackage(String packageName, String packagePrefix) {
    return packageName.equals(packagePrefix) || packageName.startsWith(packagePrefix + ".");
  }

  public static String coreEntityPrefix(ArchitectureSlice slice) {
    Path buildFile = moduleRoot(slice).resolve("build.gradle");
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
        return quotedValues.getLast();
      }
    }

    throw new IllegalStateException("Cannot determine Jmix projectId for " + slice.id());
  }

  public static List<Path> productionJavaAndXmlFiles(ArchitectureSlice slice) {
    return ArchitectureLayer.productionLayers().stream()
        .map(layer -> layerRoot(slice, layer))
        .flatMap(ArchitectureProject::productionJavaAndXmlFilesInLayer)
        .toList();
  }

  static Stream<Path> productionJavaAndXmlFilesInLayer(Path layerRoot) {
    return Stream.of(
            layerRoot.resolve(Path.of("src", "main", "java")),
            layerRoot.resolve(Path.of("src", "main", "resources")))
        .flatMap(root -> ArchitectureFiles.javaAndXmlFiles(root).stream());
  }
}
