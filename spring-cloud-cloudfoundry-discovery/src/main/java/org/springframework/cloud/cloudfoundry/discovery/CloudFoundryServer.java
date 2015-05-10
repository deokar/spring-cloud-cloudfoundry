package org.springframework.cloud.cloudfoundry.discovery;

import com.netflix.loadbalancer.Server;
import org.cloudfoundry.client.lib.domain.CloudApplication;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</A>
 */
public class CloudFoundryServer extends Server {

    private final MetaInfo metaInfo;

    public CloudFoundryServer(final CloudApplication cloudApplication) {

        super(cloudApplication.getUris().iterator().next(), 80);

        this.metaInfo = new MetaInfo() {
            @Override
            public String getAppName() {
                return cloudApplication.getName();
            }

            @Override
            public String getServerGroup() {
                return null;
            }

            @Override
            public String getServiceIdForDiscovery() {
                return cloudApplication.getName();
            }

            @Override
            public String getInstanceId() {
                return cloudApplication.getName();
            }
        };
    }

    @Override
    public MetaInfo getMetaInfo() {
        return metaInfo;
    }
}