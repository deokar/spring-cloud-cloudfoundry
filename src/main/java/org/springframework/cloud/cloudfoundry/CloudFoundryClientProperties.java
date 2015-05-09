package org.springframework.cloud.cloudfoundry;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "cloudfoundry.client" )
public class CloudFoundryClientProperties {

    @NotNull
    private String cloudControllerUrl = "https://api.run.pivotal.io";

    @NotNull
    private String  email ;

    @NotNull
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
