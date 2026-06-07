package com.insurance.common.test_support.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.ArchTest;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JmixDomainBoundaryRules {

  @ArchTest
  public static void domainModulesDoNotReferenceForeignPersistentEntities(JavaClasses classes) {
    Map<String, String> entityOwners = PersistentEntityIndex.persistentEntityOwners();
    List<ForeignEntityReference> violations = new ArrayList<>();

    for (String module : ArchitectureProject.entityBoundaryModules()) {
      for (Path path : ArchitectureProject.productionJavaAndXmlFiles(module)) {
        String content = ArchitectureFiles.read(path);
        entityOwners.forEach(
            (entityName, owner) -> {
              if (!owner.equals(module)
                  && PersistentEntityIndex.containsEntityName(content, entityName)) {
                violations.add(new ForeignEntityReference(path, module, entityName, owner));
              }
            });
      }
    }

    assertThat(violations)
        .as(
            "Domain modules must not reference foreign persistent Jmix entity names directly. "
                + "Use the owning module's API service or DTO instead.")
        .isEmpty();
  }

  private record ForeignEntityReference(
      Path path, String referencingModule, String entityName, String entityOwner) {

    @Override
    public String toString() {
      return ArchitectureProject.projectRoot().relativize(path.toAbsolutePath().normalize())
          + " in module "
          + referencingModule
          + " references foreign entity "
          + entityName
          + " owned by "
          + entityOwner
          + ". Use "
          + entityOwner
          + "-api instead of direct entity access.";
    }
  }
}
