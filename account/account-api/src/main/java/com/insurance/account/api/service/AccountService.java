package com.insurance.account.api.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Public account API used by other modules to query accounting information.
 *
 * <p>The account module owns account balances and accounting documents. Other modules should depend
 * on this contract instead of account-core classes.
 */
public interface AccountService {

  /**
   * Calculates the account balance for the account associated with the given policy number at a
   * specific effective date.
   *
   * <p>Only accounting documents with a document date on or before the effective date are included
   * in the returned balance.
   *
   * @param policyNo the policy number used as the account number
   * @param effectiveDate the date for which the balance should be calculated
   * @return the calculated balance, or {@link BigDecimal#ZERO} if no account exists
   * @throws IllegalArgumentException if the requested date is after the account's policy snapshot
   *     period
   */
  BigDecimal getAccountBalance(String policyNo, LocalDate effectiveDate);
}
