package com.insurance.claim.core.listener;

import com.insurance.claim.core.entity.Claim;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("claim_ClaimEntityListener")
public class ClaimEntityListener {

  private final Sequences sequences;

  public ClaimEntityListener(Sequences sequences) {
    this.sequences = sequences;
  }

  @EventListener
  public void onClaimSaving(final EntitySavingEvent<Claim> event) {
    Claim claim = event.getEntity();

    if (!StringUtils.hasText(claim.getClaimNo())) {
      long nextVal = sequences.createNextValue(Sequence.withName("claim_number_sequence"));
      claim.setClaimNo("CL-" + String.format("%05d", nextVal));
    }
  }
}
