package com.insurance.claim.core.listener;

import com.insurance.claim.core.entity.Claim;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("claim_ClaimEventListener")
public class ClaimEventListener {

  private final Sequences sequences;

  public ClaimEventListener(Sequences sequences) {
    this.sequences = sequences;
  }

  @EventListener
  public void onClaimSaving(final EntitySavingEvent<Claim> event) {
    Claim claim = event.getEntity();

    if (!StringUtils.hasText(claim.getClaimNo())) {
      long nextVal = sequences.createNextValue(Sequence.withName("claim_number_sequence"));
      int year = claim.getReportDate() != null ? claim.getReportDate().getYear() : java.time.LocalDate.now().getYear();
      claim.setClaimNo("CLM-" + year + "-" + String.format("%06d", nextVal));
    }
  }
}
