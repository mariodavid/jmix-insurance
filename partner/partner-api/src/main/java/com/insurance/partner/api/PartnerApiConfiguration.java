package com.insurance.partner.api;

import io.jmix.core.annotation.JmixModule;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = EclipselinkConfiguration.class)
@PropertySource(
    name = "com.insurance.partner.api",
    value = "classpath:/com/insurance/partner/api/module.properties")
public class PartnerApiConfiguration {}
