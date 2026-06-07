package com.insurance.partner.core;

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
@Import(PartnerConfiguration.class)
@PropertySource("classpath:/com/insurance/partner/core/test-app.properties")
@JmixModule(
    id = "com.insurance.partner.core.test",
    dependsOn = {
      PartnerConfiguration.class,
      com.insurance.security.core.SecurityCoreConfiguration.class
    })
public class PartnerTestConfiguration {

  @Bean
  @Primary
  DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.HSQL)
        .build();
  }
}
