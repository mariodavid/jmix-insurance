package com.insurance.security.autoconfigure.ui;

import com.insurance.security.ui.SecurityUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({SecurityUiConfiguration.class})
public class SecurityUiAutoConfiguration {}
