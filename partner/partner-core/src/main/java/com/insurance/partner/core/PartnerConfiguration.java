package com.insurance.partner.core;

import com.insurance.partner.api.PartnerApiConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.repository.EnableJmixDataRepositories;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@EnableJmixDataRepositories
@JmixModule(
    dependsOn = {
      EclipselinkConfiguration.class,
      PartnerApiConfiguration.class,
      io.jmix.security.SecurityConfiguration.class
    })
@PropertySource(
    name = "com.insurance.partner.core",
    value = "classpath:/com/insurance/partner/core/module.properties")
public class PartnerConfiguration {}
