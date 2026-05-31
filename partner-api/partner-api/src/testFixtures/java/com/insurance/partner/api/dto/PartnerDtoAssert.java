package com.insurance.partner.api.dto;

import org.assertj.core.api.AbstractAssert;

import java.util.Objects;
import java.util.UUID;

public class PartnerDtoAssert extends AbstractAssert<PartnerDtoAssert, PartnerDto> {

    public PartnerDtoAssert(PartnerDto actual) {
        super(actual, PartnerDtoAssert.class);
    }

    public static PartnerDtoAssert assertThat(PartnerDto actual) {
        return new PartnerDtoAssert(actual);
    }

    public PartnerDtoAssert hasId(UUID expected) {
        isNotNull();
        if (!Objects.equals(actual.getId(), expected)) {
            failWithMessage("Expected PartnerDto id to be <%s> but was <%s>", expected, actual.getId());
        }
        return this;
    }

    public PartnerDtoAssert hasPartnerNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getPartnerNo(), expected)) {
            failWithMessage("Expected PartnerDto partnerNo to be <%s> but was <%s>", expected, actual.getPartnerNo());
        }
        return this;
    }

    public PartnerDtoAssert hasFirstName(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getFirstName(), expected)) {
            failWithMessage("Expected PartnerDto firstName to be <%s> but was <%s>", expected, actual.getFirstName());
        }
        return this;
    }

    public PartnerDtoAssert hasLastName(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getLastName(), expected)) {
            failWithMessage("Expected PartnerDto lastName to be <%s> but was <%s>", expected, actual.getLastName());
        }
        return this;
    }
}
