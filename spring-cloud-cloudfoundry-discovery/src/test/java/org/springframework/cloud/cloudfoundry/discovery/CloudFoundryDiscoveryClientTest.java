package org.springframework.cloud.cloudfoundry.discovery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.InstanceInfo;
import org.cloudfoundry.client.lib.domain.InstanceState;
import org.cloudfoundry.client.lib.domain.InstancesInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <A href="mailto:josh@Joshlong.com">Josh Long</A>
 */
public class CloudFoundryDiscoveryClientTest {

    private final Log log = LogFactory.getLog(getClass());

    private CloudFoundryDiscoveryClient cloudFoundryDiscoveryClient;

    private CloudApplication cloudApplication;

    private String hiServiceServiceId = "hi-service";

    private CloudFoundryClient cloudFoundryClient;

    private CloudApplication fakeCloudApplication(String name, String... uri) {
        CloudApplication cloudApplication = Mockito.mock(CloudApplication.class);
        Mockito.when(cloudApplication.getName()).thenReturn(name);
        Mockito.when(cloudApplication.getUris()).thenReturn(Arrays.asList(uri));
        return cloudApplication;
    }

    @Before
    public void setUp() {
        this.cloudFoundryClient = Mockito.mock(CloudFoundryClient.class);
        Environment environment = Mockito.mock(Environment.class);

        Mockito.when(environment.getProperty("VCAP_APPLICATION"))
                .thenReturn("{\"limits\":{\"mem\":1024,\"disk\":1024,\"fds\":16384},\"application_version\":" +
                        "\"36eff082-96d6-498f-8214-508fda72ba65\",\"application_name\":\"" + hiServiceServiceId +
                        "\",\"application_uris\"" +
                        ":[\"" + hiServiceServiceId +
                        ".cfapps.io\"],\"version\":\"36eff082-96d6-498f-8214-508fda72ba65\",\"name\":" +
                        "\"hi-service\",\"space_name\":\"joshlong\",\"space_id\":\"e0cd969c-3461-41ae-abde-4e11bb5acbd1\"," +
                        "\"uris\":[\"hi-service.cfapps.io\"],\"users\":null,\"application_id\":\"af350f7c-88c4-4e35-a04e-698a1dbc7354\"," +
                        "\"instance_id\":\"e4843ca23bd947b28e6d4cb3f9b92cbb\",\"instance_index\":0,\"host\":\"0.0.0.0\",\"port\":61590," +
                        "\"started_at\":\"2015-05-07 20:00:10 +0000\",\"started_at_timestamp\":1431028810,\"start\":\"2015-05-07 20:00:10 +0000\"," +
                        "\"state_timestamp\":1431028810}");

        List<CloudApplication> cloudApplications = new ArrayList<>();
         cloudApplications.add(fakeCloudApplication(this.hiServiceServiceId, "hi-service.cfapps.io", "hi-service-1.cfapps.io"));
        cloudApplications.add(fakeCloudApplication("config-service", "conf-service.cfapps.io", "conf-service-1.cfapps.io"));

        Mockito.when(this.cloudFoundryClient.getApplications())
                .thenReturn(cloudApplications);

        cloudApplication = cloudApplications.get(0);
        Mockito.when(this.cloudFoundryClient.getApplication(this.hiServiceServiceId))
                .thenReturn(cloudApplication);

        Mockito.when(this.cloudFoundryClient.getApplication(this.hiServiceServiceId))
                .thenReturn(this.cloudApplication);

        InstanceInfo instanceInfo = Mockito.mock(InstanceInfo.class);
        InstancesInfo instancesInfo = Mockito.mock(InstancesInfo.class);
        Mockito.when(instancesInfo.getInstances())
                .thenReturn(Collections.singletonList(instanceInfo));
        Mockito.when(instanceInfo.getState())
                .thenReturn(InstanceState.RUNNING);

        Mockito.when(this.cloudFoundryClient.getApplicationInstances(this.cloudApplication))
                .thenReturn(instancesInfo);

        this.cloudFoundryDiscoveryClient = new CloudFoundryDiscoveryClient(cloudFoundryClient, environment);
    }

    @Test
    public void testServiceResolution() {
        List<String> serviceNames = this.cloudFoundryDiscoveryClient.getServices();

        Assert.assertTrue("there should be one registered service.", serviceNames.contains(
                this.hiServiceServiceId));

        for (String serviceName : serviceNames) {
            this.log.debug("\t discovered serviceName: " + serviceName);
        }
    }

    @Test
    public void testInstances() {
        List<ServiceInstance> instances = this.cloudFoundryDiscoveryClient.getInstances(
                this.hiServiceServiceId);
        assertEquals(instances.size(), 1);
    }

    @Test
    public void testLocalServiceInstanceRunning() {

        InstanceInfo instanceInfo = Mockito.mock(InstanceInfo.class);
        InstancesInfo instancesInfo = Mockito.mock(InstancesInfo.class);
        Mockito.when(instancesInfo.getInstances()).thenReturn(Collections.singletonList(instanceInfo));
        Mockito.when(instanceInfo.getState()).thenReturn(InstanceState.RUNNING);

        Mockito.when(cloudFoundryClient.getApplicationInstances(this.cloudApplication)).thenReturn(instancesInfo);

        ServiceInstance localServiceInstance = this.cloudFoundryDiscoveryClient.getLocalServiceInstance();
        assertTrue(localServiceInstance.getHost().contains("hi-service.cfapps.io"));
        assertTrue(localServiceInstance.getServiceId().equals(this.hiServiceServiceId));
        assertEquals(localServiceInstance.getPort(), 80);
    }

    @Test
    public void testLocalServiceInstanceNotRunning() {

        InstanceInfo instanceInfo = Mockito.mock(InstanceInfo.class);
        InstancesInfo instancesInfo = Mockito.mock(InstancesInfo.class);
        Mockito.when(instancesInfo.getInstances()).thenReturn(Collections.singletonList(instanceInfo));
        Mockito.when(instanceInfo.getState()).thenReturn(InstanceState.CRASHED);

        Mockito.when(cloudFoundryClient.getApplicationInstances(this.cloudApplication)).thenReturn(instancesInfo);

        ServiceInstance localServiceInstance = this.cloudFoundryDiscoveryClient.getLocalServiceInstance();
        assertNull(localServiceInstance);
    }

}