package com.insurance.quote.api.dto;

import org.assertj.core.api.AbstractAssert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class QuoteDtoAssert extends AbstractAssert<QuoteDtoAssert, QuoteDto> {

    public QuoteDtoAssert(QuoteDto actual) {
        super(actual, QuoteDtoAssert.class);
    }

    public static QuoteDtoAssert assertThat(QuoteDto actual) {
        return new QuoteDtoAssert(actual);
    }

    public QuoteDtoAssert hasId(UUID expected) {
        isNotNull();
        if (!Objects.equals(actual.getId(), expected)) {
            failWithMessage("Expected QuoteDto id to be <%s> but was <%s>", expected, actual.getId());
        }
        return this;
    }

    public QuoteDtoAssert hasQuoteNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getQuoteNo(), expected)) {
            failWithMessage("Expected QuoteDto quoteNo to be <%s> but was <%s>", expected, actual.getQuoteNo());
        }
        return this;
    }

    public QuoteDtoAssert hasPartnerNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getPartnerNo(), expected)) {
            failWithMessage("Expected QuoteDto partnerNo to be <%s> but was <%s>", expected, actual.getPartnerNo());
        }
        return this;
    }

    public QuoteDtoAssert hasStatus(String expected) {
        isNotNull();
        QuoteStatus status = actual.getStatus();
        String actualId = status == null ? null : status.getId();
        if (!Objects.equals(actualId, expected)) {
            failWithMessage("Expected QuoteDto status to be <%s> but was <%s>", expected, actualId);
        }
        return this;
    }

    public QuoteDtoAssert hasCalculatedPremium(BigDecimal expected) {
        isNotNull();
        if (actual.getCalculatedPremium() == null || actual.getCalculatedPremium().compareTo(expected) != 0) {
            failWithMessage("Expected QuoteDto calculatedPremium to be <%s> but was <%s>", expected, actual.getCalculatedPremium());
        }
        return this;
    }

    public QuoteDtoAssert hasEffectiveDate(LocalDate expected) {
        isNotNull();
        if (!Objects.equals(actual.getEffectiveDate(), expected)) {
            failWithMessage("Expected QuoteDto effectiveDate to be <%s> but was <%s>", expected, actual.getEffectiveDate());
        }
        return this;
    }

    public QuoteDtoAssert hasCreatedPolicyNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getCreatedPolicyNo(), expected)) {
            failWithMessage("Expected QuoteDto createdPolicyNo to be <%s> but was <%s>", expected, actual.getCreatedPolicyNo());
        }
        return this;
    }

    public QuoteDtoAssert hasCreatedPolicyId(UUID expected) {
        isNotNull();
        if (!Objects.equals(actual.getCreatedPolicyId(), expected)) {
            failWithMessage("Expected QuoteDto createdPolicyId to be <%s> but was <%s>", expected, actual.getCreatedPolicyId());
        }
        return this;
    }
}
