package com.insurance.app.claim;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.app.test_support.DatabaseCleanup;
import com.insurance.claim.api.dto.ClaimStatus;
import com.insurance.claim.api.dto.ReserveType;
import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.Reserve;
import com.insurance.claim.core.service.ClaimService;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.querycondition.PropertyCondition;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ClaimServiceTest extends BaseIntegrationTest {

  @Autowired private ClaimService claimService;

  @Autowired private DataManager dataManager;

  @Autowired private DatabaseCleanup databaseCleanup;

  private static final UUID POLICY_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
  private static final String POLICY_NO = "HC-2025-000099";
  private static final String PARTNER_NO = "PT-00001";
  private static final LocalDate DATE_OF_LOSS = LocalDate.of(2025, 6, 15);
  private static final BigDecimal ESTIMATED_AMOUNT = new BigDecimal("5000.00");

  @BeforeEach
  void setUp() {
    databaseCleanup.removeAllEntities();
  }

  @Test
  void given_newClaim_when_created_then_claimNoIsGenerated() {
    Claim claim =
        claimService.createClaim(
            POLICY_ID, POLICY_NO, PARTNER_NO, DATE_OF_LOSS, "Water damage", ESTIMATED_AMOUNT);

    assertThat(claim.getClaimNo()).startsWith("CL-");
  }

  @Test
  void given_newClaim_when_created_then_statusIsOpen() {
    claimService.createClaim(
        POLICY_ID, POLICY_NO, PARTNER_NO, DATE_OF_LOSS, "Water damage", ESTIMATED_AMOUNT);

    Claim loaded = loadClaimWithReserves();
    assertThat(loaded.getStatus()).isEqualTo(ClaimStatus.OPEN);
  }

  @Test
  void given_newClaim_when_created_then_initialCompensationReserveExists() {
    claimService.createClaim(
        POLICY_ID, POLICY_NO, PARTNER_NO, DATE_OF_LOSS, "Water damage", ESTIMATED_AMOUNT);

    Claim loaded = loadClaimWithReserves();
    List<Reserve> reserves = loaded.getReserves();

    assertThat(reserves).hasSize(1);
    assertThat(reserves.get(0).getType()).isEqualTo(ReserveType.COMPENSATION);
    assertThat(reserves.get(0).getAmount()).isEqualByComparingTo(ESTIMATED_AMOUNT);
    assertThat(reserves.get(0).getComment()).isEqualTo("Initial reserve");
  }

  @Test
  void given_newClaim_when_created_then_policyReferenceIsStored() {
    claimService.createClaim(
        POLICY_ID, POLICY_NO, PARTNER_NO, DATE_OF_LOSS, "Water damage", ESTIMATED_AMOUNT);

    Claim loaded = loadClaimWithReserves();
    assertThat(loaded.getPolicy().getPolicyId()).isEqualTo(POLICY_ID);
    assertThat(loaded.getPolicy().getPolicyNo()).isEqualTo(POLICY_NO);
    assertThat(loaded.getPolicy().getPartnerNo()).isEqualTo(PARTNER_NO);
  }

  private Claim loadClaimWithReserves() {
    return dataManager
        .load(Claim.class)
        .condition(PropertyCondition.equal("policy.policyNo", POLICY_NO))
        .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("reserves", FetchPlan.BASE))
        .one();
  }
}
