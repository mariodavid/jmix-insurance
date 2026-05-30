package com.insurance.quote.core.service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.TimeSource;

@Service("quote_QuoteService")
public class QuoteServiceCore implements QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteServiceCore.class);

    private final DataManager dataManager;
    private final PolicyService policyService;
    private final TimeSource timeSource;

    public QuoteServiceCore(DataManager dataManager, PolicyService policyService, TimeSource timeSource) {
        this.dataManager = dataManager;
        this.policyService = policyService;
        this.timeSource = timeSource;
    }

    @Override
    public void reject(Id<?> quoteId) {
        Quote quote = loadQuote(quoteId);
        quote.setStatus(QuoteStatus.REJECTED);
        quote.setRejectedAt(timeSource.now().toLocalDateTime());
        dataManager.save(quote);
    }

    @Override
    public Quote accept(Id<?> quoteId) {
        log.info("Trying to accept quote");

        log.debug("Loading quote with id {}", quoteId);
        Quote quote = loadQuote(quoteId);

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setAcceptedAt(timeSource.now().toLocalDateTime());

        log.debug("Saving accepted quote");
        Quote savedQuote = dataManager.save(quote);
        log.info("Quote accepted successfully");

        CreatePolicyRequestDto request = new CreatePolicyRequestDto(
                quote.getQuoteNo(),
                quote.getPartnerNo(),
                quote.getInsuranceProduct().getId(),
                quote.getEffectiveDate(),
                quote.getCalculatedPremium(),
                quote.getPaymentFrequency().getId()
        );

        PolicyDto policyResponse = policyService.createPolicy(request);

        log.info("Policy creation successful");

        log.debug("Updating quote policy references");
        savedQuote.setCreatedPolicyNo(policyResponse.getPolicyNo());
        savedQuote.setCreatedPolicyId(policyResponse.getId().toString());
        Quote updatedQuote = dataManager.save(savedQuote);
        log.debug("Quote policy references updated successfully");

        return updatedQuote;
    }

    private Quote loadQuote(Id<?> quoteId) {
        return (Quote) dataManager.load(quoteId).one();
    }
}
