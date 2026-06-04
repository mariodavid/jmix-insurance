package com.insurance.quote.core;

import io.jmix.core.annotation.JmixModule;
import javax.sql.DataSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(QuoteConfiguration.class)
@PropertySource("classpath:/com/insurance/quote/core/test-app.properties")
@JmixModule(
    id = "com.insurance.quote.core.test",
    dependsOn = {QuoteConfiguration.class, com.insurance.security.SecurityCoreConfiguration.class})
public class QuoteTestConfiguration {

  @Bean
  @Primary
  DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.HSQL)
        .build();
  }

  @Bean
  com.insurance.policy.api.service.PolicyService policyService() {
    return org.mockito.Mockito.mock(com.insurance.policy.api.service.PolicyService.class);
  }
}
