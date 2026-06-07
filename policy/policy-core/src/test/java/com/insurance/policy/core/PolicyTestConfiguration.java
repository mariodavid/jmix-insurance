package com.insurance.policy.core;

import io.jmix.core.annotation.JmixModule;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({PolicyConfiguration.class, com.insurance.partner.core.PartnerConfiguration.class})
@PropertySource("classpath:/com/insurance/policy/core/test-app.properties")
@JmixModule(
    id = "com.insurance.policy.core.test",
    dependsOn = {
      PolicyConfiguration.class,
      com.insurance.security.core.SecurityCoreConfiguration.class,
      com.insurance.partner.core.PartnerConfiguration.class
    })
public class PolicyTestConfiguration {

  @Bean
  @Primary
  DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .setType(EmbeddedDatabaseType.HSQL)
        .build();
  }

  @Bean
  FailingPolicyCreatedEventOrchestrator failingPolicyCreatedEventOrchestrator() {
    return new FailingPolicyCreatedEventOrchestrator();
  }

  public static class FailingPolicyCreatedEventOrchestrator {

    private final AtomicBoolean failOnPolicyCreated = new AtomicBoolean();

    void failNextPolicyCreatedEvent() {
      failOnPolicyCreated.set(true);
    }

    @EventListener
    void onPolicyCreated(com.insurance.policy.api.event.PolicyCreatedEvent event) {
      if (failOnPolicyCreated.getAndSet(false)) {
        throw new IllegalStateException(
            "Account orchestration failed for policy " + event.getPolicyNo());
      }
    }
  }
}
