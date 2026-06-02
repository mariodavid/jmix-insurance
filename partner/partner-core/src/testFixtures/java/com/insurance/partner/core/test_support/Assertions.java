package com.insurance.partner.core.test_support;

import com.insurance.partner.core.entity.Partner;
import com.insurance.partner.core.entity.PartnerAssert;

public class Assertions extends org.assertj.core.api.Assertions {

    public static PartnerAssert assertThat(Partner actual) {
        return new PartnerAssert(actual);
    }

    protected Assertions() {
        // empty
    }
}
