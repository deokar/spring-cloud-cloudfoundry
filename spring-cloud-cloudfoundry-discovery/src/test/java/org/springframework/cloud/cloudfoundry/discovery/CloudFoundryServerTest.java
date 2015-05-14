package org.springframework.cloud.cloudfoundry.discovery;


import com.netflix.loadbalancer.Server;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

/**
 * @author <A href="mailto:josh@Joshlong.com">Josh Long</A>
 */
public class CloudFoundryServerTest {

    private CloudFoundryServer cloudFoundryServer;
    private List<String> urls = Arrays.asList("a-url.com", "b-url.com");
    private String serverName = "server-name";

    @Before
    public void setUp() {
        CloudApplication cloudApplication = Mockito.mock(CloudApplication.class);
        Mockito.when(cloudApplication.getUris()).thenReturn(this.urls);
        Mockito.when(cloudApplication.getName()).thenReturn(this.serverName);
        Mockito.when(cloudApplication.getRunningInstances()).thenReturn(1);
        this.cloudFoundryServer = new CloudFoundryServer(cloudApplication);
    }

    @Test
    public void testProperConstruction() {
        Server.MetaInfo metaInfo = this.cloudFoundryServer.getMetaInfo();

        Assert.assertEquals(metaInfo.getAppName(), this.serverName);
        Assert.assertEquals(metaInfo.getServiceIdForDiscovery(), this.serverName);
        Assert.assertEquals(metaInfo.getInstanceId(), this.serverName);
        Assert.assertEquals(this.cloudFoundryServer.getHost(), this.urls.get(0));
    }
}
