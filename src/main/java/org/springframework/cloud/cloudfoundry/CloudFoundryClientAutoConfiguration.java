package org.springframework.cloud.cloudfoundry;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * Configures Cloud Foundry based {@link org.springframework.cloud.client.discovery.DiscoveryClient}
 *
 * @author Josh Long
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(CloudFoundryClient.class)
@ConditionalOnProperty(value = "cloudfoundry.client.enabled", matchIfMissing = true)
@AutoConfigureBefore(NoopDiscoveryClientAutoConfiguration.class)
public class CloudFoundryClientAutoConfiguration {

    @Autowired
    private CloudFoundryClientProperties cloudFoundryClientProperties;

    @Bean
    @ConditionalOnMissingBean(CloudCredentials.class)
    public CloudCredentials cloudCredentials() {
        return new CloudCredentials(this.cloudFoundryClientProperties.getEmail(),
                this.cloudFoundryClientProperties.getPassword());
    }

    @Bean
    @ConditionalOnMissingBean(CloudFoundryClient.class)
    public CloudFoundryClient cloudFoundryClient(CloudCredentials cc) throws MalformedURLException {
        CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(cc,
                URI.create(this.cloudFoundryClientProperties.getCloudControllerUrl()).toURL());
        cloudFoundryClient.login();
        return cloudFoundryClient;
    }

    @Bean
    @ConditionalOnMissingBean(CloudFoundryDiscoveryClient.class)
    public CloudFoundryDiscoveryClient cloudFoundryDiscoveryClient(
            CloudFoundryClient cloudFoundryClient, Environment environment) {
        return new CloudFoundryDiscoveryClient(cloudFoundryClient, environment);
    }
}
