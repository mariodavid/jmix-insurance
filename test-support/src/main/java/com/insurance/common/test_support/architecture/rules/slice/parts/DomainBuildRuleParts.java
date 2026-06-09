package com.insurance.common.test_support.architecture.rules.slice.parts;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.insurance.common.test_support.architecture.scan.DomainBuildConventions;
import com.insurance.common.test_support.architecture.scan.DomainBuildConventions.DomainBuildFile;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public final class DomainBuildRuleParts {

  private DomainBuildRuleParts() {}

  public static ClassesTransformer<DomainBuildFile> domainBuildFiles() {
    return ArchitectureConditions.transformClasses(
        "domain build files", classes -> DomainBuildConventions.domainBuildFiles());
  }

  public static ArchCondition<DomainBuildFile> useSharedDomainBuildConvention() {
    return ArchitectureConditions.checkCondition(
        "use the shared domain build convention",
        (buildFile, events) ->
            buildFile
                .conventionViolations()
                .forEach(
                    violation ->
                        events.add(
                            SimpleConditionEvent.violated(buildFile, violation.toString()))));
  }
}
