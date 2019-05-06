package org.entando.plugin.registry.service.external;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.upperCase;

@Service
@RequiredArgsConstructor
public class SpringExternalServiceConfigurerResolver implements ExternalServiceConfigurerResolver {

    private final @NonNull ApplicationContext applicationContext;

    public ExternalServiceConfigurer resolve(final String serviceType) {
        final Class<ExternalServiceConfigurer> clazz = ExternalServiceConfigurer.class;
        try {
            return applicationContext.getBean(String.format("%s.%s", clazz.getSimpleName(), upperCase(serviceType)), clazz);
        } catch (final NoSuchBeanDefinitionException ex) {
            return applicationContext.getBean(String.format("%s.%s", clazz.getSimpleName(), "DEFAULT"), clazz);
        }
    }

}
