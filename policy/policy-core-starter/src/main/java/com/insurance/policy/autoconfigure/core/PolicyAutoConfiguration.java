package com.insurance.policy.autoconfigure.core;

import com.insurance.policy.core.PolicyConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({PolicyConfiguration.class})
public class PolicyAutoConfiguration {
}

