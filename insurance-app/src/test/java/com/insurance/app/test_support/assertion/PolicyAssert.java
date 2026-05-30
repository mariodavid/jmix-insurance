package com.insurance.app.test_support.assertion;

import com.insurance.policy.core.entity.Policy;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PolicyAssert extends AbstractObjectAssert<PolicyAssert, Policy> {

    public PolicyAssert(Policy actual) {
        super(actual, PolicyAssert.class);
    }

    public static PolicyAssert assertThat(Policy actual) {
        return new PolicyAssert(actual);
    }

    public PolicyAssert hasPolicyNoMatchingFormat() {
        isNotNull();
        Assertions.assertThat(actual.getPolicyNo())
                .as("policyNo should match HC-YYYY-NNNNNN format")
                .matches("HC-\\d{4}-\\d{6}");
        return this;
    }

    public PolicyAssert hasCoverageEndOneYearAfter(LocalDate coverageStart) {
        isNotNull();
        Assertions.assertThat(actual.getCoverageEnd())
                .as("coverageEnd should be one year after coverageStart")
                .isEqualTo(coverageStart.plusYears(1));
        return this;
    }

    public PolicyAssert hasCoverageStart(LocalDate coverageStart) {
        isNotNull();
        Assertions.assertThat(actual.getCoverageStart())
                .as("coverageStart")
                .isEqualTo(coverageStart);
        return this;
    }

    public PolicyAssert hasPremium(BigDecimal premium) {
        isNotNull();
        Assertions.assertThat(actual.getPremium())
                .as("premium")
                .isEqualByComparingTo(premium);
        return this;
    }

    public PolicyAssert hasPartnerNo(String partnerNo) {
        isNotNull();
        Assertions.assertThat(actual.getPartnerNo())
                .as("partnerNo")
                .isEqualTo(partnerNo);
        return this;
    }

    public PolicyAssert hasPolicyNo(String policyNo) {
        isNotNull();
        Assertions.assertThat(actual.getPolicyNo())
                .as("policyNo")
                .isEqualTo(policyNo);
        return this;
    }
}
