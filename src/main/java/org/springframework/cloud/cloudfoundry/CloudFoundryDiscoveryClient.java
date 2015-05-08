package org.springframework.cloud.cloudfoundry;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.*;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
/*
@Configuration
@PropertySource("classpath:application.properties")
public class DemoApplication {

    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    CloudCredentials cloudCredentials(@Value("${cf.email}") String u,
                                      @Value("${cf.pw}") String pw) {
        return new CloudCredentials(u, pw);
    }

    @Bean
    CloudFoundryClient cloudFoundryClient(
            @Value("${cf.api}") String api, CloudCredentials cc) throws MalformedURLException {

        CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(cc, URI.create(api).toURL());
        cloudFoundryClient.login();
        return cloudFoundryClient;
    }

    @Bean
    CloudFoundryDiscoveryClient cloudFoundryDiscoveryClient(
            CloudFoundryClient cloudFoundryClient, Environment environment) {
        return new CloudFoundryDiscoveryClient(cloudFoundryClient, environment);
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext annotationConfigApplicationContext =
                new AnnotationConfigApplicationContext(DemoApplication.class);

    }

    @Bean
    InitializingBean runner(
            CloudFoundryDiscoveryClient cfdc) {
        return () -> {

            // service enumeration works!
            cfdc.getServices().forEach(serviceId -> System.out.println("serviceId: " + serviceId));

            // loading an isntance for a given service works!
            cfdc.getInstances("bookmark-service")
                    .forEach(ci -> System.out.println(ToStringBuilder.reflectionToString(ci)));


            // does returning a local instance work?
            System.out.println(ToStringBuilder.reflectionToString(cfdc.getLocalServiceInstance()));
        };
    }
} */

/**
 * @author <A href= "mailto:josh@joshlong.com">Josh Long</A>
 */
public class CloudFoundryDiscoveryClient implements DiscoveryClient {

    private final Log log = LogFactory.getLog(getClass());

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            Jackson2ObjectMapperBuilder.json().build();

    private final CloudFoundryClient cloudFoundryClient;

    private final String vcapApplicationName;

    private static final String DESCRIPTION = "Cloud Foundry " + DiscoveryClient.class.getName() + " implementation";

    public CloudFoundryDiscoveryClient(CloudFoundryClient cloudFoundryClient,
                                       Environment environment) {
        this.cloudFoundryClient = cloudFoundryClient;
        String vcap_application = environment.getProperty("VCAP_APPLICATION");
        try {
            JsonNode jsonNode = objectMapper.readTree(vcap_application);
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

        List<ServiceInstance> serviceInstances =
                this.serviceInstances(Arrays.<CloudApplication>asList(
                        this.cloudFoundryClient.getApplication(this.vcapApplicationName)));
        return serviceInstances.size() > 0 ? serviceInstances.iterator().next() : null;

    }

    @Override
    public List<ServiceInstance> getInstances(String s) {
        List<CloudApplication> applications = this.cloudFoundryClient.getApplications();
        List<CloudApplication> collect = applications
                .stream().filter(ca -> ca.getName().equals(s)).collect(Collectors.toList());
        return this.serviceInstances(collect);

    }

    @Override
    public List<String> getServices() {
        List<String> services = new ArrayList<>();
        List<CloudApplication> applications = this.cloudFoundryClient.getApplications();
        services.addAll(applications.stream().map(CloudEntity::getName).collect(Collectors.<String>toSet()));
        return services;
    }

    private List<ServiceInstance> serviceInstances(Collection<CloudApplication> cloudApplications) {
        Set<ServiceInstance> serviceInstances = new HashSet<>();
        cloudApplications.forEach(ca -> {

            InstancesInfo ii = this.cloudFoundryClient.getApplicationInstances(ca);

            Optional.ofNullable(ii)
                    .ifPresent(nonNullII ->
                                    Optional.ofNullable(nonNullII.getInstances())
                                            .ifPresent(instances ->
                                                    instances
                                                            .stream()
                                                            .filter(ai -> ai.getState() != null && ai.getState().equals(InstanceState.RUNNING))
                                                            .forEach(resolvedInstanceInfo ->
                                                                    serviceInstances.add(new CloudFoundryServiceInstance(ca, resolvedInstanceInfo))))
                    );
        });

        List<ServiceInstance> si = new ArrayList<>();
        si.addAll(serviceInstances);
        return si;
    }


    public static class CloudFoundryServiceInstance extends DefaultServiceInstance {

        private final InstanceInfo instancesInfo;

        private final CloudApplication cloudApplication;

        public InstanceInfo getInstanceInfo() {
            return instancesInfo;
        }

        public CloudApplication getCloudApplication() {
            return cloudApplication;
        }

        public CloudFoundryServiceInstance(CloudApplication ca, InstanceInfo ii) {

            super(ca.getName(),
                    String.format("http://%s", ca.getUris().stream().findFirst().get()),
                    80,
                    false);

            this.cloudApplication = ca;
            this.instancesInfo = ii;
        }
    }


}