package com.insurance.security.test_support;

import com.insurance.security.entity.User;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;
import java.util.UUID;

public class UserAssert extends AbstractAssert<UserAssert, User> {

    protected UserAssert(User actual) {
        super(actual, UserAssert.class);
    }

    public static UserAssert assertThat(User actual) {
        return new UserAssert(actual);
    }

    public UserAssert hasId(UUID expectedId) {
        isNotNull();
        if (!Objects.equals(actual.getId(), expectedId)) {
            failWithMessage("Expected user's id to be <%s> but was <%s>", expectedId, actual.getId());
        }
        return this;
    }

    public UserAssert hasUsername(String expectedUsername) {
        isNotNull();
        if (!Objects.equals(actual.getUsername(), expectedUsername)) {
            failWithMessage("Expected user's username to be <%s> but was <%s>", expectedUsername, actual.getUsername());
        }
        return this;
    }

    public UserAssert isActive() {
        isNotNull();
        if (!Boolean.TRUE.equals(actual.getActive())) {
            failWithMessage("Expected user to be active but was not");
        }
        return this;
    }
}
