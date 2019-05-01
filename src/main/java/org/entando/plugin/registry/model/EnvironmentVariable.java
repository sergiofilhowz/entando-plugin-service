package org.entando.plugin.registry.model;

import lombok.Data;

@Data
public class EnvironmentVariable {

    private String id;
    private String description;
    private String defaultValue;
    private boolean optional;

}
