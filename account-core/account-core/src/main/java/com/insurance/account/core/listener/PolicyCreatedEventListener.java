package com.insurance.account.core.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.insurance.account.core.service.AccountServiceCore;
import com.insurance.policy.api.event.PolicyCreatedEvent;
import com.insurance.product.api.dto.PaymentFrequency;

import io.jmix.core.security.Authenticated;

@Component("account_PolicyCreatedEventListener")
public class PolicyCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(PolicyCreatedEventListener.class);

    private final AccountServiceCore accountService;

    public PolicyCreatedEventListener(AccountServiceCore accountService) {
        this.accountService = accountService;
    }

    @EventListener
    @Authenticated
    public void onPolicyCreated(final PolicyCreatedEvent event) {
        log.info("Local PolicyCreatedEvent received for policyId: {}", event.getPolicyId());

        PaymentFrequency paymentFrequency = PaymentFrequency.fromId(event.getPaymentFrequencyId());
        if (paymentFrequency == null) {
            log.error("Policy has unknown payment frequency {}. Account creation aborted.", event.getPaymentFrequencyId());
            return;
        }

        accountService.createAccount(
                event.getPolicyId().toString(),
                event.getPolicyNo(),
                event.getCoverageStart(),
                event.getPremium(),
                paymentFrequency
        );

        log.info("Monolithic account automatically created for PolicyNo: {}", event.getPolicyNo());
    }
}
