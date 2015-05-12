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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.InstanceInfo;
import org.cloudfoundry.client.lib.domain.InstanceState;
import org.cloudfoundry.client.lib.domain.InstancesInfo;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * A Cloud Foundry v2 API-aware implementation of the {@link DiscoveryClient discovery client}
 * SPI. Cloud Foundry already retains a registry of running applications which we expose here as services.
 * Newer versions of Cloud Foundry support instance-specific networking, but as the Cloud Foundry client API doesn't
 * yet support that, this {@link DiscoveryClient implementation doesn't either}.
 * <p/>
 * You need to provide an instance of the {@link CloudFoundryClient}. A workable configuration looks like this:
 * <p/>
 * <pre class="code">
 * &#064;Bean
 * CloudFoundryClient cloudFoundryClient(
 * &#064;Value("${MY_CUSTOM_CF_API:https://api.run.pivotal.io}") String api,
 * CloudCredentials cc) throws MalformedURLException {
 * CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(cc, URI.create(api).toURL());
 * cloudFoundryClient.login();
 * return cloudFoundryClient;
 * }
 * </pre>
 * <p/>
 * You can configure all sorts of other things including which Cloud Foundry cloud controller URI to use,
 * how and whether to use an HTTP proxy, and more using alternative constructors. As configured above, the client
 * will talk to all services and applications deployed in <EM>all</EM> spaces and organizations. Use one of the
 * {@link CloudFoundryClient#CloudFoundryClient(CloudCredentials, URL, String, String)}   variants to specify which space
 * and organization to use.
 * <p/>
 *
 * @author <A href="mailto:josh@joshlong.com">Josh Long</A>
 * @author Spencer Gibb
 */
public class CloudFoundryDiscoveryClient implements DiscoveryClient {

    private static final String DESCRIPTION = "Cloud Foundry " + DiscoveryClient.class.getName() + " implementation";

    private final Log log = LogFactory.getLog(getClass());

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    private final CloudFoundryClient cloudFoundryClient;

    private final String vcapApplicationName;

    public CloudFoundryDiscoveryClient(CloudFoundryClient cloudFoundryClient, Environment environment) {

        this.cloudFoundryClient = cloudFoundryClient;

        String vcapApplication = environment.getProperty("VCAP_APPLICATION");

        try {
            JsonNode jsonNode = objectMapper.readTree(vcapApplication);
            JsonNode appNameNode = jsonNode.get("application_name");

            this.vcapApplicationName = appNameNode.toString().replaceAll("\"", "");

            this.log.debug("Current ServiceInstance information...");
            this.log.debug("\tvcapApplicationName: " + this.vcapApplicationName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public ServiceInstance getLocalServiceInstance() {
        CloudApplication application = this.cloudFoundryClient.getApplication(this.vcapApplicationName);
        List<ServiceInstance> serviceInstances =
                this.createServiceInstancesFromCloudApplications(Collections.singletonList(application));
        return serviceInstances.size() > 0 ? serviceInstances.iterator().next() : null;
    }

    @Override
    public List<ServiceInstance> getInstances(String s) {
        CloudApplication applications = this.cloudFoundryClient.getApplication(s);
        return this.createServiceInstancesFromCloudApplications(
                Collections.singletonList(applications));
    }

    private boolean isRunning(CloudApplication ca) {
        InstancesInfo ii = this.cloudFoundryClient.getApplicationInstances(ca);
        List<InstanceInfo> instances;
        if (ii != null && (instances = ii.getInstances()) != null) {
            for (InstanceInfo resolved : instances) {
                InstanceState state = resolved.getState();
                if (state != null && state.equals(InstanceState.RUNNING)) {
                     return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> getServices() {
        List<String> services = new ArrayList<>();
        List<CloudApplication> applications = this.cloudFoundryClient.getApplications();
        Set<String> serviceIds = new HashSet<>();
        for (CloudApplication ca : applications) {
            if (isRunning(ca)) {
                serviceIds.add(ca.getName());
            }
        }
        services.addAll(serviceIds);
        return services;
    }

    protected List<ServiceInstance> createServiceInstancesFromCloudApplications(
            Collection<CloudApplication> cloudApplications) {
        Set<ServiceInstance> serviceInstances = new HashSet<>();
        for (CloudApplication ca : cloudApplications) {
            if (isRunning(ca)) {
                serviceInstances.add(new CloudFoundryServiceInstance(ca));
            }
        }
        List<ServiceInstance> instances = new ArrayList<>();
        instances.addAll(serviceInstances);
        return instances;
    }

    public static class CloudFoundryServiceInstance extends DefaultServiceInstance {

        private final CloudApplication cloudApplication;

        public CloudApplication getCloudApplication() {
            return cloudApplication;
        }

        public CloudFoundryServiceInstance(CloudApplication ca) {
            super(ca.getName(),
                    ca.getUris().iterator().next(),
                    80,
                    false);

            this.cloudApplication = ca;
        }
    }
}