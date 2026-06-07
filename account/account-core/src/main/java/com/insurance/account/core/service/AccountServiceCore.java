package com.insurance.account.core.service;

import com.insurance.account.api.service.AccountService;
import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.account.core.entity.AccountPolicyReference;
import com.insurance.account.core.entity.DocumentType;
import com.insurance.product.api.dto.PaymentFrequency;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.SaveContext;
import io.jmix.core.querycondition.PropertyCondition;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("account_AccountService")
public class AccountServiceCore implements AccountService {

  private static final Logger log = LoggerFactory.getLogger(AccountServiceCore.class);
  private static final String MDC_POLICY_NO = "policyNo";

  private final DataManager dataManager;

  public AccountServiceCore(DataManager dataManager) {
    this.dataManager = dataManager;
  }

  @Transactional
  public Account createAccount(
      java.util.UUID policyId,
      String policyNo,
      String partnerNo,
      LocalDate coverageStart,
      LocalDate coverageEnd,
      BigDecimal premium,
      PaymentFrequency paymentFrequency) {
    AccountPolicyReference policyReference = dataManager.create(AccountPolicyReference.class);
    policyReference.setPolicyId(policyId);
    policyReference.setPolicyNo(policyNo);
    policyReference.setPartnerNo(partnerNo);

    return createAccount(policyReference, coverageStart, coverageEnd, premium, paymentFrequency);
  }

  @Transactional
  public Account createAccount(
      AccountPolicyReference policy,
      LocalDate accountingPeriodStart,
      LocalDate accountingPeriodEnd,
      BigDecimal premium,
      PaymentFrequency paymentFrequency) {
    if (policy == null
        || policy.getPolicyId() == null
        || policy.getPolicyNo() == null
        || accountingPeriodStart == null
        || accountingPeriodEnd == null
        || premium == null
        || paymentFrequency == null) {
      throw new IllegalArgumentException(
          "Policy reference, accounting period, premium and payment frequency are required");
    }

    String previousPolicyNo = replaceMdc(MDC_POLICY_NO, policy.getPolicyNo());

    try {
      log.info(
          "account.create.started policyNo={} partnerNo={}",
          policy.getPolicyNo(),
          policy.getPartnerNo());
      Account account = dataManager.create(Account.class);

      SaveContext saveContext = new SaveContext();

      account.setPolicy(policy);
      account.setAccountingPeriodStart(accountingPeriodStart);
      account.setAccountingPeriodEnd(accountingPeriodEnd);
      account.setAccountBalance(premium.negate());

      int payments = paymentFrequency.getFrequency();
      BigDecimal documentAmount = premium.divide(new BigDecimal(payments), RoundingMode.HALF_UP);

      saveContext.saving(account);

      int intervalMonths;
      switch (payments) {
        case 1:
          intervalMonths = 0;
          break;
        case 4:
          intervalMonths = 3;
          break;
        case 12:
          intervalMonths = 1;
          break;
        default:
          intervalMonths = 12 / payments;
          break;
      }

      for (int i = 0; i < payments; i++) {
        LocalDate documentDate = accountingPeriodStart.plusMonths((long) i * intervalMonths);
        AccountDocument accountDocument = dataManager.create(AccountDocument.class);
        accountDocument.setAccount(account);
        accountDocument.setAmount(documentAmount.negate());
        accountDocument.setDescription("Payment for " + documentDate);
        accountDocument.setType(DocumentType.CREDIT);
        accountDocument.setDocumentDate(documentDate);
        saveContext.saving(accountDocument);
      }

      Account savedAccount = dataManager.save(saveContext).get(account);
      log.info(
          "account.created policyNo={} accountNo={} documents={}",
          policy.getPolicyNo(),
          savedAccount.getAccountNo(),
          payments);
      return savedAccount;
    } catch (RuntimeException e) {
      log.error(
          "account.create.failed policyNo={} partnerNo={}",
          policy.getPolicyNo(),
          policy.getPartnerNo(),
          e);
      throw e;
    } finally {
      restoreMdc(MDC_POLICY_NO, previousPolicyNo);
    }
  }

  @Override
  public BigDecimal getAccountBalance(String accountNo, LocalDate effectiveDate) {
    log.info(
        "Calculating balance for accountNo {} as of effectiveDate {}", accountNo, effectiveDate);
    Optional<Account> potentialAccount =
        dataManager
            .load(Account.class)
            .condition(PropertyCondition.equal("policy.policyNo", accountNo))
            .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("documents", FetchPlan.BASE))
            .optional();

    if (potentialAccount.isEmpty()) {
      log.warn("Account with accountNo {} not found.", accountNo);
      return BigDecimal.ZERO;
    }

    Account account = potentialAccount.get();

    if (account.getAccountingPeriodEnd().isBefore(effectiveDate)) {
      throw new IllegalArgumentException(
          "Account balance calculation not possible. Coverage end is before effectiveDate "
              + effectiveDate);
    }

    BigDecimal computedBalance =
        account.getDocuments().stream()
            .filter(doc -> !doc.getDocumentDate().isAfter(effectiveDate))
            .map(AccountDocument::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.debug("Computed balance for accountNo {}: {}", accountNo, computedBalance);
    return computedBalance;
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
