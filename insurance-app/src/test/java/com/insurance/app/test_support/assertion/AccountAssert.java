package com.insurance.app.test_support.assertion;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;

import java.math.BigDecimal;
import java.util.List;

public class AccountAssert extends AbstractObjectAssert<AccountAssert, Account> {

    public AccountAssert(Account actual) {
        super(actual, AccountAssert.class);
    }

    public static AccountAssert assertThat(Account actual) {
        return new AccountAssert(actual);
    }

    public AccountAssert hasBalance(BigDecimal balance) {
        isNotNull();
        Assertions.assertThat(actual.getAccountBalance())
                .as("accountBalance")
                .isEqualByComparingTo(balance);
        return this;
    }

    public AccountAssert hasAccountNo(String accountNo) {
        isNotNull();
        Assertions.assertThat(actual.getAccountNo())
                .as("accountNo")
                .isEqualTo(accountNo);
        return this;
    }

    public AccountAssert hasDocumentCount(int count) {
        isNotNull();
        List<AccountDocument> docs = actual.getDocuments();
        Assertions.assertThat(docs)
                .as("number of AccountDocuments")
                .hasSize(count);
        return this;
    }
}
