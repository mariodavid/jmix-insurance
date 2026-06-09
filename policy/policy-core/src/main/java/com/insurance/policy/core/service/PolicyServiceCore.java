package com.insurance.policy.core.service;

import com.insurance.partner.api.dto.PartnerDto;
import com.insurance.partner.api.service.PartnerService;
import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.event.PolicyCreatedEvent;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.policy.core.entity.Policy;
import com.insurance.policy.core.entity.PolicyPartnerReference;
import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.DataManager;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("PMD.GuardLogStatement")
@Service("policy_PolicyService")
public class PolicyServiceCore implements PolicyService {

  private static final Logger log = LoggerFactory.getLogger(PolicyServiceCore.class);
  private static final String MDC_QUOTE_NO = "quoteNo";
  private static final String MDC_POLICY_NO = "policyNo";
  private static final String MDC_PARTNER_NO = "partnerNo";

  private final DataManager dataManager;
  private final Sequences sequences;
  private final ApplicationEventPublisher eventPublisher;
  private final PartnerService partnerService;

  public PolicyServiceCore(
      DataManager dataManager,
      Sequences sequences,
      ApplicationEventPublisher eventPublisher,
      PartnerService partnerService) {
    this.dataManager = dataManager;
    this.sequences = sequences;
    this.eventPublisher = eventPublisher;
    this.partnerService = partnerService;
  }

  @SuppressWarnings({"PMD.ExceptionAsFlowControl", "PMD.AvoidCatchingGenericException"})
  @Override
  @Transactional
  public PolicyDto createPolicy(CreatePolicyRequestDto request) {
    String previousQuoteNo = replaceMdc(MDC_QUOTE_NO, request.quoteNo());
    String previousPartnerNo = replaceMdc(MDC_PARTNER_NO, request.partnerNo());
    String previousPolicyNo = MDC.get(MDC_POLICY_NO);

    try {
      log.info(
          "policy.create.started quoteNo={} partnerNo={}", request.quoteNo(), request.partnerNo());

      InsuranceProduct insuranceProduct = InsuranceProduct.fromId(request.insuranceProductId());
      if (insuranceProduct == null) {
        throw new IllegalArgumentException(
            "Unknown insurance product: " + request.insuranceProductId());
      }

      PaymentFrequency paymentFrequency = PaymentFrequency.fromId(request.paymentFrequencyId());
      if (paymentFrequency == null) {
        throw new IllegalArgumentException(
            "Unknown payment frequency: " + request.paymentFrequencyId());
      }

      long policySequenceNumber = sequences.createNextValue(Sequence.withName("policy"));

      String productCode = insuranceProduct.getProductType().getName();
      String policyNo =
          "%s-%s-%06d"
              .formatted(productCode, request.effectiveDate().getYear(), policySequenceNumber);
      putMdc(MDC_POLICY_NO, policyNo);
      log.debug(
          "policy.create.number-assigned quoteNo={} policyNo={}", request.quoteNo(), policyNo);

      Policy policy = dataManager.create(Policy.class);
      policy.setPolicyNo(policyNo);

      PolicyPartnerReference partnerRef = dataManager.create(PolicyPartnerReference.class);
      partnerRef.setPartnerNo(request.partnerNo());
      if (partnerService != null) {
        PartnerDto partnerDto = partnerService.getPartner(request.partnerNo());
        if (partnerDto != null) {
          partnerRef.setPartnerId(partnerDto.getId());
        }
      }
      policy.setPartner(partnerRef);
      policy.setInsuranceProduct(insuranceProduct);
      policy.setCoverageStart(request.effectiveDate());
      policy.setPaymentFrequency(paymentFrequency);
      policy.setPremium(request.premium());

      LocalDate coverageEnd = request.effectiveDate().plusYears(1);
      log.debug(
          "policy.create.coverage-calculated policyNo={} coverageEnd={}", policyNo, coverageEnd);
      policy.setCoverageEnd(coverageEnd);

      Policy savedPolicy = dataManager.save(policy);
      log.info(
          "policy.created quoteNo={} policyNo={} partnerNo={}",
          request.quoteNo(),
          savedPolicy.getPolicyNo(),
          savedPolicy.getPartnerNo());

      log.info(
          "policy.created.event-published quoteNo={} policyNo={}",
          request.quoteNo(),
          savedPolicy.getPolicyNo());
      eventPublisher.publishEvent(
          new PolicyCreatedEvent(
              this,
              savedPolicy.getId(),
              savedPolicy.getPolicyNo(),
              savedPolicy.getPartnerNo(),
              savedPolicy.getCoverageStart(),
              savedPolicy.getCoverageEnd(),
              savedPolicy.getPremium(),
              savedPolicy.getPaymentFrequency().getId()));

      return mapToDto(savedPolicy);
    } catch (RuntimeException e) {
      log.error(
          "policy.create.failed quoteNo={} policyNo={}",
          request.quoteNo(),
          MDC.get(MDC_POLICY_NO),
          e);
      throw e;
    } finally {
      restoreMdc(MDC_POLICY_NO, previousPolicyNo);
      restoreMdc(MDC_PARTNER_NO, previousPartnerNo);
      restoreMdc(MDC_QUOTE_NO, previousQuoteNo);
    }
  }

  @Override
  public PolicyDto findPolicyById(UUID policyId) {
    Policy policy = dataManager.load(Policy.class).id(policyId).optional().orElse(null);
    return mapToDto(policy);
  }

  private PolicyDto mapToDto(Policy policy) {
    if (policy == null) {
      return null;
    }
    PolicyDto dto = dataManager.create(PolicyDto.class);
    dto.setId(policy.getId());
    dto.setPartnerNo(policy.getPartnerNo());
    dto.setInsuranceProduct(policy.getInsuranceProduct().getId());
    dto.setPolicyNo(policy.getPolicyNo());
    dto.setCoverageStart(policy.getCoverageStart());
    dto.setCoverageEnd(policy.getCoverageEnd());
    dto.setPremium(policy.getPremium());
    dto.setPaymentFrequency(policy.getPaymentFrequency().getId());
    return dto;
  }

  private String replaceMdc(String key, String value) {
    String previousValue = MDC.get(key);
    putMdc(key, value);
    return previousValue;
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
