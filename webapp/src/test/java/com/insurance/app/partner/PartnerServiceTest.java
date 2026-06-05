package com.insurance.app.partner;

import static com.insurance.app.test_support.assertion.InsuranceAssertions.assertThat;

import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.partner.api.dto.PartnerDto;
import com.insurance.partner.api.service.PartnerService;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.test_support.PartnerDataProvider;
import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PartnerServiceTest extends BaseIntegrationTest {

  @Autowired private PartnerService partnerService;

  @Autowired private DataManager dataManager;

  @Autowired private EntityTestData entityTestData;

  @Autowired private DatabaseCleanup databaseCleanup;

  @BeforeEach
  void setUp() {
    databaseCleanup.removeAllEntities();
  }

  @Test
  void given_newPartnerWithoutPartnerNo_when_saved_then_partnerNoIsGenerated() {
    // given
    PartnerDto dto = dataManager.create(PartnerDto.class);
    dto.setFirstName("Anna");
    dto.setLastName("Schmidt");

    // when
    partnerService.savePartner(dto);

    // then
    Partner saved = loadPartnerByLastName("Schmidt");
    assertThat(saved).hasPartnerNoMatchingPattern().hasFirstName("Anna").hasLastName("Schmidt");
  }

  @Test
  void given_existingPartner_when_savedWithUpdatedFields_then_partnerNoRemainsUnchanged() {
    // given
    Partner existing = entityTestData.saveWithDefaults(new PartnerDataProvider());

    // when
    PartnerDto dto = dataManager.create(PartnerDto.class);
    dto.setId(existing.getId());
    dto.setPartnerNo(existing.getPartnerNo());
    dto.setFirstName("Updated");
    dto.setLastName("Name");
    partnerService.savePartner(dto);

    // then
    Partner updated = dataManager.load(Partner.class).id(existing.getId()).one();
    assertThat(updated)
        .hasPartnerNo(existing.getPartnerNo())
        .hasFirstName("Updated")
        .hasLastName("Name");
  }

  @Test
  void given_twoPartners_when_searchByLastName_then_onlyMatchingPartnerReturned() {
    // given
    entityTestData.saveWithDefaults(new PartnerDataProvider(), p -> p.setLastName("Mayer"));
    entityTestData.saveWithDefaults(new PartnerDataProvider(), p -> p.setLastName("Müller"));

    // when
    List<PartnerDto> result = partnerService.findPartners("Mayer", 10, 0);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getLastName()).isEqualTo("Mayer");
  }

  @Test
  void given_savedPartner_when_loadedByPartnerNo_then_correctDtoReturned() {
    // given
    Partner saved = entityTestData.saveWithDefaults(new PartnerDataProvider());

    // when
    PartnerDto found = partnerService.getPartner(saved.getPartnerNo());

    // then
    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(saved.getId());
    assertThat(found.getFirstName()).isEqualTo(saved.getFirstName());
    assertThat(found.getLastName()).isEqualTo(saved.getLastName());
  }

  private Partner loadPartnerByLastName(String lastName) {
    return dataManager
        .load(Partner.class)
        .condition(PropertyCondition.equal("lastName", lastName))
        .one();
  }
}
