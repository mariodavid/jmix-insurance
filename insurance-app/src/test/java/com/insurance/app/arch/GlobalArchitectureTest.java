package com.insurance.app.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalArchitectureTest {

    private static final List<String> MODULES = List.of("account", "partner", "policy", "product", "quote");
    private static final List<Path> CORE_MODULES = List.of(
            Path.of("account-core", "account-core"),
            Path.of("partner-core", "partner-core"),
            Path.of("policy-core", "policy-core"),
            Path.of("product-core", "product-core"),
            Path.of("quote-core", "quote-core")
    );
    private static final List<Path> PRODUCT_MODULE_SOURCES = List.of(
            Path.of("product-api", "product-api", "src"),
            Path.of("product-core", "product-core", "src")
    );

    private final JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.insurance");

    @Test
    void apiPackagesDoNotDependOnCoreUiOrFlowUi() {
        noClasses()
                .that().resideInAPackage(anyInsuranceApiPackage())
                .should().dependOnClassesThat().resideInAnyPackage(
                        anyInsuranceCorePackage(),
                        anyInsuranceUiPackage(),
                        anyFlowUiPackage()
                )
                .check(productionClasses);
    }

    @Test
    void corePackagesDoNotDependOnForeignCoreOrUiPackages() {
        MODULES.forEach(module -> noClasses()
                .that().resideInAPackage(corePackageOf(module))
                .should().dependOnClassesThat().resideInAnyPackage(foreignCoreOrUiPackagesOf(module))
                .allowEmptyShould(true)
                .check(productionClasses));
    }

    @Test
    void uiPackagesDoNotDependOnForeignCorePackages() {
        MODULES.forEach(module -> noClasses()
                .that().resideInAPackage(uiPackageOf(module))
                .should().dependOnClassesThat().resideInAnyPackage(foreignCorePackagesOf(module))
                .allowEmptyShould(true)
                .check(productionClasses));
    }

    @Test
    void corePackagesDoNotDependOnFlowUi() {
        noClasses()
                .that().resideInAPackage(anyInsuranceCorePackage())
                .should().dependOnClassesThat().resideInAnyPackage(anyFlowUiPackage())
                .check(productionClasses);
    }

    @Test
    void coreModulesDoNotDeclareFlowUiOrViewResources() {
        CORE_MODULES.forEach(module -> {
            Path moduleRoot = projectRoot().resolve(module);
            Path gradleFile = moduleRoot.resolve(module.getFileName() + ".gradle");

            assertThat(read(gradleFile))
                    .as("%s must stay independent from Flow UI", projectRoot().relativize(gradleFile))
                    .doesNotContain("jmix-flowui");

            Path resources = moduleRoot.resolve(Path.of("src", "main", "resources"));
            if (Files.exists(resources)) {
                walk(resources).forEach(path -> {
                    String relativePath = resources.relativize(path).toString().replace('\\', '/');

                    assertThat(relativePath)
                            .as("%s must not contain Flow UI view resources", projectRoot().relativize(path))
                            .doesNotContain("/view/");
                    assertThat(read(path))
                            .as("%s must not use Flow UI XML schema", projectRoot().relativize(path))
                            .doesNotContain("jmix.io/schema/flowui");
                });
            }
        });
    }

    @Test
    void productModulesDoNotContainPartnerLeftovers() {
        PRODUCT_MODULE_SOURCES.forEach(sourceRoot -> walk(projectRoot().resolve(sourceRoot)).forEach(path -> {
            String relativePath = projectRoot().relativize(path).toString().replace('\\', '/');

            assertThat(relativePath)
                    .as("%s must not be a partner package/resource inside product", relativePath)
                    .doesNotContain("/com/insurance/partner/");

            if (relativePath.contains("/src/main/")) {
                assertThat(read(path))
                        .as("%s must not import or reference partner implementation classes", relativePath)
                        .doesNotContain("com.insurance.partner");
            }
        }));
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
                .flatMap(otherModule -> Stream.of(layers)
                        .map(layer -> moduleLayerPackage(otherModule, layer)))
                .toArray(String[]::new);
    }

    private static String moduleLayerPackage(String module, String layer) {
        return "com.insurance." + module + "." + layer + "..";
    }

    private static Path projectRoot() {
        Path userDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        return userDir.getFileName().toString().equals("insurance-app") ? userDir.getParent() : userDir;
    }

    private static Stream<Path> walk(Path root) {
        try {
            return Files.walk(root).filter(Files::isRegularFile);
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
