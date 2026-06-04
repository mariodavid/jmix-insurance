package com.insurance.quote.api.dto;

public class Assertions extends org.assertj.core.api.Assertions {

  public static QuoteDtoAssert assertThat(QuoteDto actual) {
    return new QuoteDtoAssert(actual);
  }

  protected Assertions() {}
}
