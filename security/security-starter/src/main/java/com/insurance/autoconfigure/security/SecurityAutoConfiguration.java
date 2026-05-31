package com.insurance.autoconfigure.security;

import com.insurance.security.SecurityConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({SecurityConfiguration.class})
public class SecurityAutoConfiguration {
}
