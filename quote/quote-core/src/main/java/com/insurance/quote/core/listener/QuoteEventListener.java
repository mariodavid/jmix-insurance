package com.insurance.quote.core.listener;

import com.insurance.quote.core.entity.Quote;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("quote_QuoteEventListener")
public class QuoteEventListener {

  private final Sequences sequences;

  public QuoteEventListener(Sequences sequences) {
    this.sequences = sequences;
  }

  @EventListener
  public void onQuoteSaving(final EntitySavingEvent<Quote> event) {
    Quote quote = event.getEntity();

    // Generate unique quoteNo using Jmix Sequences if not present yet
    if (quote.getQuoteNo() == null || quote.getQuoteNo().trim().isEmpty()) {
      long nextVal = sequences.createNextValue(Sequence.withName("quote_number_sequence"));
      quote.setQuoteNo("QT-" + String.format("%05d", nextVal));
    }
  }
}
