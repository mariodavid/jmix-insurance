package com.insurance.product.core;

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
@JmixModule(dependsOn = {EclipselinkConfiguration.class, ProductApiConfiguration.class})
@PropertySource(name = "com.insurance.product.core", value = "classpath:/com/insurance/product/core/module.properties")
public class ProductConfiguration {
}
