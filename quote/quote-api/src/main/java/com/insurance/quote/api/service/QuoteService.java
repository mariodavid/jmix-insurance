package com.insurance.quote.api.service;

import com.insurance.quote.api.dto.QuoteDto;
import io.jmix.core.Id;

/**
 * Public quote API used to complete the quote lifecycle.
 * <p>
 * The quote module owns quote status transitions. Accepting a quote delegates
 * policy creation to the policy API and stores the created policy reference on
 * the quote.
 */
public interface QuoteService {

    /**
     * Rejects a quote and records the rejection timestamp.
     *
     * @param quoteId the Jmix id of the quote to reject
     * @throws IllegalStateException if no quote exists for the id
     */
    void reject(Id<?> quoteId);

    /**
     * Accepts a quote, creates the corresponding policy, and stores the policy
     * reference on the quote.
     *
     * @param quoteId the Jmix id of the quote to accept
     * @return the saved quote data after the status and policy reference have been updated
     * @throws RuntimeException if quote loading, policy creation, or saving fails
     */
    QuoteDto accept(Id<?> quoteId);
}
