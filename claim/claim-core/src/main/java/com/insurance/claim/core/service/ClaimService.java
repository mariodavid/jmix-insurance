package com.insurance.claim.core.service;

import com.insurance.claim.api.dto.ClaimStatus;
import com.insurance.claim.api.dto.ReserveType;
import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.ClaimPolicyReference;
import com.insurance.claim.core.entity.Reserve;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service("claim_ClaimService")
public class ClaimService {

  private final DataManager dataManager;

  public ClaimService(DataManager dataManager) {
    this.dataManager = dataManager;
  }

  public Claim createClaim(
      UUID policyId,
      String policyNo,
      String partnerNo,
      LocalDate dateOfLoss,
      String description,
      BigDecimal estimatedAmount) {

    Claim claim = dataManager.create(Claim.class);
    claim.setStatus(ClaimStatus.OPEN);
    claim.setDateOfLoss(dateOfLoss);
    claim.setDescription(description);
    claim.setEstimatedAmount(estimatedAmount);

    ClaimPolicyReference policyRef = dataManager.create(ClaimPolicyReference.class);
    policyRef.setPolicyId(policyId);
    policyRef.setPolicyNo(policyNo);
    policyRef.setPartnerNo(partnerNo);
    claim.setPolicy(policyRef);

    Reserve initialReserve = dataManager.create(Reserve.class);
    initialReserve.setClaim(claim);
    initialReserve.setType(ReserveType.COMPENSATION);
    initialReserve.setAmount(estimatedAmount);
    initialReserve.setComment("Initial reserve");

    SaveContext saveContext = new SaveContext();
    saveContext.saving(claim);
    saveContext.saving(initialReserve);
    dataManager.save(saveContext);

    return claim;
  }
}
