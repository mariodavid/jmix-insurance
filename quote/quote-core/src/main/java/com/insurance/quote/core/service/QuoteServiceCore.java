package com.insurance.quote.core.service;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.quote.api.dto.QuoteDto;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.api.service.QuoteService;
import com.insurance.quote.core.entity.Quote;
import com.insurance.quote.core.entity.QuotePolicyReference;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.TimeSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("quote_QuoteService")
public class QuoteServiceCore implements QuoteService {

  private static final Logger log = LoggerFactory.getLogger(QuoteServiceCore.class);
  private static final String MDC_CORRELATION_ID = "correlationId";
  private static final String MDC_QUOTE_NO = "quoteNo";
  private static final String MDC_POLICY_NO = "policyNo";
  private static final String MDC_PARTNER_NO = "partnerNo";

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
    assertPendingTransition(quote, "reject");
    quote.setStatus(QuoteStatus.REJECTED);
    quote.setRejectedAt(timeSource.now().toLocalDateTime());
    dataManager.save(quote);
  }

  @Override
  @Transactional
  public QuoteDto accept(Id<?> quoteId) {
    String previousCorrelationId = ensureCorrelationId();
    String previousQuoteNo = MDC.get(MDC_QUOTE_NO);
    String previousPolicyNo = MDC.get(MDC_POLICY_NO);
    String previousPartnerNo = MDC.get(MDC_PARTNER_NO);
    Quote quote = null;

    try {
      log.debug("quote.accept.load quoteId={}", quoteId);
      quote = loadQuote(quoteId);
      putMdc(MDC_QUOTE_NO, quote.getQuoteNo());
      putMdc(MDC_PARTNER_NO, quote.getPartnerNo());
      log.info(
          "quote.accept.started quoteNo={} partnerNo={}", quote.getQuoteNo(), quote.getPartnerNo());
      assertCanAccept(quote);

      CreatePolicyRequestDto request =
          new CreatePolicyRequestDto(
              quote.getQuoteNo(),
              quote.getPartnerNo(),
              quote.getInsuranceProduct().getId(),
              quote.getEffectiveDate(),
              quote.getCalculatedPremium(),
              quote.getPaymentFrequency().getId());

      PolicyDto policyResponse = policyService.createPolicy(request);
      putMdc(MDC_POLICY_NO, policyResponse.getPolicyNo());
      log.info(
          "quote.accept.policy-created quoteNo={} policyNo={}",
          quote.getQuoteNo(),
          policyResponse.getPolicyNo());

      quote.setStatus(QuoteStatus.ACCEPTED);
      quote.setAcceptedAt(timeSource.now().toLocalDateTime());

      QuotePolicyReference createdPolicyRef = dataManager.create(QuotePolicyReference.class);
      createdPolicyRef.setPolicyId(policyResponse.getId());
      createdPolicyRef.setPolicyNo(policyResponse.getPolicyNo());
      quote.setCreatedPolicy(createdPolicyRef);

      log.debug(
          "quote.accept.persist quoteNo={} policyNo={}",
          quote.getQuoteNo(),
          policyResponse.getPolicyNo());
      Quote savedQuote = dataManager.save(quote);
      log.info(
          "quote.accept.completed quoteNo={} policyNo={}",
          savedQuote.getQuoteNo(),
          savedQuote.getCreatedPolicyNo());

      return mapToDto(savedQuote);
    } catch (RuntimeException e) {
      String quoteNo = quote == null ? null : quote.getQuoteNo();
      log.error("quote.accept.failed quoteId={} quoteNo={}", quoteId, quoteNo, e);
      throw e;
    } finally {
      restoreMdc(MDC_PARTNER_NO, previousPartnerNo);
      restoreMdc(MDC_POLICY_NO, previousPolicyNo);
      restoreMdc(MDC_QUOTE_NO, previousQuoteNo);
      restoreMdc(MDC_CORRELATION_ID, previousCorrelationId);
    }
  }

  private Quote loadQuote(Id<?> quoteId) {
    return (Quote) dataManager.load(quoteId).one();
  }

  private void assertCanAccept(Quote quote) {
    assertPendingTransition(quote, "accept");
    if (quote.getCalculatedPremium() == null
        || quote.getCalculatedPremium().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalStateException(
          "Quote " + quote.getQuoteNo() + " requires a positive calculated premium");
    }
    if (quote.getInsuranceProduct() == null
        || quote.getPaymentFrequency() == null
        || quote.getEffectiveDate() == null) {
      throw new IllegalStateException(
          "Quote " + quote.getQuoteNo() + " is missing required policy creation data");
    }

    LocalDate today = timeSource.now().toLocalDate();
    if (quote.getValidFrom() == null
        || quote.getValidUntil() == null
        || quote.getValidFrom().isAfter(today)
        || quote.getValidUntil().isBefore(today)) {
      throw new IllegalStateException("Quote " + quote.getQuoteNo() + " is not valid on " + today);
    }
  }

  private void assertPendingTransition(Quote quote, String action) {
    if (quote.getStatus() != QuoteStatus.PENDING) {
      throw new IllegalStateException(
          "Only PENDING quotes can be " + action + "ed: " + quote.getQuoteNo());
    }
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

  private String ensureCorrelationId() {
    String previousCorrelationId = MDC.get(MDC_CORRELATION_ID);
    if (previousCorrelationId == null) {
      MDC.put(MDC_CORRELATION_ID, UUID.randomUUID().toString());
    }
    return previousCorrelationId;
  }

  private void putMdc(String key, String value) {
    if (value == null) {
      MDC.remove(key);
    } else {
      MDC.put(key, value);
    }
  }

  private void restoreMdc(String key, String previousValue) {
    if (previousValue == null) {
      MDC.remove(key);
    } else {
      MDC.put(key, previousValue);
    }
  }
}
