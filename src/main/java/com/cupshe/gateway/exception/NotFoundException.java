package com.cupshe.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * NotFoundException
 *
 * @author zxy
 */
@Component
public class NotFoundException extends RuntimeException implements GatewayException {

    /**
     * 404
     */
    public static final HttpStatus HS = HttpStatus.NOT_FOUND;

    public static final String MESSAGE = HS.value() + " " + HS.getReasonPhrase();

    public NotFoundException() {
        super(MESSAGE);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HS;
    }
}
