package com.insurance.account.autoconfigure.ui;

import com.insurance.account.ui.AccountUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AccountUiConfiguration.class})
public class AccountUiAutoConfiguration {
}
