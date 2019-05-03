package org.entando.plugin.registry.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {

    @Bean
    public KubernetesClient client() {
        final Config config = new ConfigBuilder()
//                .withMasterUrl("https://mymaster.com")
                .build();
        return new DefaultKubernetesClient(config);
    }

}
