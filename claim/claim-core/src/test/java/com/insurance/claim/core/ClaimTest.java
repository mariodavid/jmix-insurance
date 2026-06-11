package com.insurance.claim.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.claim.api.dto.ClaimStatus;
import com.insurance.claim.api.dto.ReserveType;
import com.insurance.claim.core.entity.Claim;
import com.insurance.claim.core.entity.ClaimReserve;
import com.insurance.claim.core.service.ClaimService;
import com.insurance.claim.core.test_support.ClaimDataProvider;
import com.insurance.common.test_support.AuthenticatedAsAdmin;
import com.insurance.common.test_support.EntityTestData;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ExtendWith(AuthenticatedAsAdmin.class)
class ClaimTest {

  @Autowired private DataManager dataManager;

  @Autowired private EntityTestData entityTestData;

  @Autowired private ClaimService claimService;

  @Autowired private DataSource dataSource;

  @Test
  void contextLoads() {}

  @BeforeEach
  void setUp() {
    deleteAllClaims();
  }

  @AfterEach
  void tearDown() {
    deleteAllClaims();
  }

  @Test
  void given_newClaim_when_saved_then_claimNoIsGenerated() {
    Claim claim = entityTestData.createWithDefaults(new ClaimDataProvider());
    claimService.saveClaim(claim);

    Claim loaded = dataManager.load(Claim.class).id(claim.getId()).one();

    assertThat(loaded.getClaimNo()).isNotNull();
    assertThat(loaded.getClaimNo()).startsWith("CLM-2026-");
  }

  @Test
  void given_claimWithExpectedAmount_when_saved_then_initialIndemnityReserveIsCreated() {
    Claim claim = entityTestData.createWithDefaults(new ClaimDataProvider());
    claimService.saveClaim(claim);

    Claim loaded = dataManager.load(Claim.class)
        .id(claim.getId())
        .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("reserves", FetchPlan.BASE))
        .one();

    assertThat(loaded.getReserves()).hasSize(1);

    ClaimReserve reserve = loaded.getReserves().get(0);
    assertThat(reserve.getReserveType()).isEqualTo(ReserveType.INDEMNITY);
    assertThat(reserve.getReserveAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
    assertThat(reserve.getReason()).isEqualTo("Initial indemnity reserve");
  }

  @Test
  void given_claimWithoutExpectedAmount_when_saved_then_noReserveIsCreated() {
    Claim claim = entityTestData.createWithDefaults(new ClaimDataProvider(), c ->
        c.setExpectedClaimAmount(null));
    claimService.saveClaim(claim);

    Claim loaded = dataManager.load(Claim.class)
        .id(claim.getId())
        .fetchPlan(fp -> fp.addFetchPlan(FetchPlan.BASE).add("reserves", FetchPlan.BASE))
        .one();

    assertThat(loaded.getReserves()).isEmpty();
  }

  @Test
  void given_claim_when_savedAndReloaded_then_auditFieldsAreSet() {
    Claim claim = entityTestData.createWithDefaults(new ClaimDataProvider());
    claimService.saveClaim(claim);

    Claim loaded = dataManager.load(Claim.class).id(claim.getId()).one();

    assertThat(loaded.getCreatedDate()).isNotNull();
    assertThat(loaded.getCreatedBy()).isNotNull();
    assertThat(loaded.getVersion()).isEqualTo(1);
    assertThat(loaded.getClaimStatus()).isEqualTo(ClaimStatus.OPEN);
  }

  private void deleteAllClaims() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    jdbc.update("DELETE FROM CLAIM_CLAIM_RESERVE");
    jdbc.update("DELETE FROM CLAIM_CLAIM");
  }
}
