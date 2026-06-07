package com.insurance.security.core;

import com.insurance.security.api.SecurityApiConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.securitydata.SecurityDataConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(
    dependsOn = {
      SecurityApiConfiguration.class,
      EclipselinkConfiguration.class,
      io.jmix.security.SecurityConfiguration.class,
      SecurityDataConfiguration.class
    })
@PropertySource(
    name = "com.insurance.security",
    value = "classpath:/com/insurance/security/module.properties")
public class SecurityCoreConfiguration {}
