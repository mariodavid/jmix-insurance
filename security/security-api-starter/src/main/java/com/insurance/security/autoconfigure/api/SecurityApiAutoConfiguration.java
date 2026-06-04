package com.insurance.security.autoconfigure.api;

import com.insurance.security.api.SecurityApiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({SecurityApiConfiguration.class})
public class SecurityApiAutoConfiguration {}
