package com.insurance.common;

import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.core.annotation.JmixModule;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.List;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(CommonConfiguration.class)
@PropertySource("classpath:/com/insurance/common/test-app.properties")
@JmixModule(id = "com.insurance.common.test", dependsOn = CommonConfiguration.class)
public class CommonTestConfiguration {

    @Bean
    @Primary
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager() {
        return authentication -> {
            UserDetails user = User.withUsername(String.valueOf(authentication.getPrincipal()))
                    .password("")
                    .authorities(List.of())
                    .build();
            return new SystemAuthenticationToken(user, user.getAuthorities());
        };
    }
}
