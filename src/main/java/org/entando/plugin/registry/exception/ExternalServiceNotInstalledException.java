package org.entando.plugin.registry.exception;

import org.entando.web.exception.HttpException;
import org.springframework.http.HttpStatus;

public class ExternalServiceNotInstalledException extends HttpException {

    public ExternalServiceNotInstalledException(final String id) {
        super(HttpStatus.BAD_REQUEST, "org.entando.error.externalServiceNotInstalled", new Object[] { id });
    }

}
