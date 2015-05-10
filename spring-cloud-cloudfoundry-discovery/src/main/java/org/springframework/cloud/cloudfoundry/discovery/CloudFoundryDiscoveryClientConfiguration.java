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