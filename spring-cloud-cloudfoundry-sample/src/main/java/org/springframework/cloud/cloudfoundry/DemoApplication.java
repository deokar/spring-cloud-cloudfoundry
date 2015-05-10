package org.springframework.cloud.cloudfoundry;

import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.cloudfoundry.discovery.EnableCloudFoundryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableCloudFoundryClient
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(final DiscoveryClient discoveryClient) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                for (String svc : discoveryClient.getServices()) {
                    LogFactory.getLog(getClass()).info("service = " + svc);
                }
            }
        };
    }
}

