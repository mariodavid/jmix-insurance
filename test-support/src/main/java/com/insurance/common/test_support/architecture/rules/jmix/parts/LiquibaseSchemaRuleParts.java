package com.insurance.common.test_support.architecture.rules.jmix.parts;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.insurance.common.test_support.architecture.scan.LiquibaseSchemaDrift;
import com.insurance.common.test_support.architecture.scan.LiquibaseSchemaDrift.RequiredUniqueColumn;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public final class LiquibaseSchemaRuleParts {

  private LiquibaseSchemaRuleParts() {}

  public static ClassesTransformer<RequiredUniqueColumn> requiredUniqueEntityColumns() {
    return ArchitectureConditions.transformClasses(
        "required unique entity columns", classes -> LiquibaseSchemaDrift.requiredUniqueColumns());
  }

  public static ArchCondition<RequiredUniqueColumn> haveLiquibaseUniqueConstraints() {
    return ArchitectureConditions.checkCondition(
        "have Liquibase unique constraints",
        (column, events) -> {
          if (!column.liquibaseUniqueConstraintDeclared()) {
            events.add(
                SimpleConditionEvent.violated(column, column.missingConstraintDescription()));
          }
        });
  }
}
