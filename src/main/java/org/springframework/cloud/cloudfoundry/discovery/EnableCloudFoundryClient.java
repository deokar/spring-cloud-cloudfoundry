package org.springframework.cloud.cloudfoundry.discovery;


import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.lang.annotation.*;

/**
 * Convenience annotation for clients to enable Cloud Foundry discovery configuration (specifically).
 * Use this (optionally) in case you want discovery and know for sure that it is Cloud Foundry you want.
 * All it does is turn on discovery and let the auto-configuration find the Cloud Foundry classes.
 *
 * @author Dave Syer
 * @author Spencer Gibb
 * @author <A href="mailto:josh@joshlong.com">Josh Long</A>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableDiscoveryClient
public @interface EnableCloudFoundryClient {
}
