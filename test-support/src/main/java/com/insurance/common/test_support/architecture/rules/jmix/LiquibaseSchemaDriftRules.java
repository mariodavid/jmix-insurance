package com.insurance.common.test_support.architecture.rules.jmix;

import static com.insurance.common.test_support.architecture.rules.jmix.parts.LiquibaseSchemaRuleParts.haveLiquibaseUniqueConstraints;
import static com.insurance.common.test_support.architecture.rules.jmix.parts.LiquibaseSchemaRuleParts.requiredUniqueEntityColumns;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces persistence-model and Liquibase drift guardrails.
 *
 * <p>The boundary here is between Java entity metadata and database schema migrations. Required
 * unique business keys declared on persistent entities must also be represented in Liquibase, so
 * the database enforces the same invariant as the Jmix model.
 */
public class LiquibaseSchemaDriftRules {

  /**
   * Every required unique entity column must have a matching Liquibase unique constraint in the
   * owning module's changelog files.
   */
  @ArchTest
  public static final ArchRule requiredUniqueEntityColumnsHaveLiquibaseConstraints =
      all(requiredUniqueEntityColumns())
          .should(haveLiquibaseUniqueConstraints())
          .as("required unique entity columns should have Liquibase unique constraints")
          .because(
              "persistent business keys declared with @Column(unique = true, nullable = false) "
                  + "must be backed by a database constraint")
          .allowEmptyShould(true);
}
