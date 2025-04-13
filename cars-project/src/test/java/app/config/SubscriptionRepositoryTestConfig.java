package app.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {"app.user.model", "app.subscription.model"})
@EnableJpaRepositories(basePackages = "app.subscription.repository")
public class SubscriptionRepositoryTestConfig {
}