package org.springframework.cloud.cloudfoundry.discovery;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;

import java.util.Collections;
import java.util.List;

/**
 * @author <A href="mailto:josh@joshlong.com">Josh Long</A>
 */
public class CloudFoundryServerList extends AbstractServerList<CloudFoundryServer> {

    private String serviceId;

    private final CloudFoundryClient cloudFoundryClient;

    public CloudFoundryServerList(CloudFoundryClient cloudFoundryClient  ) {
        this.cloudFoundryClient = cloudFoundryClient;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        this.serviceId = iClientConfig.getClientName();
    }

    @Override
    public List<CloudFoundryServer> getInitialListOfServers() {
        return this.cloudFoundryServers();
    }

    @Override
    public List<CloudFoundryServer> getUpdatedListOfServers() {
        return this.cloudFoundryServers();
    }

    protected List<CloudFoundryServer> cloudFoundryServers() {
        CloudApplication cloudApplications = this.cloudFoundryClient.getApplication(this.serviceId);
        return Collections.singletonList( new CloudFoundryServer(cloudApplications));
    }
}
