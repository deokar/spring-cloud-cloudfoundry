package org.springframework.cloud.cloudfoundry.sample;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.cloudfoundry.discovery.EnableCloudFoundryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class DemoApplication {
	@Autowired
	DiscoveryClient discoveryClient;

	@RequestMapping("/")
	public List<String> home() {
		return discoveryClient.getServices();
	}

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /*@Bean
    CommandLineRunner runner(final DiscoveryClient discoveryClient) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                for (String svc : discoveryClient.getServices()) {
                    LogFactory.getLog(getClass()).info("service = " + svc);
                }
            }
        };
    }*/
}

