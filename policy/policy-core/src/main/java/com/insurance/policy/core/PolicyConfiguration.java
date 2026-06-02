package com.insurance.policy.core;

import com.insurance.account.api.AccountApiConfiguration;
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
@JmixModule(dependsOn = {EclipselinkConfiguration.class, AccountApiConfiguration.class, PartnerApiConfiguration.class, PolicyApiConfiguration.class, ProductApiConfiguration.class, io.jmix.security.SecurityConfiguration.class})
@PropertySource(name = "com.insurance.policy.core", value = "classpath:/com/insurance/policy/core/module.properties")
public class PolicyConfiguration {
}
