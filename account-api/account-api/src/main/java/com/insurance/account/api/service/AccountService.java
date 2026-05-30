package com.insurance.account.api.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AccountService {

    BigDecimal getAccountBalance(String policyNo, LocalDate effectiveDate);
}
