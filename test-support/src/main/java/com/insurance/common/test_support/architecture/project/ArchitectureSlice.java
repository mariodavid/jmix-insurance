package com.insurance.common.test_support.architecture.project;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum ArchitectureSlice {
  ACCOUNT("account", true, true, true),
  PARTNER("partner", true, true, true),
  POLICY("policy", true, true, true),
  PRODUCT("product", true, true, true),
  QUOTE("quote", true, true, true),
  SECURITY("security", false, true, true);

  private final String id;
  private final String directoryName;
  private final boolean domain;
  private final boolean entityBoundary;
  private final boolean uiView;

  ArchitectureSlice(String id, boolean domain, boolean entityBoundary, boolean uiView) {
    this(id, id, domain, entityBoundary, uiView);
  }

  ArchitectureSlice(
      String id, String directoryName, boolean domain, boolean entityBoundary, boolean uiView) {
    this.id = id;
    this.directoryName = directoryName;
    this.domain = domain;
    this.entityBoundary = entityBoundary;
    this.uiView = uiView;
  }

  public String id() {
    return id;
  }

  public String directoryName() {
    return directoryName;
  }

  public boolean isDomain() {
    return domain;
  }

  public boolean isEntityBoundary() {
    return entityBoundary;
  }

  public boolean hasUiViews() {
    return uiView;
  }

  public static Optional<ArchitectureSlice> fromId(String id) {
    return Arrays.stream(values()).filter(slice -> slice.id.equals(id)).findFirst();
  }

  public static List<ArchitectureSlice> domainSlices() {
    return Arrays.stream(values()).filter(ArchitectureSlice::isDomain).toList();
  }

  public static List<ArchitectureSlice> entityBoundarySlices() {
    return Arrays.stream(values()).filter(ArchitectureSlice::isEntityBoundary).toList();
  }

  public static List<ArchitectureSlice> uiViewSlices() {
    return Arrays.stream(values()).filter(ArchitectureSlice::hasUiViews).toList();
  }

  public String packagePattern() {
    return packagePrefix() + "..";
  }

  public String packagePrefix() {
    return ArchitectureProject.BASE_PACKAGE + "." + id;
  }

  public String moduleLayerPackagePrefix(ArchitectureLayer layer) {
    return packagePrefix() + "." + layer.id();
  }

  public String moduleLayerPackage(ArchitectureLayer layer) {
    return moduleLayerPackagePrefix(layer) + "..";
  }

  public String moduleLayerPackage(ArchitectureLayer layer, String subpackagePattern) {
    return moduleLayerPackagePrefix(layer) + "." + subpackagePattern;
  }

  public String entityPackagePrefix() {
    return moduleLayerPackagePrefix(ArchitectureLayer.CORE) + ".entity";
  }

  public boolean isEntityPackage(String packageName) {
    return ArchitectureProject.isPackage(packageName, entityPackagePrefix());
  }

  public String uiViewPackage() {
    return moduleLayerPackage(ArchitectureLayer.UI, "view..");
  }

  public String uiApiPackagePrefix() {
    return moduleLayerPackagePrefix(ArchitectureLayer.UI) + "." + ArchitectureLayer.API.id();
  }

  public String uiApiPackage() {
    return uiApiPackagePrefix() + "..";
  }

  public String corePackagePrefix() {
    return moduleLayerPackagePrefix(ArchitectureLayer.CORE);
  }

  public String uiPackagePrefix() {
    return moduleLayerPackagePrefix(ArchitectureLayer.UI);
  }
}
