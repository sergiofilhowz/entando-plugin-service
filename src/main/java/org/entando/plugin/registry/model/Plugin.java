package org.entando.plugin.registry.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Plugin {

    private String id;
    private String name;
    private String image;
    private String version;

    private List<EnvironmentVariable> envVariables;
    private Map<String, List<ExternalService>> externalServices;

}
