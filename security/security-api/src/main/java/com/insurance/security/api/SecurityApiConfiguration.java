package com.insurance.security.api;

import io.jmix.core.annotation.JmixModule;
import io.jmix.security.SecurityConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@JmixModule(dependsOn = {SecurityConfiguration.class})
@PropertySource(name = "com.insurance.security.api", value = "classpath:/com/insurance/security/api/module.properties")
public class SecurityApiConfiguration {
}
