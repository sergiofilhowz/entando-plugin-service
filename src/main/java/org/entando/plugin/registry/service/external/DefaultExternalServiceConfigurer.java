package org.entando.plugin.registry.service.external;


import org.apache.commons.lang3.StringUtils;
import org.entando.plugin.registry.model.EntandoPluginDeployment;
import org.entando.plugin.registry.model.ExternalService;
import org.entando.plugin.registry.model.PropertyConfig;
import org.entando.plugin.registry.response.EntandoPluginDeploymentResponse;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;

@Service("ExternalServiceConfigurer.DEFAULT")
public class DefaultExternalServiceConfigurer implements ExternalServiceConfigurer {

    private static final String PORT = "service.port";
    private static final String HOST = "service.host";
    private static final String HOST_AND_PORT = "service.hostAndPort";

    @Override
    public void config(final EntandoPluginDeployment deployment,
                       final ExternalService externalService,
                       final EntandoPluginDeploymentResponse externalServiceDeployment) {

        externalService.getMapProperties().forEach((key, value) ->
            ofNullable(resolveValue(value, externalServiceDeployment))
                    .ifPresent(content -> deployment.getEnvs().put(key, content))
        );
    }

    private String resolveValue(final PropertyConfig property, final EntandoPluginDeploymentResponse external) {
        if (StringUtils.isNotEmpty(property.getId())) {
            switch (property.getId()) {
                case PORT: return getPort();
                case HOST: return getHost(external);
                case HOST_AND_PORT: return getHost(external) + ":" + getPort();
                default: return null;
            }
        }
        return StringUtils.isNotEmpty(property.getValue()) ? property.getValue() : null;
    }

    private String getPort() {
        return "8080";
    }

    private String getHost(final EntandoPluginDeploymentResponse external) {
        return external.getIp();
    }

}
