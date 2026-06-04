package com.insurance.app;

import com.google.common.base.Strings;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import javax.sql.DataSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Push
@Theme(value = "insurance-app")
@PWA(name = "Insurance App", shortName = "Insurance App", offline = false)
@JmixModule(
    dependsOn = {
      io.jmix.eclipselink.EclipselinkConfiguration.class,
      io.jmix.flowui.FlowuiConfiguration.class,
      io.jmix.security.SecurityConfiguration.class,
      io.jmix.securityflowui.SecurityFlowuiConfiguration.class,
      io.jmix.securitydata.SecurityDataConfiguration.class,
      io.jmix.datatools.DatatoolsConfiguration.class,
      io.jmix.datatoolsflowui.DatatoolsFlowuiConfiguration.class,
      io.jmix.flowuidata.FlowuiDataConfiguration.class,
      io.jmix.localfs.LocalFileStorageConfiguration.class,
      com.insurance.security.api.SecurityApiConfiguration.class,
      com.insurance.security.SecurityCoreConfiguration.class,
      com.insurance.security.ui.SecurityUiConfiguration.class,
      com.insurance.partner.core.PartnerConfiguration.class,
      com.insurance.partner.ui.PartnerUiConfiguration.class,
      com.insurance.product.core.ProductConfiguration.class,
      com.insurance.policy.core.PolicyConfiguration.class,
      com.insurance.policy.ui.PolicyUiConfiguration.class,
      com.insurance.quote.core.QuoteConfiguration.class,
      com.insurance.quote.ui.QuoteUiConfiguration.class,
      com.insurance.account.core.AccountConfiguration.class,
      com.insurance.account.ui.AccountUiConfiguration.class
    })
@SpringBootApplication
public class InsuranceAppApplication implements AppShellConfigurator {

  @Autowired private Environment environment;

  public static void main(String[] args) {
    SpringApplication.run(InsuranceAppApplication.class, args);
  }

  @Bean
  @Primary
  @ConfigurationProperties("main.datasource")
  DataSourceProperties dataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  @ConfigurationProperties("main.datasource.hikari")
  DataSource dataSource(final DataSourceProperties dataSourceProperties) {
    return dataSourceProperties.initializeDataSourceBuilder().build();
  }

  @EventListener
  public void printApplicationUrl(final ApplicationStartedEvent event) {
    LoggerFactory.getLogger(InsuranceAppApplication.class)
        .info(
            "Application started at "
                + "http://localhost:"
                + environment.getProperty("local.server.port")
                + Strings.nullToEmpty(environment.getProperty("server.servlet.context-path")));
  }

  @Bean("app_ViewControllers")
  public ViewControllersConfiguration screens(
      final ApplicationContext applicationContext,
      final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
    final ViewControllersConfiguration viewControllers =
        new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
    viewControllers.setBasePackages(java.util.Collections.singletonList("com.insurance.app"));
    return viewControllers;
  }

  @Bean("app_Actions")
  public ActionsConfiguration actions(
      final ApplicationContext applicationContext,
      final AnnotationScanMetadataReaderFactory metadataReaderFactory) {
    final ActionsConfiguration actions =
        new ActionsConfiguration(applicationContext, metadataReaderFactory);
    actions.setBasePackages(java.util.Collections.singletonList("com.insurance.app"));
    return actions;
  }
}
