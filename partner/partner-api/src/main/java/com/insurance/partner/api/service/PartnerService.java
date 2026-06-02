package com.insurance.partner.api.service;

import com.insurance.partner.api.dto.PartnerDto;
import java.util.List;

/**
 * Public partner API used by other modules to search, read, and persist
 * customer records.
 * <p>
 * The partner module owns partner numbers and customer master data. Other
 * modules should depend on this contract instead of partner-core classes.
 */
public interface PartnerService {

    /**
     * Searches partners by first name, last name, or partner number.
     *
     * @param search the case-insensitive search term; blank values return all partners
     * @param limit  the maximum number of partners to return
     * @param offset the zero-based offset of the first result
     * @return matching partners ordered by partner number
     */
    List<PartnerDto> findPartners(String search, int limit, int offset);

    /**
     * Loads a partner by its business key.
     *
     * @param partnerNo the unique partner number, for example {@code PT-10001}
     * @return the partner data, or {@code null} if no partner exists for the number
     */
    PartnerDto getPartner(String partnerNo);

    /**
     * Creates or updates a partner.
     * <p>
     * If the DTO has an id, the existing partner is updated. Otherwise, a new
     * partner is created and a partner number is generated when none is supplied.
     *
     * @param partnerDto the partner data to persist
     * @return the saved partner data including generated values
     */
    PartnerDto savePartner(PartnerDto partnerDto);
}
