package com.insurance.app.test_support.assertion;

import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.core.entity.Quote;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;

public class QuoteAssert extends AbstractObjectAssert<QuoteAssert, Quote> {

    public QuoteAssert(Quote actual) {
        super(actual, QuoteAssert.class);
    }

    public static QuoteAssert assertThat(Quote actual) {
        return new QuoteAssert(actual);
    }

    public QuoteAssert isAccepted() {
        isNotNull();
        Assertions.assertThat(actual.getStatus())
                .as("quote should be ACCEPTED")
                .isEqualTo(QuoteStatus.ACCEPTED);
        Assertions.assertThat(actual.getAcceptedAt())
                .as("acceptedAt should be set when accepted")
                .isNotNull();
        return this;
    }

    public QuoteAssert isRejected() {
        isNotNull();
        Assertions.assertThat(actual.getStatus())
                .as("quote should be REJECTED")
                .isEqualTo(QuoteStatus.REJECTED);
        Assertions.assertThat(actual.getRejectedAt())
                .as("rejectedAt should be set when rejected")
                .isNotNull();
        return this;
    }

    public QuoteAssert hasPolicyReference() {
        isNotNull();
        Assertions.assertThat(actual.getCreatedPolicyNo())
                .as("createdPolicyNo should be set after acceptance")
                .isNotNull();
        Assertions.assertThat(actual.getCreatedPolicyId())
                .as("createdPolicyId should be set after acceptance")
                .isNotNull();
        return this;
    }

    public QuoteAssert hasPolicyNo(String policyNo) {
        isNotNull();
        Assertions.assertThat(actual.getCreatedPolicyNo())
                .as("createdPolicyNo")
                .isEqualTo(policyNo);
        return this;
    }
}
