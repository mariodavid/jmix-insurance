package com.insurance.common.test_support.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.ArchTest;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JmixDomainBuildRules {

  private static final String CONVENTION_PATH = "../gradle/jmix-domain-conventions.gradle";

  @ArchTest
  public static void domainBuildsUseSharedJmixConvention(JavaClasses classes) {
    List<String> violations = new ArrayList<>();

    for (String module : ArchitectureProject.domainBuildModules()) {
      Path buildFile = ArchitectureProject.moduleRoot(module).resolve("build.gradle");
      String content = ArchitectureFiles.read(buildFile);

      if (!content.contains("ext.jmixDomainProjectId = '" + module + "'")) {
        violations.add(
            relative(buildFile) + " must declare ext.jmixDomainProjectId = '" + module + "'");
      }
      if (!content.contains(CONVENTION_PATH)) {
        violations.add(relative(buildFile) + " must apply " + CONVENTION_PATH);
      }
      if (content.contains("subprojects {")) {
        violations.add(relative(buildFile) + " must not copy the shared subprojects convention");
      }
      if (content.contains("static-analysis.gradle")
          || content.contains("liquibase.duplicateFileMode")
          || content.contains("jacocoTestReport")) {
        violations.add(
            relative(buildFile) + " must keep quality configuration in the convention script");
      }
    }

    assertThat(violations)
        .as("Domain build roots must delegate shared Jmix build rules to the convention script")
        .isEmpty();
  }

  private static String relative(Path path) {
    return ArchitectureProject.projectRoot()
        .relativize(path.toAbsolutePath().normalize())
        .toString();
  }
}
