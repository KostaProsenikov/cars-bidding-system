package app.config;

import app.vin.client.VinClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@Configuration
@EnableFeignClients(clients = VinClient.class)
@Import(FeignClientsConfiguration.class)
@AutoConfigureWireMock(port = 8082)
public class VinClientTestConfig {
}