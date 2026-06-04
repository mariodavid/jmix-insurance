package com.insurance.security.autoconfigure.core;

import com.insurance.security.SecurityCoreConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({SecurityCoreConfiguration.class})
public class SecurityCoreAutoConfiguration {}
