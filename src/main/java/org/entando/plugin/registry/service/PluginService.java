package org.entando.plugin.registry.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.entando.plugin.registry.exception.PluginNotFoundException;
import org.entando.plugin.registry.exception.VersionAlreadyExistsException;
import org.entando.plugin.registry.model.EntandoPlugin;
import org.entando.plugin.registry.model.EnvironmentVariable;
import org.entando.plugin.registry.model.ExternalService;
import org.entando.plugin.registry.model.Plugin;
import org.entando.plugin.registry.repository.PluginRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PluginService {

    private final @NonNull PluginRepository repository;
    private final @NonNull ObjectMapper objectMapper;

    public void registerPlugin(final EntandoPlugin plugin) throws JsonProcessingException {
        repository.findByPluginIdAndVersion(plugin.getId(), plugin.getVersion())
                .ifPresent(p -> { throw new VersionAlreadyExistsException(); });
        final Plugin registry = new Plugin();
        registry.setCreatedAt(LocalDateTime.now());
        registry.setEnvVariables(objectMapper.writeValueAsString(plugin.getEnvVariables()));
        registry.setExternalServices(objectMapper.writeValueAsString(plugin.getExternalServices()));
        registry.setImage(plugin.getImage());
        registry.setName(plugin.getName());
        registry.setPluginId(plugin.getId());
        registry.setVersion(plugin.getVersion());
        registry.setPort(plugin.getPort());

        repository.save(registry);
    }

    public EntandoPlugin get(final String pluginId, final String version) {
        return repository.findByPluginIdAndVersion(pluginId, version)
                .map(this::map)
                .orElseThrow(PluginNotFoundException::new);
    }

    public List<EntandoPlugin> list(final String pluginId) {
        return repository.findByPluginIdOrderByCreatedAt(pluginId).stream().map(this::map)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private EntandoPlugin map(final Plugin plugin) {
        try {
            final EntandoPlugin result = new EntandoPlugin();
            result.setId(plugin.getPluginId());
            result.setImage(plugin.getImage());
            result.setName(plugin.getName());
            result.setVersion(plugin.getVersion());
            result.setPort(plugin.getPort());
            result.setEnvVariables(objectMapper.readValue(plugin.getEnvVariables(), new TypeReference<List<EnvironmentVariable>>(){}));
            result.setExternalServices(objectMapper.readValue(plugin.getExternalServices(), new TypeReference<Map<String, List<ExternalService>>>(){}));
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
