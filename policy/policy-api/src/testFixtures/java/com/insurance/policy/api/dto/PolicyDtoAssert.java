package com.insurance.policy.api.dto;

import org.assertj.core.api.AbstractAssert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class PolicyDtoAssert extends AbstractAssert<PolicyDtoAssert, PolicyDto> {

    public PolicyDtoAssert(PolicyDto actual) {
        super(actual, PolicyDtoAssert.class);
    }

    public static PolicyDtoAssert assertThat(PolicyDto actual) {
        return new PolicyDtoAssert(actual);
    }

    public PolicyDtoAssert hasId(UUID expected) {
        isNotNull();
        if (!Objects.equals(actual.getId(), expected)) {
            failWithMessage("Expected PolicyDto id to be <%s> but was <%s>", expected, actual.getId());
        }
        return this;
    }

    public PolicyDtoAssert hasPolicyNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getPolicyNo(), expected)) {
            failWithMessage("Expected PolicyDto policyNo to be <%s> but was <%s>", expected, actual.getPolicyNo());
        }
        return this;
    }

    public PolicyDtoAssert hasPartnerNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getPartnerNo(), expected)) {
            failWithMessage("Expected PolicyDto partnerNo to be <%s> but was <%s>", expected, actual.getPartnerNo());
        }
        return this;
    }

    public PolicyDtoAssert hasCoverageStart(LocalDate expected) {
        isNotNull();
        if (!Objects.equals(actual.getCoverageStart(), expected)) {
            failWithMessage("Expected PolicyDto coverageStart to be <%s> but was <%s>", expected, actual.getCoverageStart());
        }
        return this;
    }

    public PolicyDtoAssert hasCoverageEnd(LocalDate expected) {
        isNotNull();
        if (!Objects.equals(actual.getCoverageEnd(), expected)) {
            failWithMessage("Expected PolicyDto coverageEnd to be <%s> but was <%s>", expected, actual.getCoverageEnd());
        }
        return this;
    }

    public PolicyDtoAssert hasPremium(BigDecimal expected) {
        isNotNull();
        if (actual.getPremium() == null || actual.getPremium().compareTo(expected) != 0) {
            failWithMessage("Expected PolicyDto premium to be <%s> but was <%s>", expected, actual.getPremium());
        }
        return this;
    }

    public PolicyDtoAssert hasPolicyNoMatchingFormat() {
        isNotNull();
        String policyNo = actual.getPolicyNo();
        if (policyNo == null || !policyNo.matches("HC-\\d{4}-\\d{6}")) {
            failWithMessage("Expected PolicyDto policyNo to match format HC-YYYY-NNNNNN but was <%s>", policyNo);
        }
        return this;
    }
}
