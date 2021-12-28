package puretherapie.crm.application.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "puretherapie.crm.person")
public class PersistenceConfiguration {


}
