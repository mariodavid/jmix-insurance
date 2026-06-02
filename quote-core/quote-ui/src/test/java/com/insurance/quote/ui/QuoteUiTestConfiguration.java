package com.insurance.quote.ui;

import com.insurance.partner.api.service.PartnerService;
import com.insurance.policy.api.service.PolicyService;
import com.insurance.quote.core.QuoteConfiguration;
import com.insurance.security.SecurityConfiguration;
import io.jmix.core.annotation.JmixModule;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({QuoteUiConfiguration.class, QuoteConfiguration.class})
@PropertySource("classpath:/com/insurance/quote/ui/test-app.properties")
@JmixModule(id = "com.insurance.quote.ui.test", dependsOn = {
        QuoteUiConfiguration.class,
        SecurityConfiguration.class,
        QuoteConfiguration.class
})
public class QuoteUiTestConfiguration {

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
