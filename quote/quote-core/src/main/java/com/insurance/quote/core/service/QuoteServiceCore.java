package com.insurance.quote.core.service;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.quote.api.dto.QuoteDto;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.TimeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("quote_QuoteService")
public class QuoteServiceCore implements QuoteService {

  private static final Logger log = LoggerFactory.getLogger(QuoteServiceCore.class);

  private final DataManager dataManager;
  private final PolicyService policyService;
  private final TimeSource timeSource;

  public QuoteServiceCore(
      DataManager dataManager, PolicyService policyService, TimeSource timeSource) {
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
  @Transactional
  public QuoteDto accept(Id<?> quoteId) {
    log.info("Trying to accept quote");

    log.debug("Loading quote with id {}", quoteId);
    Quote quote = loadQuote(quoteId);

    CreatePolicyRequestDto request =
        new CreatePolicyRequestDto(
            quote.getQuoteNo(),
            quote.getPartnerNo(),
            quote.getInsuranceProduct().getId(),
            quote.getEffectiveDate(),
            quote.getCalculatedPremium(),
            quote.getPaymentFrequency().getId());

    PolicyDto policyResponse = policyService.createPolicy(request);
    log.info("Policy creation successful");

    quote.setStatus(QuoteStatus.ACCEPTED);
    quote.setAcceptedAt(timeSource.now().toLocalDateTime());
    quote.setCreatedPolicyNo(policyResponse.getPolicyNo());
    quote.setCreatedPolicyId(policyResponse.getId());

    log.debug("Saving accepted quote with policy references");
    Quote savedQuote = dataManager.save(quote);
    log.debug("Quote accepted and policy references saved successfully");

    return mapToDto(savedQuote);
  }

  private Quote loadQuote(Id<?> quoteId) {
    return (Quote) dataManager.load(quoteId).one();
  }

  private QuoteDto mapToDto(Quote quote) {
    QuoteDto dto = dataManager.create(QuoteDto.class);
    dto.setId(quote.getId());
    dto.setPartnerNo(quote.getPartnerNo());
    dto.setQuoteNo(quote.getQuoteNo());
    dto.setStatus(quote.getStatus());
    dto.setProductType(quote.getProductType());
    dto.setProductVariant(quote.getProductVariant());
    dto.setPaymentFrequency(quote.getPaymentFrequency());
    dto.setInsuranceProduct(quote.getInsuranceProduct());
    dto.setEffectiveDate(quote.getEffectiveDate());
    dto.setSquareMeters(quote.getSquareMeters());
    dto.setCalculatedPremium(quote.getCalculatedPremium());
    dto.setValidFrom(quote.getValidFrom());
    dto.setValidUntil(quote.getValidUntil());
    dto.setCreatedPolicyNo(quote.getCreatedPolicyNo());
    dto.setCreatedPolicyId(quote.getCreatedPolicyId());
    dto.setAcceptedAt(quote.getAcceptedAt());
    dto.setRejectedAt(quote.getRejectedAt());
    return dto;
  }
}
