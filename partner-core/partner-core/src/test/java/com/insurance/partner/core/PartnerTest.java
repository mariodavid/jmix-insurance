package com.insurance.partner.core;

import com.insurance.partner.api.dto.PartnerDto;
import com.insurance.partner.api.service.PartnerService;
import com.insurance.partner.core.entity.Partner;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class PartnerTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private final List<Partner> cleanup = new ArrayList<>();

    @Test
    void contextLoads() {
    }

    @Test
    void given_partner_when_savedAndUpdated_then_commonEntityAuditFieldsAndVersionAreMaintained() {
        Partner saved = systemAuthenticator.withSystem(() -> {
            Partner partner = dataManager.create(Partner.class);
            partner.setFirstName("Audit");
            partner.setLastName("Partner");
            return dataManager.save(partner);
        });
        cleanup.add(saved);

        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getLastModifiedDate()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(1);

        Partner updated = systemAuthenticator.withSystem(() -> {
            Partner reloaded = dataManager.load(Partner.class).id(saved.getId()).one();
            reloaded.setLastName("Updated");
            return dataManager.save(reloaded);
        });

        assertThat(updated.getCreatedDate()).isEqualTo(saved.getCreatedDate());
        assertThat(updated.getLastModifiedDate()).isNotNull();
        assertThat(updated.getVersion()).isGreaterThan(saved.getVersion());
        cleanup.remove(saved);
        cleanup.add(updated);
    }

    @Test
    void given_newPartnerWithoutPartnerNo_when_savedViaService_then_partnerNoIsGeneratedAndFieldsMatch() {
        PartnerDto dto = dataManager.create(PartnerDto.class);
        dto.setFirstName("Anna");
        dto.setLastName("Schmidt");

        PartnerDto savedDto = systemAuthenticator.withUser("admin", () -> partnerService.savePartner(dto));

        Partner saved = systemAuthenticator.withUser("admin", () -> dataManager.load(Partner.class)
                .condition(PropertyCondition.equal("lastName", "Schmidt"))
                .one());
        cleanup.add(saved);

        assertThat(savedDto.getId()).isEqualTo(saved.getId());
        assertThat(savedDto.getPartnerNo()).matches("PT-\\d{5}");
        assertThat(saved.getPartnerNo()).isEqualTo(savedDto.getPartnerNo());
        assertThat(saved.getFirstName()).isEqualTo("Anna");
        assertThat(saved.getLastName()).isEqualTo("Schmidt");
    }

    @Test
    void given_existingPartner_when_savedViaService_then_partnerNoRemainsUnchangedAndNameChanges() {
        Partner existing = systemAuthenticator.withUser("admin",
                () -> savePartner("PT-81001", "Old", "Name"));
        cleanup.add(existing);

        PartnerDto dto = dataManager.create(PartnerDto.class);
        dto.setId(existing.getId());
        dto.setPartnerNo(existing.getPartnerNo());
        dto.setFirstName("Updated");
        dto.setLastName("Partner");

        PartnerDto savedDto = systemAuthenticator.withUser("admin", () -> partnerService.savePartner(dto));

        Partner updated = systemAuthenticator.withUser("admin",
                () -> dataManager.load(Partner.class).id(existing.getId()).one());

        assertThat(savedDto.getPartnerNo()).isEqualTo("PT-81001");
        assertThat(updated.getPartnerNo()).isEqualTo("PT-81001");
        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getLastName()).isEqualTo("Partner");
        cleanup.remove(existing);
        cleanup.add(updated);
    }

    @Test
    void given_multiplePartners_when_searching_then_resultIsFilteredSortedAndPaged() {
        Partner match1 = systemAuthenticator.withUser("admin",
                () -> savePartner("PT-82001", "Anna", "Mayer"));
        Partner other = systemAuthenticator.withUser("admin",
                () -> savePartner("PT-82002", "Bernd", "Schmidt"));
        Partner match2 = systemAuthenticator.withUser("admin",
                () -> savePartner("PT-82003", "Clara", "Mayer"));
        Partner match3 = systemAuthenticator.withUser("admin",
                () -> savePartner("PT-82004", "Mayer", "Schulz"));
        cleanup.addAll(List.of(match1, other, match2, match3));

        List<PartnerDto> firstPage = systemAuthenticator.withUser("admin",
                () -> partnerService.findPartners("Mayer", 2, 0));
        List<PartnerDto> secondPage = systemAuthenticator.withUser("admin",
                () -> partnerService.findPartners("Mayer", 2, 2));

        assertThat(firstPage)
                .extracting(PartnerDto::getPartnerNo)
                .containsExactly("PT-82001", "PT-82003");
        assertThat(secondPage)
                .extracting(PartnerDto::getPartnerNo)
                .containsExactly("PT-82004");
    }

    @Test
    void given_unknownPartnerNo_when_getPartner_then_nullIsReturned() {
        PartnerDto found = systemAuthenticator.withUser("admin",
                () -> partnerService.getPartner("PT-UNKNOWN"));

        assertThat(found).isNull();
    }

    @AfterEach
    void tearDown() {
        cleanup.forEach(partner -> systemAuthenticator.withUser("admin", () -> {
            dataManager.load(Partner.class)
                    .id(partner.getId())
                    .optional()
                    .ifPresent(dataManager::remove);
            return null;
        }));
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
