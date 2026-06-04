package com.insurance.policy.core.service;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.event.PolicyCreatedEvent;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.policy.core.entity.Policy;
import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.DataManager;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("policy_PolicyService")
public class PolicyServiceCore implements PolicyService {

  private static final Logger log = LoggerFactory.getLogger(PolicyServiceCore.class);

  private final DataManager dataManager;
  private final Sequences sequences;
  private final ApplicationEventPublisher eventPublisher;

  public PolicyServiceCore(
      DataManager dataManager, Sequences sequences, ApplicationEventPublisher eventPublisher) {
    this.dataManager = dataManager;
    this.sequences = sequences;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public PolicyDto createPolicy(CreatePolicyRequestDto request) {
    log.debug("Starting policy creation");

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
    log.debug("Policy no: {}", policyNo);

    Policy policy = dataManager.create(Policy.class);
    policy.setPolicyNo(policyNo);
    policy.setPartnerNo(request.partnerNo());
    policy.setInsuranceProduct(insuranceProduct);
    policy.setCoverageStart(request.effectiveDate());
    policy.setPaymentFrequency(paymentFrequency);
    policy.setPremium(request.premium());

    LocalDate coverageEnd = request.effectiveDate().plusYears(1);
    log.debug("Calculated coverage end: {}", coverageEnd);
    policy.setCoverageEnd(coverageEnd);

    log.debug("Trying to save policy");
    Policy savedPolicy = dataManager.save(policy);
    log.debug("Policy saving successful");

    eventPublisher.publishEvent(
        new PolicyCreatedEvent(
            this,
            savedPolicy.getId(),
            savedPolicy.getPolicyNo(),
            savedPolicy.getCoverageStart(),
            savedPolicy.getPremium(),
            savedPolicy.getPaymentFrequency().getId()));

    return mapToDto(savedPolicy);
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
}
