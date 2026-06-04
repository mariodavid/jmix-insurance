package com.insurance.account.api;

import static com.insurance.account.api.dto.Assertions.assertThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.insurance.account.api.dto.AccountDto;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@AnalyzeClasses(
    packages = "com.insurance.account.api",
    importOptions = ImportOption.DoNotIncludeTests.class)
@SpringBootTest
class AccountTest {

  @ArchTest
  static final ArchRule accountApiDoesNotDependOnCoreUiOrFlowUi =
      noClasses()
          .that()
          .resideInAPackage("com.insurance.account.api..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.insurance.account.core..", "com.insurance..ui..", "io.jmix.flowui..");

  @Autowired private DataManager dataManager;

  @Autowired private Metadata metadata;

  @Test
  void contextLoads() {}

  @Test
  void given_accountDto_when_createdThroughDataManager_then_metadataAndInstanceNameAreStable() {
    AccountDto dto = dataManager.create(AccountDto.class);
    dto.setPolicyNo("HC-2025-000001");
    dto.setBalance(new BigDecimal("120.00"));

    MetaClass metaClass = metadata.getClass(AccountDto.class);

    assertThat(dto.getId()).isNotNull();
    assertThat(metaClass.getProperty("id")).isNotNull();
    assertThat(dto).hasPolicyNo("HC-2025-000001").hasBalance(new BigDecimal("120.00"));
    assertThat(dto.instanceName()).isEqualTo("HC-2025-000001");
  }
}
