package com.insurance.partner.autoconfigure.api;

import com.insurance.partner.api.PartnerApiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({PartnerApiConfiguration.class})
public class PartnerAutoConfiguration {
}

