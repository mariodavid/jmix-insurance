package com.insurance.app.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Global architecture rules")
class ArchitectureTest {

    private static final List<String> MODULES = List.of("account", "partner", "policy", "product", "quote");
    private final JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.insurance");

    @Nested
    @DisplayName("API layer")
    class ApiLayer {

        @Test
        @DisplayName("API modules stay independent from Core, UI, and Flow UI")
        void apiModulesOnlyExposeApiContracts() {
            noClasses()
                    .that().resideInAPackage(anyInsuranceApiPackage())
                    .should().dependOnClassesThat().resideInAnyPackage(
                            anyInsuranceCorePackage(),
                            anyInsuranceUiPackage(),
                            anyFlowUiPackage()
                    )
                    .check(productionClasses);
        }
    }

    @Nested
    @DisplayName("Core layer")
    class CoreLayer {

        @Test
        @DisplayName("Core modules do not use foreign Core or UI implementations")
        void coreModulesOnlyUseTheirOwnImplementationAndForeignApis() {
            MODULES.forEach(module -> noClasses()
                    .that().resideInAPackage(corePackageOf(module))
                    .should().dependOnClassesThat().resideInAnyPackage(foreignCoreOrUiPackagesOf(module))
                    .allowEmptyShould(true)
                    .check(productionClasses));
        }

        @Test
        @DisplayName("Core classes stay independent from Flow UI")
        void coreClassesDoNotUseFlowUiApis() {
            noClasses()
                    .that().resideInAPackage(anyInsuranceCorePackage())
                    .should().dependOnClassesThat().resideInAnyPackage(anyFlowUiPackage())
                    .check(productionClasses);
        }

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "account-core/account-core",
                "partner-core/partner-core",
                "policy-core/policy-core",
                "product-core/product-core",
                "quote-core/quote-core"
        })
        @DisplayName("Core modules do not declare Flow UI dependencies or view resources")
        void coreModuleDoesNotDeclareFlowUiDependenciesOrViews(String modulePath) {
            assertCoreModuleDoesNotDeclareFlowUi(projectRoot().resolve(modulePath));
        }
    }

    @Nested
    @DisplayName("UI layer")
    class UiLayer {

        @Test
        @DisplayName("UI modules do not use foreign Core implementations")
        void uiModulesDoNotUseForeignCoreImplementations() {
            MODULES.forEach(module -> noClasses()
                    .that().resideInAPackage(uiPackageOf(module))
                    .should().dependOnClassesThat().resideInAnyPackage(foreignCorePackagesOf(module))
                    .allowEmptyShould(true)
                    .check(productionClasses));
        }
    }

    @Nested
    @DisplayName("Product module")
    class ProductModule {

        @Test
        @DisplayName("Product classes do not depend on Partner classes")
        void productClassesDoNotDependOnPartnerClasses() {
            noClasses()
                    .that().resideInAPackage(productPackage())
                    .should().dependOnClassesThat().resideInAnyPackage(partnerPackage())
                    .check(productionClasses);
        }
    }

    private static String anyInsuranceApiPackage() {
        return "com.insurance..api..";
    }

    private static String anyInsuranceCorePackage() {
        return "com.insurance..core..";
    }

    private static String anyInsuranceUiPackage() {
        return "com.insurance..ui..";
    }

    private static String anyFlowUiPackage() {
        return "io.jmix.flowui..";
    }

    private static String productPackage() {
        return "com.insurance.product..";
    }

    private static String partnerPackage() {
        return "com.insurance.partner..";
    }

    private static String corePackageOf(String module) {
        return moduleLayerPackage(module, "core");
    }

    private static String uiPackageOf(String module) {
        return moduleLayerPackage(module, "ui");
    }

    private static String[] foreignCoreOrUiPackagesOf(String module) {
        return foreignLayerPackagesOf(module, "core", "ui");
    }

    private static String[] foreignCorePackagesOf(String module) {
        return foreignLayerPackagesOf(module, "core");
    }

    private static String[] foreignLayerPackagesOf(String module, String... layers) {
        return MODULES.stream()
                .filter(otherModule -> !otherModule.equals(module))
                .flatMap(otherModule -> Stream.of(layers).map(layer -> moduleLayerPackage(otherModule, layer)))
                .toArray(String[]::new);
    }

    private static String moduleLayerPackage(String module, String layer) {
        return "com.insurance." + module + "." + layer + "..";
    }

    private static Path projectRoot() {
        Path userDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        return userDir.getFileName().toString().equals("insurance-app") ? userDir.getParent() : userDir;
    }

    private static void assertCoreModuleDoesNotDeclareFlowUi(Path moduleRoot) {
        assertGradleFileDoesNotDeclareFlowUi(moduleRoot);
        assertMainResourcesDoNotContainFlowUiViews(moduleRoot);
    }

    private static void assertGradleFileDoesNotDeclareFlowUi(Path moduleRoot) {
        Path gradleFile = moduleRoot.resolve(moduleRoot.getFileName() + ".gradle");

        assertThat(read(gradleFile))
                .as("%s must stay independent from Flow UI", projectRoot().relativize(gradleFile))
                .doesNotContain("jmix-flowui");
    }

    private static void assertMainResourcesDoNotContainFlowUiViews(Path moduleRoot) {
        Path resources = moduleRoot.resolve(Path.of("src", "main", "resources"));
        if (!Files.exists(resources)) {
            return;
        }

        assertThat(walk(resources))
                .allSatisfy(path -> assertResourceDoesNotDeclareFlowUiView(resources, path));
    }

    private static void assertResourceDoesNotDeclareFlowUiView(Path resources, Path path) {
        String relativePath = resources.relativize(path).toString().replace('\\', '/');

        assertThat(relativePath)
                .as("%s must not contain Flow UI view resources", projectRoot().relativize(path))
                .doesNotContain("/view/");
        assertThat(read(path))
                .as("%s must not use Flow UI XML schema", projectRoot().relativize(path))
                .doesNotContain("jmix.io/schema/flowui");
    }

    private static List<Path> walk(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
