package org.springframework.cloud.cloudfoundry.sample;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 * @author Spencer Gibb
 */
@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private Log log = LogFactory.getLog(getClass());

    @Bean
    CommandLineRunner runner(final DiscoveryClient discoveryClient) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {


                log.debug("=====================================");
                for (String svc : discoveryClient.getServices()) {
                    log.info("service = " + svc);
                    List<ServiceInstance> instances = discoveryClient.getInstances(svc);
                    if (instances.size() > 0) {
                        log.info ("\t"+ ReflectionToStringBuilder.reflectionToString(
                                instances.iterator().next(), ToStringStyle.MULTI_LINE_STYLE));
                    }
                }

                log.debug("=====================================");
                log.info ("local: ") ;
                log.info ("\t"+ ReflectionToStringBuilder.reflectionToString(
                        discoveryClient.getLocalServiceInstance(), ToStringStyle.MULTI_LINE_STYLE));
            }
        };
    }
}

