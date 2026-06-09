package com.insurance.common.test_support.architecture.scan;

import com.insurance.common.test_support.architecture.project.ArchitectureFiles;
import com.insurance.common.test_support.architecture.project.ArchitectureProject;
import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class DomainBuildConventions {

  private static final String CONVENTION_PATH = "../gradle/jmix-domain-conventions.gradle";

  private DomainBuildConventions() {}

  public static List<DomainBuildFile> domainBuildFiles() {
    return ArchitectureSlice.entityBoundarySlices().stream()
        .map(
            slice ->
                new DomainBuildFile(
                    slice, ArchitectureProject.moduleRoot(slice).resolve("build.gradle")))
        .toList();
  }

  public record DomainBuildFile(ArchitectureSlice slice, Path path) {

    public List<DomainBuildConventionViolation> conventionViolations() {
      return Stream.of(
              violationUnless(
                  declaresDomainProjectId(),
                  "must declare ext.jmixDomainProjectId = '" + slice.id() + "'"),
              violationUnless(appliesSharedConventionScript(), "must apply " + CONVENTION_PATH),
              violationIf(
                  copiesSharedSubprojectsConvention(),
                  "must not copy the shared subprojects convention"),
              violationIf(
                  declaresLocalQualityConfiguration(),
                  "must keep quality configuration in the convention script"))
          .flatMap(Stream::ofNullable)
          .toList();
    }

    private DomainBuildConventionViolation violationIf(boolean condition, String message) {
      return condition ? new DomainBuildConventionViolation(this, message) : null;
    }

    private DomainBuildConventionViolation violationUnless(boolean condition, String message) {
      return violationIf(!condition, message);
    }

    private boolean declaresDomainProjectId() {
      return content().contains("ext.jmixDomainProjectId = '" + slice.id() + "'");
    }

    private boolean appliesSharedConventionScript() {
      return content().contains(CONVENTION_PATH);
    }

    private boolean copiesSharedSubprojectsConvention() {
      return content().contains("subprojects {");
    }

    private boolean declaresLocalQualityConfiguration() {
      return content().contains("static-analysis.gradle")
          || content().contains("liquibase.duplicateFileMode")
          || content().contains("jacocoTestReport");
    }

    private String content() {
      return ArchitectureFiles.read(path);
    }

    public String relativePath() {
      return ArchitectureProject.projectRoot()
          .relativize(path.toAbsolutePath().normalize())
          .toString();
    }
  }

  public record DomainBuildConventionViolation(DomainBuildFile buildFile, String message) {

    @Override
    public String toString() {
      return buildFile.relativePath() + " " + message;
    }
  }
}
