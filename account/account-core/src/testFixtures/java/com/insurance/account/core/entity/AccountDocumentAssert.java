package com.insurance.account.core.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.assertj.core.api.AbstractAssert;

public class AccountDocumentAssert extends AbstractAssert<AccountDocumentAssert, AccountDocument> {

  public AccountDocumentAssert(AccountDocument actual) {
    super(actual, AccountDocumentAssert.class);
  }

  public static AccountDocumentAssert assertThat(AccountDocument actual) {
    return new AccountDocumentAssert(actual);
  }

  public AccountDocumentAssert hasAmount(BigDecimal expected) {
    isNotNull();
    if (actual.getAmount() == null || actual.getAmount().compareTo(expected) != 0) {
      failWithMessage(
          "Expected document amount to be <%s> but was <%s>", expected, actual.getAmount());
    }
    return this;
  }

  public AccountDocumentAssert hasDocumentDate(LocalDate expected) {
    isNotNull();
    if (!Objects.equals(actual.getDocumentDate(), expected)) {
      failWithMessage(
          "Expected document date to be <%s> but was <%s>", expected, actual.getDocumentDate());
    }
    return this;
  }

  public AccountDocumentAssert hasType(DocumentType expected) {
    isNotNull();
    if (!Objects.equals(actual.getType(), expected)) {
      failWithMessage("Expected document type to be <%s> but was <%s>", expected, actual.getType());
    }
    return this;
  }
}
