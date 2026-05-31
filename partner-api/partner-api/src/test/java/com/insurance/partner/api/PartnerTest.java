package com.insurance.partner.api;

import com.insurance.partner.api.dto.PartnerDto;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static com.insurance.partner.api.dto.Assertions.assertThat;

@SpringBootTest
class PartnerTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Metadata metadata;

    @Test
    void contextLoads() {
    }

    @Test
    void given_partnerDto_when_createdThroughDataManager_then_metadataAndInstanceNameAreStable() {
        PartnerDto dto = dataManager.create(PartnerDto.class);
        dto.setPartnerNo("PT-12345");
        dto.setFirstName("Anna");
        dto.setLastName("Schmidt");

        MetaClass metaClass = metadata.getClass(PartnerDto.class);

        assertThat(dto.getId()).isNotNull();
        assertThat(metaClass.getProperty("id")).isNotNull();
        assertThat(dto).hasPartnerNo("PT-12345");
        assertThat(dto).hasFirstName("Anna");
        assertThat(dto).hasLastName("Schmidt");
        assertThat(dto.instanceName()).isEqualTo("PT-12345 - Anna Schmidt");
    }

    @TestFactory
    Stream<DynamicTest> partnerApiDoesNotImportCoreOrUiPackages() throws IOException {
        Path sourceRoot = Path.of("src/main/java");
        List<Path> sources;
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            sources = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();
        }

        return sources.stream()
                .map(path -> DynamicTest.dynamicTest(sourceRoot.relativize(path).toString(), () -> {
                    String source = Files.readString(path);

                    assertThat(source)
                            .doesNotContain("import com.insurance.partner.core.")
                            .doesNotContain("import com.insurance.partner.ui.");
                }));
    }
}
