package com.insurance.claim.core.service;

import com.insurance.claim.api.dto.ReserveType;
import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.ClaimReserve;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("claim_ClaimService")
public class ClaimService {

  private final DataManager dataManager;

  public ClaimService(DataManager dataManager) {
    this.dataManager = dataManager;
  }

  @Transactional
  public Set<Object> saveClaim(Claim claim) {
    SaveContext saveContext = new SaveContext();
    saveContext.saving(claim);

    if (isNewClaimWithExpectedAmount(claim)) {
      ClaimReserve reserve = createInitialIndemnityReserve(claim);
      saveContext.saving(reserve);
    }

    return dataManager.save(saveContext);
  }

  private boolean isNewClaimWithExpectedAmount(Claim claim) {
    BigDecimal expectedAmount = claim.getExpectedClaimAmount();
    if (expectedAmount == null || expectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
      return false;
    }

    if (claim.getReserves() != null) {
      boolean hasIndemnity = claim.getReserves().stream()
          .anyMatch(r -> ReserveType.INDEMNITY.equals(r.getReserveType()));
      if (hasIndemnity) {
        return false;
      }
    }

    return claim.getVersion() == null;
  }

  private ClaimReserve createInitialIndemnityReserve(Claim claim) {
    ClaimReserve reserve = dataManager.create(ClaimReserve.class);
    reserve.setClaim(claim);
    reserve.setReserveType(ReserveType.INDEMNITY);
    reserve.setReserveAmount(claim.getExpectedClaimAmount());
    reserve.setReason("Initial indemnity reserve");
    return reserve;
  }
}
