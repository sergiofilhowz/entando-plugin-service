package org.entando.plugin.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.entando")
public class EntandoPluginJavaApplication {

    public static void main(final String[] args) {
        SpringApplication.run(EntandoPluginJavaApplication.class, args);
    }

}
