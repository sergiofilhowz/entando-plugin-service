package org.entando.plugin.registry.service.external;


import org.apache.commons.lang3.StringUtils;
import org.entando.plugin.registry.model.EntandoPluginDeployment;
import org.entando.plugin.registry.model.ExternalService;
import org.entando.plugin.registry.model.PropertyConfig;
import org.entando.plugin.registry.response.EntandoPluginDeploymentResponse;

import static java.util.Optional.ofNullable;

public class DatabaseExternalServiceConfigurer implements ExternalServiceConfigurer {

    private static final String PORT = "database.port";
    private static final String HOST = "database.host";
    private static final String NAME = "database.name";
    private static final String USER = "database.user";
    private static final String PASS = "database.pass";

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
                                final EntandoPluginDeploymentResponse externalServiceDeployment) {

        if (StringUtils.isNotEmpty(property.getId())) {
            // TODO generify to Postgres, Oracle, etc
            switch (property.getId()) {
                case PORT: return "3306";
                case HOST: return externalServiceDeployment.getIp();
                case NAME: return resolveDatabaseName(deployment);
                case USER: return externalServiceDeployment.getEnvVariables().get("MYSQL_USER");
                case PASS: return externalServiceDeployment.getEnvVariables().get("MYSQL_PASSWORD");
                default: return null;
            }
        }

        return StringUtils.isNotEmpty(property.getValue()) ? property.getValue() : null;
    }

    private String resolveDatabaseName(final EntandoPluginDeployment deployment) {
        return "entando_" + deployment.getPlugin().replaceAll("-", "_");
    }

}
