package com.insurance.quote.core.test_support;

import com.insurance.quote.core.entity.Quote;

public class Assertions extends org.assertj.core.api.Assertions {

    public static QuoteAssert assertThat(Quote actual) {
        return new QuoteAssert(actual);
    }

    protected Assertions() {
    }
}
