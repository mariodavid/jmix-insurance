package com.insurance.policy.core.service;

import com.insurance.policy.api.dto.PartnerPolicySummaryDto;
import com.insurance.policy.api.service.PartnerPolicyOverviewService;
import com.insurance.policy.core.entity.Policy;
import io.jmix.core.DataManager;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("policy_PartnerPolicyOverviewService")
public class PartnerPolicyOverviewServiceCore implements PartnerPolicyOverviewService {

  private final DataManager dataManager;

  public PartnerPolicyOverviewServiceCore(DataManager dataManager) {
    this.dataManager = dataManager;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartnerPolicySummaryDto> findPoliciesForPartner(String partnerNo) {
    if (partnerNo == null || partnerNo.isBlank()) {
      return List.of();
    }

    return dataManager
        .load(Policy.class)
        .query(
            "select e from policy_Policy e "
                + "where e.partner.partnerNo = :partnerNo "
                + "order by e.coverageEnd desc, e.policyNo")
        .parameter("partnerNo", partnerNo)
        .list()
        .stream()
        .map(this::mapToSummary)
        .toList();
  }

  private PartnerPolicySummaryDto mapToSummary(Policy policy) {
    PartnerPolicySummaryDto summary = dataManager.create(PartnerPolicySummaryDto.class);
    summary.setPolicyNo(policy.getPolicyNo());
    summary.setCoverageEnd(policy.getCoverageEnd());
    return summary;
  }
}
