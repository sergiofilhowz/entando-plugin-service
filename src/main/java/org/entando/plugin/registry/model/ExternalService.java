package org.entando.plugin.registry.model;

import lombok.Data;

import java.util.Map;

@Data
public class ExternalService {

    private String id;
    private Map<String, PropertyConfig> mapProperties;
    private Map<String, Object> metadata;

}
