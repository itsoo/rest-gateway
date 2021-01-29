package com.cupshe.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * UnavailableException
 *
 * @author zxy
 */
@Component
public class UnavailableException extends RuntimeException implements GatewayException {

    /**
     * 503
     */
    public static final HttpStatus HS = HttpStatus.SERVICE_UNAVAILABLE;

    public static final String MESSAGE = HS.value() + " Unavailable, please try again later";

    public UnavailableException() {
        super(MESSAGE);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HS;
    }
}
