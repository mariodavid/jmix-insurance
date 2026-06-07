package com.insurance.app.arch;

import com.insurance.common.test_support.architecture.ArchitectureProject;
import com.insurance.common.test_support.architecture.CoreModuleFileRules;
import com.insurance.common.test_support.architecture.JavaPackageDependencyRules;
import com.insurance.common.test_support.architecture.JmixDomainBoundaryRules;
import com.insurance.common.test_support.architecture.JmixDomainBuildRules;
import com.insurance.common.test_support.architecture.JmixDomainReferenceRules;
import com.insurance.common.test_support.architecture.JmixEntityRules;
import com.insurance.common.test_support.architecture.JmixLiquibaseRules;
import com.insurance.common.test_support.architecture.JmixSecurityRoleRules;
import com.insurance.common.test_support.architecture.JmixUiDependencyRules;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Global architecture rules")
@AnalyzeClasses(packages = "com.insurance")
class ArchitectureTest {

  @ArchTest
  static final ArchTests javaPackageDependencyRules =
      ArchTests.in(JavaPackageDependencyRules.class);

  @ArchTest static final ArchTests jmixEntityRules = ArchTests.in(JmixEntityRules.class);

  @ArchTest
  static final ArchTests jmixDomainBoundaryRules = ArchTests.in(JmixDomainBoundaryRules.class);

  @ArchTest static final ArchTests jmixDomainBuildRules = ArchTests.in(JmixDomainBuildRules.class);

  @ArchTest
  static final ArchTests jmixDomainReferenceRules = ArchTests.in(JmixDomainReferenceRules.class);

  @ArchTest static final ArchTests jmixLiquibaseRules = ArchTests.in(JmixLiquibaseRules.class);

  @ArchTest
  static final ArchTests jmixUiDependencyRules = ArchTests.in(JmixUiDependencyRules.class);

  @ArchTest
  static final ArchTests jmixSecurityRoleRules = ArchTests.in(JmixSecurityRoleRules.class);

  @Nested
  @DisplayName("Core Module File Rules")
  class FileSystemChecks {

    @ParameterizedTest(name = "{0}")
    @ValueSource(
        strings = {
          "account/account-core",
          "partner/partner-core",
          "policy/policy-core",
          "product/product-core",
          "quote/quote-core",
          "security/security-core"
        })
    @DisplayName("Core modules do not declare Flow UI dependencies or view resources")
    void coreModuleDoesNotDeclareFlowUiDependenciesOrViews(String modulePath) {
      CoreModuleFileRules.assertCoreModuleDoesNotDeclareFlowUi(
          ArchitectureProject.projectRoot().resolve(modulePath));
    }
  }
}
