package com.insurance.policy.autoconfigure.api;

import com.insurance.policy.api.PolicyApiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({PolicyApiConfiguration.class})
public class PolicyApiAutoConfiguration {}
