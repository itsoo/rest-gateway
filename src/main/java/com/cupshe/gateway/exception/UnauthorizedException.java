package com.cupshe.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * UnauthorizedException
 *
 * @author zxy
 */
@Component
public class UnauthorizedException extends RuntimeException implements GatewayException {

    /**
     * 401
     */
    public static final HttpStatus HS = HttpStatus.UNAUTHORIZED;

    public static final String MESSAGE = HS.value() + " " + HS.getReasonPhrase();

    public UnauthorizedException() {
        super(MESSAGE);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HS;
    }
}
