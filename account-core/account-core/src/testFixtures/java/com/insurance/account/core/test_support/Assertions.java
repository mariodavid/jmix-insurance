package com.insurance.account.core.test_support;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountAssert;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.account.core.entity.AccountDocumentAssert;

public class Assertions extends org.assertj.core.api.Assertions {

    public static AccountAssert assertThat(Account actual) {
        return new AccountAssert(actual);
    }

    public static AccountDocumentAssert assertThat(AccountDocument actual) {
        return new AccountDocumentAssert(actual);
    }

    protected Assertions() {
        // empty
    }
}
