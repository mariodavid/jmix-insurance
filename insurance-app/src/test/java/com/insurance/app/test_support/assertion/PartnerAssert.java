package com.insurance.app.test_support.assertion;

import com.insurance.partner.core.entity.Partner;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;

public class PartnerAssert extends AbstractObjectAssert<PartnerAssert, Partner> {

    public PartnerAssert(Partner actual) {
        super(actual, PartnerAssert.class);
    }

    public static PartnerAssert assertThat(Partner actual) {
        return new PartnerAssert(actual);
    }

    public PartnerAssert hasPartnerNoMatchingPattern() {
        isNotNull();
        Assertions.assertThat(actual.getPartnerNo())
                .as("partnerNo should match PT-NNNNN pattern")
                .matches("PT-\\d{5}");
        return this;
    }

    public PartnerAssert hasFirstName(String firstName) {
        isNotNull();
        Assertions.assertThat(actual.getFirstName())
                .as("firstName")
                .isEqualTo(firstName);
        return this;
    }

    public PartnerAssert hasLastName(String lastName) {
        isNotNull();
        Assertions.assertThat(actual.getLastName())
                .as("lastName")
                .isEqualTo(lastName);
        return this;
    }

    public PartnerAssert hasPartnerNo(String partnerNo) {
        isNotNull();
        Assertions.assertThat(actual.getPartnerNo())
                .as("partnerNo")
                .isEqualTo(partnerNo);
        return this;
    }
}
