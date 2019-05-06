package org.entando.plugin.registry.service.external;


import org.apache.commons.lang3.StringUtils;
import org.entando.plugin.registry.model.EntandoPluginDeployment;
import org.entando.plugin.registry.model.ExternalService;
import org.entando.plugin.registry.model.PropertyConfig;
import org.entando.plugin.registry.response.EntandoPluginDeploymentResponse;
import org.springframework.stereotype.Service;

import static java.util.Optional.ofNullable;

@Service("ExternalServiceConfigurer.DATABASE")
public class DatabaseExternalServiceConfigurer implements ExternalServiceConfigurer {

    private static final String PORT = "database.port";
    private static final String HOST = "database.host";
    private static final String NAME = "database.name";
    private static final String USER = "database.user";
    private static final String PASS = "database.pass";
    private static final String HOST_AND_PORT = "database.hostAndPort";

    @Override
    public void config(final EntandoPluginDeployment deployment,
                       final ExternalService externalService,
                       final EntandoPluginDeploymentResponse externalServiceDeployment) {

        externalService.getMapProperties().forEach((key, value) ->
            ofNullable(resolveValue(value, deployment, externalServiceDeployment))
                    .ifPresent(content -> deployment.getEnvs().put(key, content))
        );
    }

    private String resolveValue(final PropertyConfig property,
                                final EntandoPluginDeployment deployment,
                                final EntandoPluginDeploymentResponse external) {

        if (StringUtils.isNotEmpty(property.getId())) {
            // TODO generify to Postgres, Oracle, etc
            switch (property.getId()) {
                case PORT: return getPort();
                case HOST: return getHost(external);
                case NAME: return resolveDatabaseName(deployment);
                case USER: return external.getEnvVariables().get("MYSQL_USER");
                case PASS: return external.getEnvVariables().get("MYSQL_PASSWORD");
                case HOST_AND_PORT: return getHost(external) + ":" + getPort();
                default: return null;
            }
        }

        return StringUtils.isNotEmpty(property.getValue()) ? property.getValue() : null;
    }

    private String getHost(final EntandoPluginDeploymentResponse external) {
        return external.getIp();
    }

    private String getPort() {
        return "3306";
    }

    private String resolveDatabaseName(final EntandoPluginDeployment deployment) {
        return "entando_" + deployment.getPlugin().replaceAll("-", "_");
    }

}
