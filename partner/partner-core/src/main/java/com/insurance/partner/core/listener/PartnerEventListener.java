package com.insurance.partner.core.listener;

import com.insurance.partner.core.entity.Partner;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("partner_PartnerEventListener")
public class PartnerEventListener {

    private final Sequences sequences;

    public PartnerEventListener(Sequences sequences) {
        this.sequences = sequences;
    }

    @EventListener
    public void onPartnerSaving(final EntitySavingEvent<Partner> event) {
        Partner partner = event.getEntity();
        
        // Generate unique partnerNo using Jmix Sequences if not present yet
        if (partner.getPartnerNo() == null || partner.getPartnerNo().trim().isEmpty()) {
            long nextVal = sequences.createNextValue(Sequence.withName("partner_number_sequence"));
            partner.setPartnerNo("PT-" + String.format("%05d", nextVal));
        }
    }
}
