package com.insurance.security.test_support;

import com.insurance.security.entity.User;

public class Assertions extends org.assertj.core.api.Assertions {

    public static UserAssert assertThat(User actual) {
        return new UserAssert(actual);
    }

    protected Assertions() {
        // empty
    }
}
