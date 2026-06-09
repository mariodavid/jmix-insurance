package com.insurance.product.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.product.api.dto.ProductType;
import com.insurance.product.api.dto.ProductVariant;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductTest {

  @Test
  void contextLoads() {}

  @Test
  void given_paymentFrequencyIds_when_fromId_then_frequenciesAreMappedAndUnknownIdReturnsNull() {
    assertThat(PaymentFrequency.fromId("YEARLY")).isEqualTo(PaymentFrequency.YEARLY);
    assertThat(PaymentFrequency.YEARLY.getFrequency()).isEqualTo(1);
    assertThat(PaymentFrequency.fromId("QUARTERLY")).isEqualTo(PaymentFrequency.QUARTERLY);
    assertThat(PaymentFrequency.QUARTERLY.getFrequency()).isEqualTo(4);
    assertThat(PaymentFrequency.fromId("MONTHLY")).isEqualTo(PaymentFrequency.MONTHLY);
    assertThat(PaymentFrequency.MONTHLY.getFrequency()).isEqualTo(12);
    assertThat(PaymentFrequency.fromId("UNKNOWN")).isNull();
  }

  @Test
  void given_wrongExpectedPremium_when_calculatePremium_then_intentionallyFails() {
    InsuranceProduct product = InsuranceProduct.HOME_CONTENT_BASIC_2024_01;
    assertThat(product.calculatePremium(new BigDecimal("50.00"))).isEqualByComparingTo("999.00");
  }

  @Test
  void given_homeContentBasicProductId_when_fromId_then_productDataAndPremiumAreCorrect() {
    InsuranceProduct product = InsuranceProduct.fromId("HOME_CONTENT_BASIC_2024_01");

    assertThat(product).isEqualTo(InsuranceProduct.HOME_CONTENT_BASIC_2024_01);
    assertThat(product.getProductType()).isEqualTo(ProductType.HOME_CONTENT);
    assertThat(product.getVariant()).isEqualTo(ProductVariant.SMALL);
    assertThat(product.getValidFrom()).isEqualTo(LocalDate.of(2022, 1, 1));
    assertThat(product.getValidUntil()).isNull();
    assertThat(product.calculatePremium(new BigDecimal("50.00"))).isEqualByComparingTo("200.00");
  }

  @Test
  void
      given_productTypeVariantAndEffectiveDate_when_findFirstMatchingProduct_then_matchingProductIsReturnedOnlyWithinValidity() {
    Optional<InsuranceProduct> valid =
        InsuranceProduct.findFirstMatchingProduct(
            ProductType.HOME_CONTENT, ProductVariant.SMALL, LocalDate.of(2024, 1, 1));
    Optional<InsuranceProduct> invalidDate =
        InsuranceProduct.findFirstMatchingProduct(
            ProductType.HOME_CONTENT, ProductVariant.LARGE, LocalDate.of(2023, 12, 31));

    assertThat(valid).contains(InsuranceProduct.HOME_CONTENT_BASIC_2024_01);
    assertThat(invalidDate).isEmpty();
  }

  @TestFactory
  Stream<DynamicTest> productModulesDoNotContainPartnerImportsOrPartnerViewResources()
      throws IOException {
    Path repositoryRoot = repositoryRoot();
    List<Path> roots =
        List.of(
            repositoryRoot.resolve("product/product-api/src/main"),
            repositoryRoot.resolve("product/product-core/src/main"));
    List<Path> files;
    try (Stream<Path> paths = roots.stream().flatMap(root -> walk(root).stream())) {
      files = paths.toList();
    }

    return files.stream()
        .map(
            path ->
                DynamicTest.dynamicTest(
                    repositoryRoot.relativize(path).toString(),
                    () -> {
                      assertThat(path.toString()).doesNotContain("/view/partner/");
                      if (path.toString().endsWith(".java")) {
                        assertThat(Files.readString(path))
                            .doesNotContain("import com.insurance.partner.")
                            .doesNotContain("com.insurance.partner.");
                      }
                    }));
  }

  private List<Path> walk(Path root) {
    if (!Files.exists(root)) {
      return List.of();
    }
    try (Stream<Path> paths = Files.walk(root)) {
      return paths.filter(Files::isRegularFile).toList();
    } catch (IOException e) {
      throw new IllegalStateException("Cannot inspect product module files below " + root, e);
    }
  }

  private Path repositoryRoot() {
    Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
    return Stream.iterate(current, Objects::nonNull, Path::getParent)
        .filter(path -> Files.exists(path.resolve("product/product-api/src/main")))
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("Cannot locate repository root from " + current));
  }
}
