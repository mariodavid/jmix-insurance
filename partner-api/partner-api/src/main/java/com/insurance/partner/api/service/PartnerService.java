package com.insurance.partner.api.service;

import com.insurance.partner.api.dto.PartnerDto;
import java.util.List;

/**
 * Service interface for managing and querying partners (customers)
 * in the monolithic insurance system.
 * <p>
 * This interface provides the public API of the Partner module,
 * facilitating decoupled communication between other business modules
 * (such as Quote and Policy) and the Partner domain.
 */
public interface PartnerService {

    /**
     * Searches for partners based on a search term (first name, last name, or partner number).
     *
     * @param search the search query (case-insensitive, wildcard search)
     * @param limit  the maximum number of partners to return (pagination)
     * @param offset the starting index for results (pagination)
     * @return a list of {@link PartnerDto} objects matching the search criteria
     */
    List<PartnerDto> findPartners(String search, int limit, int offset);

    /**
     * Retrieves a specific partner by their unique business key (partner number).
     *
     * @param partnerNo the unique partner number (e.g. "PT-10001")
     * @return the {@link PartnerDto} object of the partner, or {@code null} if no partner was found
     */
    PartnerDto getPartner(String partnerNo);

    /**
     * Saves a partner (creates a new one or updates an existing one).
     *
     * @param partnerDto the {@link PartnerDto} to be saved
     * @return the saved {@link PartnerDto} containing any generated values (e.g. partner number, generated ID)
     */
    PartnerDto savePartner(PartnerDto partnerDto);
}
