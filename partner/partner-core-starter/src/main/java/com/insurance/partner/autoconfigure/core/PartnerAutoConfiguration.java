package com.insurance.partner.autoconfigure.core;

import com.insurance.partner.core.PartnerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({PartnerConfiguration.class})
public class PartnerAutoConfiguration {}
