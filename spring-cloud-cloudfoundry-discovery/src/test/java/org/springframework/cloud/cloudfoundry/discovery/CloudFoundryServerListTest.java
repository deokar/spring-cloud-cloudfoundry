package org.springframework.cloud.cloudfoundry.discovery;

import com.netflix.client.config.IClientConfig;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;

/**
 * @author <A href="mailto:josh@Joshlong.com">Josh Long</A>
 */
public class CloudFoundryServerListTest {

    private CloudFoundryServerList cloudFoundryServerList;
    private String serviceId = "foo-service";

    @Before
    public void setUp() {

        CloudApplication cloudApplication = Mockito.mock(CloudApplication.class);
        Mockito.when(cloudApplication.getUris()).then(new Answer<List<String>>() {
            @Override
            public List<String> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return Arrays.asList("a-url.com", "b-url.com") ;
            }
        }) ;

        CloudFoundryClient cloudFoundryClient = Mockito.mock(CloudFoundryClient.class);
        Mockito.when(cloudFoundryClient.getApplication(this.serviceId)).thenReturn(cloudApplication);

        IClientConfig iClientConfig = Mockito.mock(IClientConfig.class);
        Mockito.when(iClientConfig.getClientName()).thenReturn(this.serviceId);

        this.cloudFoundryServerList = new CloudFoundryServerList(cloudFoundryClient);
        this.cloudFoundryServerList.initWithNiwsConfig(iClientConfig);
    }

    @Test
    public void testListOfServers() {
        List<CloudFoundryServer> initialListOfServers = this.cloudFoundryServerList.getInitialListOfServers();
        List<CloudFoundryServer> updatedListOfServers = this.cloudFoundryServerList.getUpdatedListOfServers();
        Assert.assertEquals(updatedListOfServers, initialListOfServers);
        Assert.assertTrue(initialListOfServers.size() == 1);
    }

    @Test
    public void testInit() {
        Assert.assertEquals(this.cloudFoundryServerList.serviceId, this.serviceId);
    }
}
