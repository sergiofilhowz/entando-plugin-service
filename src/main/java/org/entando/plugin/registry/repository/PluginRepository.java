package org.entando.plugin.registry.repository;

import org.entando.plugin.registry.model.Plugin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PluginRepository extends CrudRepository<Plugin, Long> {

    Optional<Plugin> findByPluginIdAndVersion(String pluginId, String version);

    List<Plugin> findByPluginIdOrderByCreatedAt(String pluginId);

}
