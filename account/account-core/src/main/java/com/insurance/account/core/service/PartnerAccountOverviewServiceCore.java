package com.insurance.account.core.service;

import com.insurance.account.api.dto.PartnerAccountSummaryDto;
import com.insurance.account.api.service.PartnerAccountOverviewService;
import com.insurance.account.core.entity.Account;
import io.jmix.core.DataManager;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("account_PartnerAccountOverviewService")
public class PartnerAccountOverviewServiceCore implements PartnerAccountOverviewService {

  private final DataManager dataManager;

  public PartnerAccountOverviewServiceCore(DataManager dataManager) {
    this.dataManager = dataManager;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PartnerAccountSummaryDto> findAccountSummaryForPartner(String partnerNo) {
    if (partnerNo == null || partnerNo.isBlank()) {
      return Optional.empty();
    }

    return dataManager
        .load(Account.class)
        .query(
            "select e from account_Account e "
                + "where e.policy.partnerNo = :partnerNo "
                + "order by e.policy.policyNo")
        .parameter("partnerNo", partnerNo)
        .optional()
        .map(this::mapToSummary);
  }

  private PartnerAccountSummaryDto mapToSummary(Account account) {
    PartnerAccountSummaryDto summary = dataManager.create(PartnerAccountSummaryDto.class);
    summary.setAccountNo(account.getAccountNo());
    summary.setAccountBalance(account.getAccountBalance());
    return summary;
  }
}
