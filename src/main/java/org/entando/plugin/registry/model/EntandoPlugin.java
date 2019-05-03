package org.entando.plugin.registry.model;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@Data
public class EntandoPlugin {

    @Pattern(regexp = "[a-z-]+")
    private String id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String image;

    @NotEmpty
    private String version;

    @NotNull
    private int port;

    @NotNull @Valid
    private List<EnvironmentVariable> envVariables;

    @NotNull @Valid
    private Map<String, List<ExternalService>> externalServices;

}
