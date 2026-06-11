package com.insurance.claim.core;

import com.insurance.claim.api.ClaimApiConfiguration;
import com.insurance.partner.api.PartnerApiConfiguration;
import com.insurance.policy.api.PolicyApiConfiguration;
import com.insurance.product.api.ProductApiConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(
    dependsOn = {
      EclipselinkConfiguration.class,
      PolicyApiConfiguration.class,
      PartnerApiConfiguration.class,
      ClaimApiConfiguration.class,
      ProductApiConfiguration.class,
      io.jmix.security.SecurityConfiguration.class
    })
@PropertySource(
    name = "com.insurance.claim.core",
    value = "classpath:/com/insurance/claim/core/module.properties")
public class ClaimConfiguration {}
