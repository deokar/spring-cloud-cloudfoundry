/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
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
    CommandLineRunner runner(
            final LoadBalancerClient loadBalancerClient ,
            final DiscoveryClient discoveryClient) {
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

                log.debug("=====================================");
                ServiceInstance choose = loadBalancerClient.choose("hi-service");
                log.info("chose: " + '('+ choose.getServiceId() +  ") "  +
                        choose.getHost() + ':' + choose.getPort());
            }
        };
    }
}

