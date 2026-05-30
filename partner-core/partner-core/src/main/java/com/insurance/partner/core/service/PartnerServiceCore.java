package com.insurance.partner.core.service;

import com.insurance.partner.api.dto.PartnerDto;
import com.insurance.partner.api.service.PartnerService;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.repository.PartnerRepository;
import io.jmix.core.DataManager;
import io.jmix.core.security.Authenticated;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("partner_PartnerService")
public class PartnerServiceCore implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final DataManager dataManager;
    private final Sequences sequences;

    public PartnerServiceCore(PartnerRepository partnerRepository, DataManager dataManager, Sequences sequences) {
        this.partnerRepository = partnerRepository;
        this.dataManager = dataManager;
        this.sequences = sequences;
    }

    @Override
    @Authenticated
    public List<PartnerDto> findPartners(String search, int limit, int offset) {
        String queryStr = "select p from partner_Partner p";
        if (search != null && !search.trim().isEmpty()) {
            queryStr += " where (lower(p.firstName) like lower(:search) " +
                        " or lower(p.lastName) like lower(:search) " +
                        " or lower(p.partnerNo) like lower(:search))";
        }
        queryStr += " order by p.partnerNo";

        var loader = dataManager.load(Partner.class)
                .query(queryStr);

        if (search != null && !search.trim().isEmpty()) {
            loader.parameter("search", "%" + search.trim() + "%");
        }

        List<Partner> partners = loader
                .firstResult(offset >= 0 ? offset : 0)
                .maxResults(limit > 0 ? limit : 20)
                .list();

        return partners.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Authenticated
    public PartnerDto getPartner(String partnerNo) {
        return partnerRepository.findByPartnerNo(partnerNo)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Authenticated
    public PartnerDto savePartner(PartnerDto partnerDto) {
        Partner partner = null;
        
        // Find existing persistent Partner if ID is present
        if (partnerDto.getId() != null) {
            partner = dataManager.load(Partner.class)
                    .id(partnerDto.getId())
                    .optional()
                    .orElse(null);
        }

        // Create new Partner if it doesn't exist yet
        if (partner == null) {
            partner = dataManager.create(Partner.class);
            
            // Generate unique partnerNo using Jmix Sequences if not present
            if (partnerDto.getPartnerNo() == null || partnerDto.getPartnerNo().trim().isEmpty()) {
                long nextVal = sequences.createNextValue(Sequence.withName("partner_number_sequence"));
                partner.setPartnerNo("PT-" + String.format("%05d", nextVal));
            } else {
                partner.setPartnerNo(partnerDto.getPartnerNo());
            }
        }

        // Update fields
        partner.setFirstName(partnerDto.getFirstName());
        partner.setLastName(partnerDto.getLastName());

        // Save persistent entity
        Partner savedPartner = dataManager.save(partner);
        
        return mapToDto(savedPartner);
    }

    private PartnerDto mapToDto(Partner partner) {
        if (partner == null) {
            return null;
        }
        PartnerDto dto = dataManager.create(PartnerDto.class);
        dto.setId(partner.getId());
        dto.setPartnerNo(partner.getPartnerNo());
        dto.setFirstName(partner.getFirstName());
        dto.setLastName(partner.getLastName());
        return dto;
    }
}
