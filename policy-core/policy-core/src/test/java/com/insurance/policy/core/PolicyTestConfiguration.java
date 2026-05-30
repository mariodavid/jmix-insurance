package com.insurance.policy.core;

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
import java.util.Collections;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(PolicyConfiguration.class)
@PropertySource("classpath:/com/insurance/policy/core/test-app.properties")
@JmixModule(id = "com.insurance.policy.core.test",
        dependsOn = {PolicyConfiguration.class,
                     io.jmix.security.SecurityConfiguration.class,
                     io.jmix.securitydata.SecurityDataConfiguration.class})
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
    @Primary
    UserRepository userRepository() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin")
                .authorities("ROLE_test-full-access")
                .build();
        repository.addUser(admin);
        return repository;
    }
}
