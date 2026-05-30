package com.insurance.partner.core;

import com.insurance.common.CommonConfiguration;
import com.insurance.partner.api.PartnerApiConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import io.jmix.core.repository.EnableJmixDataRepositories;
import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@EnableJmixDataRepositories
@JmixModule(dependsOn = {EclipselinkConfiguration.class, FlowuiConfiguration.class, CommonConfiguration.class, PartnerApiConfiguration.class})
@PropertySource(name = "com.insurance.partner.core", value = "classpath:/com/insurance/partner/core/module.properties")
public class PartnerConfiguration {

    @Bean("partner_PartnerViewControllers")
    public ViewControllersConfiguration screens(final ApplicationContext applicationContext,
                                                final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        final ViewControllersConfiguration viewControllers
                = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        viewControllers.setBasePackages(Collections.singletonList("com.insurance.partner.core"));
        return viewControllers;
    }

    @Bean("partner_PartnerActions")
    public ActionsConfiguration actions(final ApplicationContext applicationContext,
                                        final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        final ActionsConfiguration actions
                = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actions.setBasePackages(Collections.singletonList("com.insurance.partner.core"));
        return actions;
    }
}
