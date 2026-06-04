package com.insurance.policy.core.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.assertj.core.api.AbstractAssert;

public class PolicyAssert extends AbstractAssert<PolicyAssert, Policy> {

  public PolicyAssert(Policy actual) {
    super(actual, PolicyAssert.class);
  }

  public static PolicyAssert assertThat(Policy actual) {
    return new PolicyAssert(actual);
  }

  public PolicyAssert hasPolicyNo(String expected) {
    isNotNull();
    if (!Objects.equals(actual.getPolicyNo(), expected)) {
      failWithMessage(
          "Expected policy policyNo to be <%s> but was <%s>", expected, actual.getPolicyNo());
    }
    return this;
  }

  public PolicyAssert hasPolicyNoMatchingFormat() {
    isNotNull();
    if (actual.getPolicyNo() == null || !actual.getPolicyNo().matches("HC-\\d{4}-\\d{6}")) {
      failWithMessage(
          "Expected policy policyNo to match HC-YYYY-NNNNNN but was <%s>", actual.getPolicyNo());
    }
    return this;
  }

  public PolicyAssert hasPartnerNo(String expected) {
    isNotNull();
    if (!Objects.equals(actual.getPartnerNo(), expected)) {
      failWithMessage(
          "Expected policy partnerNo to be <%s> but was <%s>", expected, actual.getPartnerNo());
    }
    return this;
  }

  public PolicyAssert hasCoverageStart(LocalDate expected) {
    isNotNull();
    if (!Objects.equals(actual.getCoverageStart(), expected)) {
      failWithMessage(
          "Expected policy coverageStart to be <%s> but was <%s>",
          expected, actual.getCoverageStart());
    }
    return this;
  }

  public PolicyAssert hasCoverageEndOneYearAfter(LocalDate coverageStart) {
    isNotNull();
    LocalDate expected = coverageStart.plusYears(1);
    if (!Objects.equals(actual.getCoverageEnd(), expected)) {
      failWithMessage(
          "Expected policy coverageEnd to be one year after <%s> (i.e. <%s>) but was <%s>",
          coverageStart, expected, actual.getCoverageEnd());
    }
    return this;
  }

  public PolicyAssert hasPremium(BigDecimal expected) {
    isNotNull();
    if (actual.getPremium() == null || actual.getPremium().compareTo(expected) != 0) {
      failWithMessage(
          "Expected policy premium to be <%s> but was <%s>", expected, actual.getPremium());
    }
    return this;
  }
}
