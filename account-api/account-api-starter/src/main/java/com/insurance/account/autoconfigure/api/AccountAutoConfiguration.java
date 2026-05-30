package com.insurance.account.autoconfigure.api;

import com.insurance.account.api.AccountApiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AccountApiConfiguration.class})
public class AccountAutoConfiguration {
}

