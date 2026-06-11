package com.insurance.claim.core.test_support;

import com.insurance.claim.api.dto.ClaimStatus;
import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.ClaimPolicyReference;
import com.insurance.common.test_support.TestDataProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ClaimDataProvider implements TestDataProvider<Claim> {

  @Override
  public Class<Claim> getEntityClass() {
    return Claim.class;
  }

  @Override
  public void accept(Claim claim) {
    ClaimPolicyReference policyRef = claim.getPolicy();
    if (policyRef != null) {
      policyRef.setPolicyId(UUID.randomUUID());
      policyRef.setPolicyNo("HC-" + UUID.randomUUID().toString().substring(0, 8));
    }
    claim.setReportDate(LocalDate.of(2026, 1, 15));
    claim.setIncidentDate(LocalDate.of(2026, 1, 10));
    claim.setClaimStatus(ClaimStatus.OPEN);
    claim.setDescription("Test claim");
    claim.setExpectedClaimAmount(new BigDecimal("1500.00"));
  }
}
