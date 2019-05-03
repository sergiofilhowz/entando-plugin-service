package org.entando.plugin.registry.service;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.plugin.registry.exception.ExternalServiceNotInstalledException;
import org.entando.plugin.registry.exception.PluginAlreadyDeployedException;
import org.entando.plugin.registry.exception.UnsetEnvVarsException;
import org.entando.plugin.registry.model.EntandoPlugin;
import org.entando.plugin.registry.model.EntandoPluginDeployment;
import org.entando.plugin.registry.model.EnvironmentVariable;
import org.entando.plugin.registry.model.ExternalService;
import org.entando.plugin.registry.response.EntandoPluginDeploymentResponse;
import org.entando.plugin.registry.service.external.DatabaseExternalServiceConfigurer;
import org.entando.plugin.registry.service.external.ExternalServiceConfigurer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class KubernetesService {

    private static final String ENTANDO_PLUGIN_LABEL = "entandoPlugin";

    private final @NonNull KubernetesClient client;
    private final @NonNull PluginService pluginService;

    private static final String namespace = "entando";
    private static final String host = "entando.local";

    public EntandoPluginDeploymentResponse getDeployment(final String pluginId) {
        return ofNullable(client.apps().deployments()
                .inNamespace(namespace).withName(pluginId + "-deployment").get())
                .map(deployment ->  this.map(pluginId, deployment)).orElse(null);
    }

    private EntandoPluginDeploymentResponse map(final String pluginId, final Deployment deployment) {
        final EntandoPluginDeploymentResponse response = new EntandoPluginDeploymentResponse();
        response.setPlugin(pluginId);
        response.setReplicas(deployment.getSpec().getReplicas());

        deployment.getSpec().getTemplate().getSpec().getContainers().stream().findFirst().map(container -> {
            response.setVersion(container.getImage().replaceAll("^.*:(.*)$", "$1"));
            return container.getEnv().stream()
                    .collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));
        }).ifPresent(response::setEnvVariables);

//        deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).g

        client.pods().inNamespace(namespace).withLabel("entandoPlugin=" + pluginId).list().getItems().stream()
                .map(Pod::getStatus)
                .map(PodStatus::getPodIP)
                .findAny().ifPresent(response::setIp);

        return response;
    }

    public EntandoPluginDeploymentResponse createDeployment(final EntandoPluginDeployment pluginDeployment) {
        final EntandoPlugin plugin = pluginService.get(pluginDeployment.getPlugin(), pluginDeployment.getVersion());
        final String pluginId = plugin.getId();

        validate(pluginDeployment, plugin);
        setEnvs(pluginDeployment, plugin);

        final boolean hasPath = isNotEmpty(pluginDeployment.getPath());

        final Service service = newService(plugin, "LoadBalancer");
        client.services().inNamespace(namespace).create(service);

        if (hasPath) {
            client.extensions().ingresses().inNamespace(namespace).create(newIngress(pluginDeployment, plugin));
        }
        final Deployment deployment = client.apps().deployments().inNamespace(namespace).create(newDeployment(pluginDeployment, plugin));
        return map(pluginId, deployment);
    }

    private void setEnvs(final EntandoPluginDeployment pluginDeployment, final EntandoPlugin plugin) {
        plugin.getEnvVariables().stream()
                .filter(env -> isNotEmpty(env.getDefaultValue()))
                .filter(env -> isEmpty(pluginDeployment.getEnvs().get(env.getId())))
                .forEach(env -> pluginDeployment.getEnvs().put(env.getId(), env.getDefaultValue()));

        ofNullable(pluginDeployment.getExternalServices()).ifPresent(external ->
            external.forEach((key, value) -> {
                // should we throw an error on not found external service?
                ofNullable(plugin.getExternalServices().get(key))
                        .map(e -> e.stream().filter(s -> s.getId().equals(value))
                                .findFirst().orElse(null)).ifPresent(service -> configureService(pluginDeployment, service));
            })
        );
    }

    private void configureService(final EntandoPluginDeployment pluginDeployment, final ExternalService service) {
        final EntandoPluginDeploymentResponse deployment = ofNullable(getDeployment(service.getId()))
                .orElseThrow(() -> new ExternalServiceNotInstalledException(service.getId()));
        resolveConfigurer(service).config(pluginDeployment, service, deployment);
    }

    private ExternalServiceConfigurer resolveConfigurer(final ExternalService externalService) {
        // TODO create a Strategy Resolver
        return new DatabaseExternalServiceConfigurer();
    }

    private Ingress newIngress(final EntandoPluginDeployment pluginDeployment, final EntandoPlugin plugin) {
        return new IngressBuilder()
                .withMetadata(from(plugin, "-ingress"))
                .withSpec(buildIngressSpec(pluginDeployment, plugin))
                .build();
    }

    private Deployment newDeployment(final EntandoPluginDeployment pluginDeployment, final EntandoPlugin plugin) {
        return new DeploymentBuilder()
                .withMetadata(from(plugin, "-deployment"))
                .withSpec(buildDeploymentSpec(pluginDeployment, plugin))
                .build();
    }

    private DeploymentSpec buildDeploymentSpec(final EntandoPluginDeployment pluginDeployment, final EntandoPlugin plugin) {
        return new DeploymentBuilder().withNewSpec()
                .withNewSelector()
                .withMatchLabels(labelsFrom(plugin))
                .endSelector()
                .withNewTemplate()
                .withNewMetadata()
                .withName(plugin.getId() + "-pod")
                .withLabels(labelsFrom(plugin))
                .endMetadata()
                .withNewSpec()
                .withHostname(plugin.getId())
                .addNewContainer()
                .withName(plugin.getId() + "-container")
                .withImage(plugin.getImage() + ":" + plugin.getVersion())
                .withEnv(extractEnvs(pluginDeployment))
                .withImagePullPolicy("Always")
                .addNewPort().withName("http").withContainerPort(plugin.getPort()).endPort()
                .withNewReadinessProbe().withNewTcpSocket().withNewPort(plugin.getPort()).endTcpSocket().endReadinessProbe()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec().buildSpec();
    }

    private List<EnvVar> extractEnvs(final EntandoPluginDeployment pluginDeployment) {
        return pluginDeployment.getEnvs().keySet().stream().map(envKey ->
            new EnvVar(envKey, pluginDeployment.getEnvs().get(envKey), null)
        ).collect(Collectors.toList());
    }

    private Service newService(final EntandoPlugin plugin, final String type) {
        return new ServiceBuilder()
                .withMetadata(from(plugin, "-service"))
                .withNewSpec()
                .withSelector(labelsFrom(plugin))
                .addNewPort().withName("http-port").withProtocol("TCP").withPort(plugin.getPort())
                .withTargetPort(new IntOrString(plugin.getPort())).endPort()
                .withType(type)
                .endSpec()
                .build();
    }

    private ObjectMeta from(final EntandoPlugin plugin, String suffix) {
        ObjectMetaBuilder metaBuilder = new ObjectMetaBuilder()
                .withName(plugin.getId() + suffix)
                .withNamespace(namespace)
                .withLabels(labelsFrom(plugin));
        return metaBuilder.build();
    }

    private Map<String, String> labelsFrom(final EntandoPlugin plugin) {
        final HashMap<String, String> labels = new HashMap<>();
        labels.put(ENTANDO_PLUGIN_LABEL, plugin.getId());
        return labels;
    }

    private IngressSpec buildIngressSpec(final EntandoPluginDeployment pluginDeployment, final EntandoPlugin plugin) {
        return new IngressBuilder().withNewSpec().addNewRule()
                //TODO figure out who determines the host. Maybe the API gateway talks directly to a service so there is no need for an ingress
                .withHost(host)
                .withNewHttp()
                .addNewPath()
                .withPath(pluginDeployment.getPath())
                .withNewBackend()
                .withServiceName(plugin.getId() + "-service")
                .withNewServicePort(plugin.getPort())
                .endBackend()
                .endPath()
                .endHttp()
                .endRule().endSpec().buildSpec();
    }

    private void validate(final EntandoPluginDeployment pluginDeployment, final EntandoPlugin plugin) {
        final String pluginId = plugin.getId();
        if (getDeployment(pluginId) != null) {
            throw new PluginAlreadyDeployedException();
        }

        final String unsetVars = plugin.getEnvVariables().stream()
                .filter(variable -> !variable.isOptional() && isEmpty(variable.getDefaultValue()))
                .filter(variable -> isEmpty(pluginDeployment.getEnvs().get(variable.getId())))
                .map(EnvironmentVariable::getId).collect(Collectors.joining(", "));
        if (isNotEmpty(unsetVars)) {
            throw new UnsetEnvVarsException(unsetVars);
        }
    }

}
