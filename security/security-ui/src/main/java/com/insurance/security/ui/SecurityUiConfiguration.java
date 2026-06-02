package com.insurance.security.ui;

import com.insurance.security.SecurityCoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import io.jmix.securityflowui.SecurityFlowuiConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@Configuration
@ComponentScan
@JmixModule(dependsOn = {SecurityCoreConfiguration.class, FlowuiConfiguration.class, SecurityFlowuiConfiguration.class})
@PropertySource(name = "com.insurance.security.ui", value = "classpath:/com/insurance/security/ui/module.properties")
public class SecurityUiConfiguration {

    @Bean("security_SecurityUiViewControllers")
    public ViewControllersConfiguration screens(final ApplicationContext applicationContext,
                                                final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        final ViewControllersConfiguration viewControllers
                = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        viewControllers.setBasePackages(Collections.singletonList("com.insurance.security.ui"));
        return viewControllers;
    }

    @Bean("security_SecurityUiActions")
    public ActionsConfiguration actions(final ApplicationContext applicationContext,
                                        final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        final ActionsConfiguration actions
                = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actions.setBasePackages(Collections.singletonList("com.insurance.security.ui"));
        return actions;
    }
}
