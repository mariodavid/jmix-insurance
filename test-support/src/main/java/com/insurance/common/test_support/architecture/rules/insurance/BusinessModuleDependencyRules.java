package com.insurance.common.test_support.architecture.rules.insurance;

import static com.insurance.common.test_support.architecture.rules.slice.parts.ArchitectureSliceRuleParts.resideInAllowedSlices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces business module-level dependency rules based on the Gradle modular monolith
 * architecture.
 *
 * <p>Each domain module is restricted to depending only on its allowed set of dependencies,
 * preventing architectural erosion and unintended couplings.
 */
public class BusinessModuleDependencyRules {

  /**
   * Product is a catalog and base insurance reference module containing definitions of products,
   * coverage, types, and variants. According to {@code product/product-core/product-core.gradle},
   * it does not declare any dependencies on other business modules, making it completely
   * independent. Therefore, classes in the product module must not depend on any classes from the
   * account, partner, policy, quote, or security modules.
   */
  @ArchTest
  public static final ArchRule productModuleIsIndependent =
      classes()
          .that()
          .resideInAPackage(ArchitectureSlice.PRODUCT.packagePattern())
          .should()
          .onlyDependOnClassesThat(resideInAllowedSlices(ArchitectureSlice.PRODUCT));

  /**
   * Partner holds customer, agency, and partner metadata. According to {@code
   * partner/partner-ui/partner-ui.gradle}, it depends on {@code policy-ui-api} to allow displaying
   * policy information within customer/partner screens. It has no core dependencies on other
   * business modules. Therefore, classes in the partner module must not depend on any classes from
   * the account, product, quote, or security modules.
   */
  @ArchTest
  public static final ArchRule partnerModuleDependenciesAreRestricted =
      classes()
          .that()
          .resideInAPackage(ArchitectureSlice.PARTNER.packagePattern())
          .should()
          .onlyDependOnClassesThat(
              resideInAllowedSlices(ArchitectureSlice.PARTNER, ArchitectureSlice.POLICY));

  /**
   * Security contains Jmix role configurations, resource policies, and user management structures.
   * According to its Gradle configurations, it has no compile/api dependencies on any business
   * domain modules. This ensures security settings are decoupled from specific domain concepts.
   * Therefore, classes in the security module must not depend on any classes from the account,
   * partner, policy, product, or quote modules.
   */
  @ArchTest
  public static final ArchRule securityModuleDoesNotDependOnBusinessModules =
      classes()
          .that()
          .resideInAPackage(ArchitectureSlice.SECURITY.packagePattern())
          .should()
          .onlyDependOnClassesThat(resideInAllowedSlices(ArchitectureSlice.SECURITY));

  /**
   * Account manages financial records, billing, and transactions. According to {@code
   * account/account-core/account-core.gradle} and {@code account/account-ui/account-ui.gradle}, it
   * depends on {@code policy-api-starter} and {@code policy-ui-api} to associate transactions with
   * contracts, {@code product-api-starter} to lookup product-specific pricing/billing
   * configurations, and {@code partner-ui-api} to associate accounts/transactions with partners. It
   * must not depend on quote or security modules.
   */
  @ArchTest
  public static final ArchRule accountModuleDependenciesAreRestricted =
      classes()
          .that()
          .resideInAPackage(ArchitectureSlice.ACCOUNT.packagePattern())
          .should()
          .onlyDependOnClassesThat(
              resideInAllowedSlices(
                  ArchitectureSlice.ACCOUNT,
                  ArchitectureSlice.POLICY,
                  ArchitectureSlice.PRODUCT,
                  ArchitectureSlice.PARTNER));

  /**
   * Policy manages active insurance contracts. According to {@code
   * policy/policy-core/policy-core.gradle} and {@code policy/policy-ui/policy-ui.gradle}, it
   * depends on {@code partner-api-starter}/{@code partner-ui-api} to link policies to
   * customers/agencies, {@code account-api-starter} to track policy financial accounts, and {@code
   * product-api-starter} to fetch catalog configurations. It must not depend on quote or security
   * modules.
   */
  @ArchTest
  public static final ArchRule policyModuleDependenciesAreRestricted =
      classes()
          .that()
          .resideInAPackage(ArchitectureSlice.POLICY.packagePattern())
          .should()
          .onlyDependOnClassesThat(
              resideInAllowedSlices(
                  ArchitectureSlice.POLICY,
                  ArchitectureSlice.PARTNER,
                  ArchitectureSlice.ACCOUNT,
                  ArchitectureSlice.PRODUCT));

  /**
   * Quote handles offers, risk calculations, and quotes prior to policy activation. According to
   * {@code quote/quote-core/quote-core.gradle} and {@code quote/quote-api/quote-api.gradle}, it
   * depends on {@code partner-api-starter} (for prospect details), {@code policy-api-starter} (to
   * convert quotes to active policies), and {@code product-api-starter} (to retrieve catalog
   * product configurations). It must not depend on account or security modules.
   */
  @ArchTest
  public static final ArchRule quoteModuleDependenciesAreRestricted =
      classes()
          .that()
          .resideInAPackage(ArchitectureSlice.QUOTE.packagePattern())
          .should()
          .onlyDependOnClassesThat(
              resideInAllowedSlices(
                  ArchitectureSlice.QUOTE,
                  ArchitectureSlice.PARTNER,
                  ArchitectureSlice.POLICY,
                  ArchitectureSlice.PRODUCT));
}
