package com.insurance.account.core.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.insurance.account.core.service.AccountServiceCore;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.event.PolicyCreatedEvent;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.product.api.dto.PaymentFrequency;

import io.jmix.core.security.Authenticated;

@Component("account_PolicyCreatedEventListener")
public class PolicyCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(PolicyCreatedEventListener.class);

    private final AccountServiceCore accountService;
    private final PolicyService policyService;

    public PolicyCreatedEventListener(AccountServiceCore accountService, PolicyService policyService) {
        this.accountService = accountService;
        this.policyService = policyService;
    }

    @EventListener
    @Authenticated
    public void onPolicyCreated(final PolicyCreatedEvent event) {
        log.info("Local PolicyCreatedEvent received for policyId: {}", event.getPolicyId());

        PolicyDto policyDto = policyService.findPolicyById(event.getPolicyId());
        if (policyDto == null) {
            log.error("Policy with ID {} not found. Account creation aborted.", event.getPolicyId());
            return;
        }

        PaymentFrequency paymentFrequency = PaymentFrequency.fromId(policyDto.getPaymentFrequency());
        if (paymentFrequency == null) {
            log.error("Policy has unknown payment frequency {}. Account creation aborted.", policyDto.getPaymentFrequency());
            return;
        }

        accountService.createAccount(
                policyDto.getId().toString(),
                policyDto.getPolicyNo(),
                policyDto.getCoverageStart(),
                policyDto.getPremium(),
                paymentFrequency
        );

        log.info("Monolithic account automatically created for PolicyNo: {}", policyDto.getPolicyNo());
    }
}
