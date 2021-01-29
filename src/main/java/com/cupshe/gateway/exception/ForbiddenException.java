package com.cupshe.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * ForbiddenException
 *
 * @author zxy
 */
@Component
public class ForbiddenException extends RuntimeException implements GatewayException {

    /**
     * 403
     */
    public static final HttpStatus HS = HttpStatus.FORBIDDEN;

    public static final String MESSAGE = HS.value() + " " + HS.getReasonPhrase();

    public ForbiddenException() {
        super(MESSAGE);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HS;
    }
}
