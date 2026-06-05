package com.insurance.app.arch;

import com.insurance.common.test_support.ArchitectureRules;
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

  @ArchTest static final ArchTests sharedRules = ArchTests.in(ArchitectureRules.class);

  @Nested
  @DisplayName("Module Dependency Rules (File System Checks)")
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
      ArchitectureRules.assertCoreModuleDoesNotDeclareFlowUi(
          ArchitectureRules.projectRoot().resolve(modulePath));
    }
  }
}
