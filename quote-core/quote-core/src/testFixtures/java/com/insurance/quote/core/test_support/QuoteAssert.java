package com.insurance.quote.core.test_support;

import com.insurance.product.api.dto.InsuranceProduct;
import com.insurance.product.api.dto.PaymentFrequency;
import com.insurance.quote.api.dto.QuoteStatus;
import com.insurance.quote.core.entity.Quote;
import org.assertj.core.api.AbstractAssert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class QuoteAssert extends AbstractAssert<QuoteAssert, Quote> {

    public QuoteAssert(Quote actual) {
        super(actual, QuoteAssert.class);
    }

    public QuoteAssert hasStatus(QuoteStatus expected) {
        isNotNull();
        if (!Objects.equals(actual.getStatus(), expected)) {
            failWithMessage("Expected quote status to be <%s> but was <%s>", expected, actual.getStatus());
        }
        return this;
    }

    public QuoteAssert hasPartnerNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getPartnerNo(), expected)) {
            failWithMessage("Expected quote partnerNo to be <%s> but was <%s>", expected, actual.getPartnerNo());
        }
        return this;
    }

    public QuoteAssert hasQuoteNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getQuoteNo(), expected)) {
            failWithMessage("Expected quote quoteNo to be <%s> but was <%s>", expected, actual.getQuoteNo());
        }
        return this;
    }

    public QuoteAssert hasCalculatedPremium(BigDecimal expected) {
        isNotNull();
        if (actual.getCalculatedPremium() == null || actual.getCalculatedPremium().compareTo(expected) != 0) {
            failWithMessage("Expected quote calculatedPremium to be <%s> but was <%s>", expected, actual.getCalculatedPremium());
        }
        return this;
    }

    public QuoteAssert hasInsuranceProduct(InsuranceProduct expected) {
        isNotNull();
        if (!Objects.equals(actual.getInsuranceProduct(), expected)) {
            failWithMessage("Expected quote insuranceProduct to be <%s> but was <%s>", expected, actual.getInsuranceProduct());
        }
        return this;
    }

    public QuoteAssert hasPaymentFrequency(PaymentFrequency expected) {
        isNotNull();
        if (!Objects.equals(actual.getPaymentFrequency(), expected)) {
            failWithMessage("Expected quote paymentFrequency to be <%s> but was <%s>", expected, actual.getPaymentFrequency());
        }
        return this;
    }

    public QuoteAssert hasEffectiveDate(LocalDate expected) {
        isNotNull();
        if (!Objects.equals(actual.getEffectiveDate(), expected)) {
            failWithMessage("Expected quote effectiveDate to be <%s> but was <%s>", expected, actual.getEffectiveDate());
        }
        return this;
    }

    public QuoteAssert hasCreatedPolicyId(java.util.UUID expected) {
        isNotNull();
        if (!Objects.equals(actual.getCreatedPolicyId(), expected)) {
            failWithMessage("Expected quote createdPolicyId to be <%s> but was <%s>", expected, actual.getCreatedPolicyId());
        }
        return this;
    }

    public QuoteAssert hasCreatedPolicyNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getCreatedPolicyNo(), expected)) {
            failWithMessage("Expected quote createdPolicyNo to be <%s> but was <%s>", expected, actual.getCreatedPolicyNo());
        }
        return this;
    }

    public QuoteAssert isAccepted() {
        isNotNull();
        if (actual.getStatus() != com.insurance.quote.api.dto.QuoteStatus.ACCEPTED) {
            failWithMessage("Expected quote to be ACCEPTED but status was <%s>", actual.getStatus());
        }
        if (actual.getAcceptedAt() == null) {
            failWithMessage("Expected quote to have acceptedAt set but was null");
        }
        return this;
    }

    public QuoteAssert isRejected() {
        isNotNull();
        if (actual.getStatus() != com.insurance.quote.api.dto.QuoteStatus.REJECTED) {
            failWithMessage("Expected quote to be REJECTED but status was <%s>", actual.getStatus());
        }
        if (actual.getRejectedAt() == null) {
            failWithMessage("Expected quote to have rejectedAt set but was null");
        }
        return this;
    }

    public QuoteAssert hasPolicyReference() {
        isNotNull();
        if (actual.getCreatedPolicyNo() == null || actual.getCreatedPolicyId() == null) {
            failWithMessage("Expected quote to have policy reference but createdPolicyNo=<%s> createdPolicyId=<%s>",
                    actual.getCreatedPolicyNo(), actual.getCreatedPolicyId());
        }
        return this;
    }
}
