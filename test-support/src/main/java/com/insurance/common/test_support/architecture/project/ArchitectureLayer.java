package com.insurance.common.test_support.architecture.project;

import java.util.Arrays;
import java.util.List;

public enum ArchitectureLayer {
  API("api", true, false, false),
  CORE("core", true, true, false),
  UI("ui", true, true, true),
  UI_STARTER("ui-starter", false, false, true);

  private final String id;
  private final boolean domain;
  private final boolean productionCode;
  private final boolean uiArtifact;

  ArchitectureLayer(String id, boolean domain, boolean productionCode, boolean uiArtifact) {
    this.id = id;
    this.domain = domain;
    this.productionCode = productionCode;
    this.uiArtifact = uiArtifact;
  }

  public String id() {
    return id;
  }

  public boolean isDomain() {
    return domain;
  }

  public boolean isProductionCode() {
    return productionCode;
  }

  public boolean isUiArtifact() {
    return uiArtifact;
  }

  public String anyInsurancePackage() {
    return ArchitectureProject.BASE_PACKAGE + ".." + id + "..";
  }

  public String anyInsuranceSubpackage(String subpackagePattern) {
    return ArchitectureProject.BASE_PACKAGE + ".." + id + "." + subpackagePattern;
  }

  public String anySlicePackage() {
    return ArchitectureProject.BASE_PACKAGE + ".*." + id;
  }

  public String anySliceSubpackage(String subpackagePattern) {
    return anySlicePackage() + "." + subpackagePattern;
  }

  public static List<ArchitectureLayer> domainLayers() {
    return Arrays.stream(values()).filter(ArchitectureLayer::isDomain).toList();
  }

  public static List<ArchitectureLayer> productionLayers() {
    return Arrays.stream(values()).filter(ArchitectureLayer::isProductionCode).toList();
  }
}
