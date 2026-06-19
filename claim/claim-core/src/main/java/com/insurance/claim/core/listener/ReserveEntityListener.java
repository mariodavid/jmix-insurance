package com.insurance.claim.core.listener;

import com.insurance.claim.api.dto.ReserveStatus;
import com.insurance.claim.core.entity.Reserve;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("claim_ReserveEntityListener")
public class ReserveEntityListener {

  @EventListener
  public void onReserveSaving(final EntitySavingEvent<Reserve> event) {
    Reserve reserve = event.getEntity();

    if (reserve.getStatus() == null) {
      reserve.setStatus(ReserveStatus.PENDING);
    }
  }
}
