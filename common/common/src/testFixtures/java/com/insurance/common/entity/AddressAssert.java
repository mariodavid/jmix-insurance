package com.insurance.common.entity;

import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class AddressAssert extends AbstractAssert<AddressAssert, Address> {

    public AddressAssert(Address actual) {
        super(actual, AddressAssert.class);
    }

    public static AddressAssert assertThat(Address actual) {
        return new AddressAssert(actual);
    }

    public AddressAssert hasStreet(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getStreet(), expected)) {
            failWithMessage("Expected address street to be <%s> but was <%s>", expected, actual.getStreet());
        }
        return this;
    }

    public AddressAssert hasHouseNumber(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getHouseNumber(), expected)) {
            failWithMessage("Expected address houseNumber to be <%s> but was <%s>", expected, actual.getHouseNumber());
        }
        return this;
    }

    public AddressAssert hasPostalCode(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getPostalCode(), expected)) {
            failWithMessage("Expected address postalCode to be <%s> but was <%s>", expected, actual.getPostalCode());
        }
        return this;
    }

    public AddressAssert hasCity(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getCity(), expected)) {
            failWithMessage("Expected address city to be <%s> but was <%s>", expected, actual.getCity());
        }
        return this;
    }

    public AddressAssert hasVersion(Integer expected) {
        isNotNull();
        if (!Objects.equals(actual.getVersion(), expected)) {
            failWithMessage("Expected address version to be <%s> but was <%s>", expected, actual.getVersion());
        }
        return this;
    }
}
