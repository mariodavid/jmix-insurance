package com.insurance.product.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class ProductTest {

    @Test
    void contextLoads() {
    }

    @Test
    void productCoreMainSourceDoesNotContainAccidentalEntitiesViewsLiquibaseOrPartnerArtifacts() throws IOException {
        Path mainRoot = Path.of("src/main");

        assertThat(mainRoot.resolve("java/com/insurance/product/core/entity")).doesNotExist();
        assertThat(mainRoot.resolve("resources/com/insurance/product/core/liquibase")).doesNotExist();
        assertThat(mainRoot.resolve("resources/com/insurance/product/core/view")).doesNotExist();

        try (var paths = Files.walk(mainRoot)) {
            assertThat(paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java")
                            || path.toString().endsWith(".xml")
                            || path.toString().endsWith(".properties"))
                    .map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }))
                    .allSatisfy(content -> assertThat(content).doesNotContain("com.insurance.partner"));
        }
    }
}
