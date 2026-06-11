package com.insurance.claim.autoconfigure.core;

import com.insurance.claim.core.ClaimConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ClaimConfiguration.class})
public class ClaimAutoConfiguration {}
