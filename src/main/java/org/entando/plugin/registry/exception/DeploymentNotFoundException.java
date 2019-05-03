package org.entando.plugin.registry.exception;

import org.entando.web.exception.NotFoundException;

public class DeploymentNotFoundException extends NotFoundException {

    public DeploymentNotFoundException() {
        super("org.entando.error.deploymentNotFound");
    }

}
