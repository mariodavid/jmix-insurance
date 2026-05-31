package com.insurance.app.test_support.assertion;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountAssert;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.account.core.entity.AccountDocumentAssert;
import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.entity.PartnerAssert;
import com.insurance.policy.core.entity.Policy;
import com.insurance.policy.core.entity.PolicyAssert;
import com.insurance.quote.core.entity.Quote;
import com.insurance.quote.core.test_support.QuoteAssert;

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

    public static AccountDocumentAssert assertThat(AccountDocument actual) {
        return new AccountDocumentAssert(actual);
    }

    protected InsuranceAssertions() {
    }
}
