package com.insurance.account.core.listener;

import com.insurance.account.core.service.AccountServiceCore;
import com.insurance.policy.api.event.PolicyCreatedEvent;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.security.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
    log.info("Synchronous PolicyCreatedEvent received for policyId: {}", event.getPolicyId());

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
          event.getCoverageStart(),
          event.getPremium(),
          paymentFrequency);
      log.info("Monolithic account automatically created for PolicyNo: {}", event.getPolicyNo());
    } catch (RuntimeException e) {
      log.error(
          "Account creation failed for PolicyNo: {}, rolling back transaction",
          event.getPolicyNo(),
          e);
      throw e;
    } catch (Exception e) {
      log.error(
          "Account creation failed for PolicyNo: {}, rolling back transaction",
          event.getPolicyNo(),
          e);
      throw new RuntimeException("Account creation failed for policy: " + event.getPolicyNo(), e);
    }
  }
}
