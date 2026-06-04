package com.insurance.policy.ui;

import com.insurance.policy.core.PolicyConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import java.util.Collections;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@JmixModule(
    dependsOn = {
      PolicyConfiguration.class,
      FlowuiConfiguration.class,
      io.jmix.securityflowui.SecurityFlowuiConfiguration.class
    })
@PropertySource(
    name = "com.insurance.policy.ui",
    value = "classpath:/com/insurance/policy/ui/module.properties")
public class PolicyUiConfiguration {

  @Bean("policy_PolicyUiViewControllers")
  public ViewControllersConfiguration screens(
      final ApplicationContext applicationContext,
      final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
    final ViewControllersConfiguration viewControllers =
        new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
    viewControllers.setBasePackages(Collections.singletonList("com.insurance.policy.ui"));
    return viewControllers;
  }

  @Bean("policy_PolicyUiActions")
  public ActionsConfiguration actions(
      final ApplicationContext applicationContext,
      final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
    final ActionsConfiguration actions =
        new ActionsConfiguration(applicationContext, metadataReaderFactory);
    actions.setBasePackages(Collections.singletonList("com.insurance.policy.ui"));
    return actions;
  }
}
