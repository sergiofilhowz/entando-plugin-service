package org.entando.plugin.registry.service.external;

public interface ExternalServiceConfigurerResolver {

    ExternalServiceConfigurer resolve(String serviceType);

}
