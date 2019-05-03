package org.entando.plugin.registry.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugin.registry.exception.DeploymentNotFoundException;
import org.entando.plugin.registry.model.EntandoPlugin;
import org.entando.plugin.registry.model.EntandoPluginDeployment;
import org.entando.plugin.registry.response.EntandoPluginDeploymentResponse;
import org.entando.plugin.registry.service.KubernetesService;
import org.entando.plugin.registry.service.PluginService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static java.util.Optional.ofNullable;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class PluginController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final @NonNull KubernetesService kubernetesService;
    private final @NonNull PluginService pluginService;

    @GetMapping(path = "/deployment/{pluginId}", produces = JSON)
    public EntandoPluginDeploymentResponse getDeployment(@PathVariable final String pluginId) {
        return ofNullable(kubernetesService.getDeployment(pluginId))
                .orElseThrow(DeploymentNotFoundException::new);
    }

    @PostMapping(path = "/deploy", consumes = JSON, produces = JSON)
    public EntandoPluginDeploymentResponse deploy(@RequestBody @Valid final EntandoPluginDeployment deployment) {
        log.info("Deploying {}", deployment);
        return kubernetesService.createDeployment(deployment);
    }

    @PostMapping(path = "/register", consumes = JSON, produces = JSON)
    public void registerPlugin(@RequestBody @Valid final EntandoPlugin request) throws JsonProcessingException {
        pluginService.registerPlugin(request);
    }

    @GetMapping(path = "/{pluginId}/{version}", produces = JSON)
    public EntandoPlugin get(@PathVariable final String pluginId, @PathVariable final String version)  {
        return pluginService.get(pluginId, version);
    }

    @GetMapping(path = "/{pluginId}", produces = JSON)
    public List<EntandoPlugin> getVersions(@PathVariable final String pluginId) {
        return pluginService.list(pluginId);
    }

}
