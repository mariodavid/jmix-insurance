package com.insurance.account.core.listener;

import com.insurance.account.core.service.AccountServiceCore;
import com.insurance.policy.api.event.PolicyCreatedEvent;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.security.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SuppressWarnings("PMD.GuardLogStatement")
@Component("account_PolicyCreatedEventListener")
public class PolicyCreatedEventListener {

  private static final Logger log = LoggerFactory.getLogger(PolicyCreatedEventListener.class);
  private static final String MDC_POLICY_NO = "policyNo";

  private final AccountServiceCore accountService;

  public PolicyCreatedEventListener(AccountServiceCore accountService) {
    this.accountService = accountService;
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  @EventListener
  @Authenticated
  public void onPolicyCreated(final PolicyCreatedEvent event) {
    String previousPolicyNo = replaceMdc(MDC_POLICY_NO, event.getPolicyNo());

    try {
      log.info("policy.created.event-received policyNo={}", event.getPolicyNo());

      PaymentFrequency paymentFrequency = PaymentFrequency.fromId(event.getPaymentFrequencyId());
      if (paymentFrequency == null) {
        throw new IllegalArgumentException(
            "Policy has unknown payment frequency "
                + event.getPaymentFrequencyId()
                + ". Account creation aborted.");
      }

      try {
        accountService.createAccount(
            event.getPolicyId(),
            event.getPolicyNo(),
            event.getPartnerNo(),
            event.getCoverageStart(),
            event.getCoverageEnd(),
            event.getPremium(),
            paymentFrequency);
        log.info("policy.created.account-created policyNo={}", event.getPolicyNo());
      } catch (RuntimeException e) {
        log.error("policy.created.account-failed policyNo={}", event.getPolicyNo(), e);
        throw e;
      } catch (Exception e) {
        log.error("policy.created.account-failed policyNo={}", event.getPolicyNo(), e);
        throw new IllegalStateException(
            "Account creation failed for policy: " + event.getPolicyNo(), e);
      }
    } finally {
      restoreMdc(MDC_POLICY_NO, previousPolicyNo);
    }
  }

  private String replaceMdc(String key, String value) {
    String previousValue = MDC.get(key);
    if (value == null) {
      MDC.remove(key);
    } else {
      MDC.put(key, value);
    }
    return previousValue;
  }

  private void restoreMdc(String key, String previousValue) {
    if (previousValue == null) {
      MDC.remove(key);
    } else {
      MDC.put(key, previousValue);
    }
  }
}
