package com.insurance.partner.api.dto;

public class Assertions extends org.assertj.core.api.Assertions {

    public static PartnerDtoAssert assertThat(PartnerDto actual) {
        return new PartnerDtoAssert(actual);
    }

    protected Assertions() {
    }
}
