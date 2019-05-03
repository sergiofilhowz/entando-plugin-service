package org.entando.plugin.registry.model;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
public class ExternalService {

    @NotEmpty
    private String id;

    @Valid
    private Map<String, PropertyConfig> mapProperties;

    @Valid
    private Map<String, Object> metadata;

}
