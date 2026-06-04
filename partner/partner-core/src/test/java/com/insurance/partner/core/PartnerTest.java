package com.insurance.partner.core;

import static com.insurance.partner.core.test_support.Assertions.assertThat;

import com.insurance.common.test_support.AuthenticatedAsAdmin;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.partner.api.dto.PartnerDto;
import com.insurance.partner.api.service.PartnerService;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.test_support.PartnerDataProvider;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ExtendWith(AuthenticatedAsAdmin.class)
class PartnerTest {

  @Autowired private DataManager dataManager;

  @Autowired private PartnerService partnerService;

  @Autowired private EntityTestData entityTestData;

  private final List<Partner> cleanup = new ArrayList<>();

  @Test
  void contextLoads() {}

  @Test
  void given_partner_when_savedAndUpdated_then_commonEntityAuditFieldsAndVersionAreMaintained() {
    Partner saved =
        entityTestData.saveWithDefaults(
            new PartnerDataProvider(),
            partner -> {
              partner.setFirstName("Audit");
              partner.setLastName("Partner");
            });
    cleanup.add(saved);

    assertThat(saved.getCreatedDate()).isNotNull();
    assertThat(saved.getLastModifiedDate()).isNotNull();
    assertThat(saved.getVersion()).isEqualTo(1);

    Partner reloaded = dataManager.load(Partner.class).id(saved.getId()).one();
    reloaded.setLastName("Updated");
    Partner updated = dataManager.save(reloaded);

    assertThat(updated.getCreatedDate()).isEqualTo(saved.getCreatedDate());
    assertThat(updated.getLastModifiedDate()).isNotNull();
    assertThat(updated.getVersion()).isGreaterThan(saved.getVersion());
    cleanup.remove(saved);
    cleanup.add(updated);
  }

  @Test
  void
      given_newPartnerWithoutPartnerNo_when_savedViaService_then_partnerNoIsGeneratedAndFieldsMatch() {
    PartnerDto dto = dataManager.create(PartnerDto.class);
    dto.setFirstName("Anna");
    dto.setLastName("Schmidt");

    PartnerDto savedDto = partnerService.savePartner(dto);

    Partner saved =
        dataManager
            .load(Partner.class)
            .condition(PropertyCondition.equal("lastName", "Schmidt"))
            .one();
    cleanup.add(saved);

    assertThat(savedDto.getId()).isEqualTo(saved.getId());
    assertThat(savedDto.getPartnerNo()).matches("PT-\\d{5}");
    assertThat(saved.getPartnerNo()).isEqualTo(savedDto.getPartnerNo());
    assertThat(saved.getFirstName()).isEqualTo("Anna");
    assertThat(saved.getLastName()).isEqualTo("Schmidt");
  }

  @Test
  void given_existingPartner_when_savedViaService_then_partnerNoRemainsUnchangedAndNameChanges() {
    Partner existing = savePartner("PT-81001", "Old", "Name");
    cleanup.add(existing);

    PartnerDto dto = dataManager.create(PartnerDto.class);
    dto.setId(existing.getId());
    dto.setPartnerNo(existing.getPartnerNo());
    dto.setFirstName("Updated");
    dto.setLastName("Partner");

    PartnerDto savedDto = partnerService.savePartner(dto);

    Partner updated = dataManager.load(Partner.class).id(existing.getId()).one();

    assertThat(savedDto.getPartnerNo()).isEqualTo("PT-81001");
    assertThat(updated.getPartnerNo()).isEqualTo("PT-81001");
    assertThat(updated.getFirstName()).isEqualTo("Updated");
    assertThat(updated.getLastName()).isEqualTo("Partner");
    cleanup.remove(existing);
    cleanup.add(updated);
  }

  @Test
  void given_multiplePartners_when_searching_then_resultIsFilteredSortedAndPaged() {
    Partner match1 = savePartner("PT-82001", "Anna", "Mayer");
    Partner other = savePartner("PT-82002", "Bernd", "Schmidt");
    Partner match2 = savePartner("PT-82003", "Clara", "Mayer");
    Partner match3 = savePartner("PT-82004", "Mayer", "Schulz");
    cleanup.addAll(List.of(match1, other, match2, match3));

    List<PartnerDto> firstPage = partnerService.findPartners("Mayer", 2, 0);
    List<PartnerDto> secondPage = partnerService.findPartners("Mayer", 2, 2);

    assertThat(firstPage)
        .extracting(PartnerDto::getPartnerNo)
        .containsExactly("PT-82001", "PT-82003");
    assertThat(secondPage).extracting(PartnerDto::getPartnerNo).containsExactly("PT-82004");
  }

  @Test
  void given_unknownPartnerNo_when_getPartner_then_nullIsReturned() {
    PartnerDto found = partnerService.getPartner("PT-UNKNOWN");

    assertThat(found).isNull();
  }

  @AfterEach
  void tearDown() {
    cleanup.forEach(
        partner -> {
          dataManager
              .load(Partner.class)
              .id(partner.getId())
              .optional()
              .ifPresent(dataManager::remove);
        });
    cleanup.clear();
  }

  private Partner savePartner(String partnerNo, String firstName, String lastName) {
    Partner partner = dataManager.create(Partner.class);
    partner.setPartnerNo(partnerNo);
    partner.setFirstName(firstName);
    partner.setLastName(lastName);
    return dataManager.save(partner);
  }
}
