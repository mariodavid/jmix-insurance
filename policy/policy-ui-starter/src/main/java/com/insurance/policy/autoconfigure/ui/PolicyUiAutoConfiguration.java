package com.insurance.policy.autoconfigure.ui;

import com.insurance.policy.ui.PolicyUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({PolicyUiConfiguration.class})
public class PolicyUiAutoConfiguration {}
