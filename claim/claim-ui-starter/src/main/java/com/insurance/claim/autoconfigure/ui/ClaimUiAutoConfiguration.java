package com.insurance.claim.autoconfigure.ui;

import com.insurance.claim.ui.ClaimUiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ClaimUiConfiguration.class})
public class ClaimUiAutoConfiguration {}
