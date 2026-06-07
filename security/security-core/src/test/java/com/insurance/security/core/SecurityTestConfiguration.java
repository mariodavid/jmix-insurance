package com.insurance.security.core;

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
@Import(SecurityCoreConfiguration.class)
@PropertySource("classpath:/com/insurance/security/test-app.properties")
@JmixModule(id = "com.insurance.security.test", dependsOn = SecurityCoreConfiguration.class)
public class SecurityTestConfiguration {

  @Bean
  @Primary
  DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.HSQL)
        .build();
  }
}
