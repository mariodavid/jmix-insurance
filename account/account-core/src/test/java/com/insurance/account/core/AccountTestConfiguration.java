package com.insurance.account.core;

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
@Import(AccountConfiguration.class)
@PropertySource("classpath:/com/insurance/account/core/test-app.properties")
@JmixModule(
    id = "com.insurance.account.core.test",
    dependsOn = {
      AccountConfiguration.class,
      com.insurance.security.core.SecurityCoreConfiguration.class
    })
public class AccountTestConfiguration {

  @Bean
  @Primary
  DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.HSQL)
        .build();
  }
}
