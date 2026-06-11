package com.insurance.claim.autoconfigure.api;

import com.insurance.claim.api.ClaimApiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ClaimApiConfiguration.class})
public class ClaimAutoConfiguration {}
