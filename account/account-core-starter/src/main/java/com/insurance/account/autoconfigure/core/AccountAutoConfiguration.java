package com.insurance.account.autoconfigure.core;

import com.insurance.account.core.AccountConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AccountConfiguration.class})
public class AccountAutoConfiguration {
}

