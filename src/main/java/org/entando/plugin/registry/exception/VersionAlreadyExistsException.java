package org.entando.plugin.registry.exception;

import org.entando.web.exception.HttpException;
import org.springframework.http.HttpStatus;

public class VersionAlreadyExistsException extends HttpException {

    public VersionAlreadyExistsException() {
        super(HttpStatus.BAD_REQUEST, "org.entando.error.pluginVersionAlreadyExists");
    }
}
