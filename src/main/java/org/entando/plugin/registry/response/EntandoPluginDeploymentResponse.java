package org.entando.plugin.registry.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;

@Data
public class EntandoPluginDeploymentResponse {

    private String plugin;
    private String version;
    private int replicas;
    private Map<String, String> envVariables;

    @JsonIgnore
    private String ip;

}
