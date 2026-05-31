package com.insurance.account.core;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.sql.DataSource;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(AccountConfiguration.class)
@PropertySource("classpath:/com/insurance/account/core/test-app.properties")
@JmixModule(id = "com.insurance.account.core.test", dependsOn = {AccountConfiguration.class, com.insurance.security.SecurityConfiguration.class})
public class AccountTestConfiguration {

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
