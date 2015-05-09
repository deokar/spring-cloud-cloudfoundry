package org.springframework.cloud.cloudfoundry.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * @author <A href="mailto:josh@joshlong.com">Josh Long</A>
 */
@ConfigurationProperties(prefix = "spring.cloud.cloudfoundry.discovery" )
public class CloudFoundryDiscoveryProperties {


    private String cloudControllerUrl = "https://api.run.pivotal.io";


    private String  email ;


    private String password;

    public String getCloudControllerUrl() {
        return cloudControllerUrl;
    }

    public void setCloudControllerUrl(String cloudControllerUrl) {
        this.cloudControllerUrl = cloudControllerUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
