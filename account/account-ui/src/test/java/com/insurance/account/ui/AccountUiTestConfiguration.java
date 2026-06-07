package com.insurance.account.ui;

import com.insurance.security.ui.SecurityUiConfiguration;
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
@Import(AccountUiConfiguration.class)
@PropertySource("classpath:/com/insurance/account/ui/test-app.properties")
@JmixModule(
    id = "com.insurance.account.ui.test",
    dependsOn = {
      AccountUiConfiguration.class,
      SecurityUiConfiguration.class,
      com.insurance.partner.core.PartnerConfiguration.class
    })
public class AccountUiTestConfiguration {

  @Bean
  @Primary
  DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.HSQL)
        .build();
  }
}
