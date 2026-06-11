package com.insurance.claim.ui;

import com.insurance.claim.core.ClaimConfiguration;
import com.insurance.partner.api.service.PartnerService;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.security.ui.SecurityUiConfiguration;
import io.jmix.core.annotation.JmixModule;
import javax.sql.DataSource;
import org.mockito.Mockito;
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
@Import({ClaimUiConfiguration.class, ClaimConfiguration.class})
@PropertySource("classpath:/com/insurance/claim/ui/test-app.properties")
@JmixModule(
    id = "com.insurance.claim.ui.test",
    dependsOn = {
      ClaimUiConfiguration.class,
      SecurityUiConfiguration.class,
      ClaimConfiguration.class
    })
public class ClaimUiTestConfiguration {

  @Bean
  @Primary
  DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.HSQL)
        .build();
  }

  @Bean
  @Primary
  PolicyService policyService() {
    return Mockito.mock(PolicyService.class);
  }

  @Bean
  @Primary
  PartnerService partnerService() {
    return Mockito.mock(PartnerService.class);
  }
}
