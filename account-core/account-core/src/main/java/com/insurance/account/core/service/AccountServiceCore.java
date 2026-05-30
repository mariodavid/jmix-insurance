package com.insurance.account.core.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.account.api.service.AccountService;
import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.account.core.entity.DocumentType;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.product.api.dto.PaymentFrequency;

import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.querycondition.PropertyCondition;

@Service("account_AccountService")
public class AccountServiceCore implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceCore.class);

    private final DataManager dataManager;
    private final PolicyService policyService;

    public AccountServiceCore(DataManager dataManager, PolicyService policyService) {
        this.dataManager = dataManager;
        this.policyService = policyService;
    }

    @Transactional
    public Account createAccount(String policyId, String accountNo, LocalDate coverageStart, BigDecimal premium, PaymentFrequency paymentFrequency) {
        log.info("Creating account {}", accountNo);
        Account account = dataManager.create(Account.class);

        SaveContext saveContext = new SaveContext();

        account.setPolicyId(policyId);
        account.setAccountNo(accountNo);
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
            LocalDate documentDate = coverageStart.plusMonths((long) i * intervalMonths);
            AccountDocument accountDocument = dataManager.create(AccountDocument.class);
            accountDocument.setAccount(account);
            accountDocument.setAmount(documentAmount.negate());
            accountDocument.setDescription("Payment for " + documentDate);
            accountDocument.setType(DocumentType.CREDIT);
            accountDocument.setDocumentDate(documentDate);
            saveContext.saving(accountDocument);
        }

        Account savedAccount = dataManager.save(saveContext).get(account);
        log.info("Account {} created successfully with balance {}", accountNo, savedAccount.getAccountBalance());
        return savedAccount;
    }

    @Override
    public BigDecimal getAccountBalance(String accountNo, LocalDate effectiveDate) {
        log.info("Calculating balance for accountNo {} as of effectiveDate {}", accountNo, effectiveDate);
        Optional<Account> potentialAccount = dataManager.load(Account.class)
                .condition(PropertyCondition.equal("accountNo", accountNo))
                .optional();

        if (potentialAccount.isEmpty()) {
            log.warn("Account with accountNo {} not found.", accountNo);
            return BigDecimal.ZERO;
        }

        Account account = potentialAccount.get();
        PolicyDto policyDto = policyService.findPolicyById(account.getPolicyId());
        
        if (policyDto != null && policyDto.getCoverageEnd().isBefore(effectiveDate)) {
            throw new IllegalArgumentException("Account balance calculation not possible. Coverage end is before effectiveDate " + effectiveDate);
        }

        BigDecimal computedBalance = account.getDocuments().stream()
                .filter(doc -> !doc.getDocumentDate().isAfter(effectiveDate))
                .map(AccountDocument::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Computed balance for accountNo {}: {}", accountNo, computedBalance);
        return computedBalance;
    }
}
