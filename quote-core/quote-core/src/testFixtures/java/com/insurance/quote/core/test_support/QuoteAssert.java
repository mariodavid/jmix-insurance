package com.insurance.quote.core.test_support;

import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.core.entity.Quote;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class QuoteAssert extends AbstractAssert<QuoteAssert, Quote> {

    public QuoteAssert(Quote actual) {
        super(actual, QuoteAssert.class);
    }

    public QuoteAssert hasPartnerNo(String partnerNo) {
        isNotNull();
        if (!Objects.equals(actual.getPartnerNo(), partnerNo)) {
            failWithMessage("Expected quote partnerNo to be <%s> but was <%s>", partnerNo, actual.getPartnerNo());
        }
        return this;
    }

    public QuoteAssert hasStatus(QuoteStatus status) {
        isNotNull();
        if (!Objects.equals(actual.getStatus(), status)) {
            failWithMessage("Expected quote status to be <%s> but was <%s>", status, actual.getStatus());
        }
        return this;
    }

    public QuoteAssert hasCreatedPolicyId(String createdPolicyId) {
        isNotNull();
        if (!Objects.equals(actual.getCreatedPolicyId(), createdPolicyId)) {
            failWithMessage("Expected quote createdPolicyId to be <%s> but was <%s>", createdPolicyId, actual.getCreatedPolicyId());
        }
        return this;
    }

    public QuoteAssert hasCreatedPolicyNo(String createdPolicyNo) {
        isNotNull();
        if (!Objects.equals(actual.getCreatedPolicyNo(), createdPolicyNo)) {
            failWithMessage("Expected quote createdPolicyNo to be <%s> but was <%s>", createdPolicyNo, actual.getCreatedPolicyNo());
        }
        return this;
    }
}
