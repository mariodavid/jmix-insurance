package com.insurance.account.core.entity;

import org.assertj.core.api.AbstractAssert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class AccountAssert extends AbstractAssert<AccountAssert, Account> {

    public AccountAssert(Account actual) {
        super(actual, AccountAssert.class);
    }

    public static AccountAssert assertThat(Account actual) {
        return new AccountAssert(actual);
    }

    public AccountAssert hasAccountNo(String expected) {
        isNotNull();
        if (!Objects.equals(actual.getAccountNo(), expected)) {
            failWithMessage("Expected account accountNo to be <%s> but was <%s>", expected, actual.getAccountNo());
        }
        return this;
    }

    public AccountAssert hasPolicyId(java.util.UUID expected) {
        isNotNull();
        if (!Objects.equals(actual.getPolicyId(), expected)) {
            failWithMessage("Expected account policyId to be <%s> but was <%s>", expected, actual.getPolicyId());
        }
        return this;
    }

    public AccountAssert hasBalance(BigDecimal expected) {
        isNotNull();
        if (actual.getAccountBalance() == null || actual.getAccountBalance().compareTo(expected) != 0) {
            failWithMessage("Expected account balance to be <%s> but was <%s>", expected, actual.getAccountBalance());
        }
        return this;
    }

    public AccountAssert hasDocumentCount(int expected) {
        isNotNull();
        List<AccountDocument> docs = actual.getDocuments();
        if (docs == null || docs.size() != expected) {
            failWithMessage("Expected account to have <%d> documents but had <%d>",
                    expected, docs == null ? 0 : docs.size());
        }
        return this;
    }
}
