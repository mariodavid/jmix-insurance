package com.insurance.app.test_support.assertion;

import com.insurance.account.core.entity.Account;
import com.insurance.partner.core.entity.Partner;
import com.insurance.policy.core.entity.Policy;
import com.insurance.quote.core.entity.Quote;

public class InsuranceAssertions extends org.assertj.core.api.Assertions {

    public static PartnerAssert assertThat(Partner actual) {
        return new PartnerAssert(actual);
    }

    public static PolicyAssert assertThat(Policy actual) {
        return new PolicyAssert(actual);
    }

    public static QuoteAssert assertThat(Quote actual) {
        return new QuoteAssert(actual);
    }

    public static AccountAssert assertThat(Account actual) {
        return new AccountAssert(actual);
    }

    protected InsuranceAssertions() {
    }
}
