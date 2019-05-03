package org.entando.plugin.registry.service.external;

import org.entando.plugin.registry.model.EntandoPluginDeployment;
import org.entando.plugin.registry.model.ExternalService;
import org.entando.plugin.registry.response.EntandoPluginDeploymentResponse;

public interface ExternalServiceConfigurer {

    void config(EntandoPluginDeployment deployment,
                ExternalService externalService,
                EntandoPluginDeploymentResponse externalServiceDeployment);

}
