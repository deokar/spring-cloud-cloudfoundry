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

package org.springframework.cloud.cloudfoundry.discovery;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author <A href="mailto:josh@joshlong.com">Josh Long</A>
 */
@Configuration
@EnableConfigurationProperties
public class CloudFoundryDiscoveryClientConfiguration {

    @Autowired
    private CloudFoundryDiscoveryProperties cloudFoundryDiscoveryProperties;

    @Bean
    @ConditionalOnMissingBean(CloudCredentials.class)
    public CloudCredentials cloudCredentials() {
        return new CloudCredentials(this.cloudFoundryDiscoveryProperties.getEmail(),
                this.cloudFoundryDiscoveryProperties.getPassword());
    }

    @Bean
    @ConditionalOnMissingBean(CloudFoundryClient.class)
    public CloudFoundryClient cloudFoundryClient(CloudCredentials cc) throws MalformedURLException {
        CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(cc,
                URI.create(this.cloudFoundryDiscoveryProperties.getCloudControllerUrl()).toURL());
        cloudFoundryClient.login();
        return cloudFoundryClient;
    }

    @Bean
    @ConditionalOnMissingBean(CloudFoundryDiscoveryClient.class)
    public CloudFoundryDiscoveryClient cloudFoundryDiscoveryClient(
            CloudFoundryClient cloudFoundryClient, Environment environment) {
        return new CloudFoundryDiscoveryClient(cloudFoundryClient, environment);
    }

    @Bean
    public CloudFoundryDiscoveryProperties cloudFoundryDiscoveryProperties() {
        return new CloudFoundryDiscoveryProperties();
    }
}