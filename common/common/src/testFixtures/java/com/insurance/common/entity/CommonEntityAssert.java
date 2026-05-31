package com.insurance.common.entity;

import org.assertj.core.api.AbstractAssert;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class CommonEntityAssert extends AbstractAssert<CommonEntityAssert, CommonEntity> {

    public CommonEntityAssert(CommonEntity actual) {
        super(actual, CommonEntityAssert.class);
    }

    public static CommonEntityAssert assertThat(CommonEntity actual) {
        return new CommonEntityAssert(actual);
    }

    public CommonEntityAssert hasId(UUID expected) {
        isNotNull();
        if (!Objects.equals(actual.getId(), expected)) {
            failWithMessage("Expected entity id to be <%s> but was <%s>", expected, actual.getId());
        }
        return this;
    }

    public CommonEntityAssert hasVersion(Integer expected) {
        isNotNull();
        if (!Objects.equals(actual.getVersion(), expected)) {
            failWithMessage("Expected entity version to be <%s> but was <%s>", expected, actual.getVersion());
        }
        return this;
    }

    public CommonEntityAssert hasCreatedBy(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getCreatedBy(), expected)) {
            failWithMessage("Expected entity createdBy to be <%s> but was <%s>", expected, actual.getCreatedBy());
        }
        return this;
    }

    public CommonEntityAssert hasCreatedDate(OffsetDateTime expected) {
        isNotNull();
        if (!Objects.equals(actual.getCreatedDate(), expected)) {
            failWithMessage("Expected entity createdDate to be <%s> but was <%s>", expected, actual.getCreatedDate());
        }
        return this;
    }

    public CommonEntityAssert hasLastModifiedBy(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getLastModifiedBy(), expected)) {
            failWithMessage("Expected entity lastModifiedBy to be <%s> but was <%s>", expected, actual.getLastModifiedBy());
        }
        return this;
    }

    public CommonEntityAssert hasLastModifiedDate(OffsetDateTime expected) {
        isNotNull();
        if (!Objects.equals(actual.getLastModifiedDate(), expected)) {
            failWithMessage("Expected entity lastModifiedDate to be <%s> but was <%s>", expected, actual.getLastModifiedDate());
        }
        return this;
    }

    public CommonEntityAssert hasDeletedBy(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getDeletedBy(), expected)) {
            failWithMessage("Expected entity deletedBy to be <%s> but was <%s>", expected, actual.getDeletedBy());
        }
        return this;
    }

    public CommonEntityAssert isNotDeleted() {
        isNotNull();
        if (actual.getDeletedDate() != null) {
            failWithMessage("Expected entity to not be deleted but deletedDate was <%s>", actual.getDeletedDate());
        }
        return this;
    }

    public CommonEntityAssert isDeleted() {
        isNotNull();
        if (actual.getDeletedDate() == null) {
            failWithMessage("Expected entity to be deleted but deletedDate was null");
        }
        return this;
    }
}
