package com.insurance.account.ui;

import com.insurance.account.core.AccountConfiguration;
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
      AccountConfiguration.class,
      FlowuiConfiguration.class,
      io.jmix.securityflowui.SecurityFlowuiConfiguration.class
    })
@PropertySource(
    name = "com.insurance.account.ui",
    value = "classpath:/com/insurance/account/ui/module.properties")
public class AccountUiConfiguration {

  @Bean("account_AccountUiViewControllers")
  public ViewControllersConfiguration screens(
      final ApplicationContext applicationContext,
      final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
    final ViewControllersConfiguration viewControllers =
        new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
    viewControllers.setBasePackages(Collections.singletonList("com.insurance.account.ui"));
    return viewControllers;
  }

  @Bean("account_AccountUiActions")
  public ActionsConfiguration actions(
      final ApplicationContext applicationContext,
      final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
    final ActionsConfiguration actions =
        new ActionsConfiguration(applicationContext, metadataReaderFactory);
    actions.setBasePackages(Collections.singletonList("com.insurance.account.ui"));
    return actions;
  }
}
