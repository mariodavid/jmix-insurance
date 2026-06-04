package com.insurance.partner.core.entity;

import java.util.Objects;
import org.assertj.core.api.AbstractAssert;

public class PartnerAssert extends AbstractAssert<PartnerAssert, Partner> {

  public PartnerAssert(Partner actual) {
    super(actual, PartnerAssert.class);
  }

  public static PartnerAssert assertThat(Partner actual) {
    return new PartnerAssert(actual);
  }

  public PartnerAssert hasPartnerNo(String expected) {
    isNotNull();
    if (!Objects.equals(actual.getPartnerNo(), expected)) {
      failWithMessage(
          "Expected partner partnerNo to be <%s> but was <%s>", expected, actual.getPartnerNo());
    }
    return this;
  }

  public PartnerAssert hasPartnerNoMatchingPattern() {
    isNotNull();
    if (actual.getPartnerNo() == null || !actual.getPartnerNo().matches("PT-\\d{5}")) {
      failWithMessage(
          "Expected partner partnerNo to match PT-NNNNN but was <%s>", actual.getPartnerNo());
    }
    return this;
  }

  public PartnerAssert hasFirstName(String expected) {
    isNotNull();
    if (!Objects.equals(actual.getFirstName(), expected)) {
      failWithMessage(
          "Expected partner firstName to be <%s> but was <%s>", expected, actual.getFirstName());
    }
    return this;
  }

  public PartnerAssert hasLastName(String expected) {
    isNotNull();
    if (!Objects.equals(actual.getLastName(), expected)) {
      failWithMessage(
          "Expected partner lastName to be <%s> but was <%s>", expected, actual.getLastName());
    }
    return this;
  }
}
