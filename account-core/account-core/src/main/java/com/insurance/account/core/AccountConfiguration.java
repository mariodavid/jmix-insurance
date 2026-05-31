package com.insurance.account.core;

import com.insurance.account.api.AccountApiConfiguration;
import com.insurance.common.CommonConfiguration;
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
@JmixModule(dependsOn = {EclipselinkConfiguration.class, PolicyApiConfiguration.class, CommonConfiguration.class, AccountApiConfiguration.class, ProductApiConfiguration.class})
@PropertySource(name = "com.insurance.account.core", value = "classpath:/com/insurance/account/core/module.properties")
public class AccountConfiguration {
}
