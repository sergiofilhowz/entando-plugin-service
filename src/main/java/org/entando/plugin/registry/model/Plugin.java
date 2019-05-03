package org.entando.plugin.registry.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "plugin")
public class Plugin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Lob
    @Column(name = "env_variables")
    private String envVariables;

    @Lob
    @Column(name = "external_services")
    private String externalServices;

    @Column(name = "plugin_id")
    private String pluginId;

    @Column private String name;
    @Column private String image;
    @Column private String version;
    @Column private int port;

}
